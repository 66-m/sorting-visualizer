package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.SphereHoopsSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

public class SphereHoops extends Visualization implements ConfigurableVisualization {

  private volatile SphereHoopsSettings settings = SphereHoopsSettings.defaults();

  private static final int SEGMENTS = 48;

  /** cos/sin of {@code t = 2π·seg/SEGMENTS}; identical for every hoop, so computed once. */
  private static final float[] SEG_COS = new float[SEGMENTS + 1];

  private static final float[] SEG_SIN = new float[SEGMENTS + 1];

  static {
    for (int seg = 0; seg <= SEGMENTS; seg++) {
      float t = (float) (2 * Math.PI * seg / SEGMENTS);
      SEG_COS[seg] = (float) Math.cos(t);
      SEG_SIN[seg] = (float) Math.sin(t);
    }
  }

  private float[] hoopWidths;
  private float[] zOffsets;
  private int cacheLength = -1;
  private int cacheRadius = -1;

  private float[] xyzxyz;
  private int[] lineArgb;
  private int lineCount;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedLength = -1;
  private int cachedRadius = -1;
  private ColorGradient cachedGradient;

  public SphereHoops(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "3D - Sphere Hoops";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof SphereHoopsSettings s) {
      settings = s;
      cacheLength = -1;
      cachedRevision = Long.MIN_VALUE;
    }
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void rebuildGeometry(int length, int radius) {
    if (cacheLength == length && cacheRadius == radius) {
      return;
    }
    cacheLength = length;
    cacheRadius = radius;
    if (hoopWidths == null || hoopWidths.length < length) {
      hoopWidths = new float[length];
      zOffsets = new float[length];
    }
    for (int i = 0; i < length; i++) {
      float wi = (float) Math.sqrt(1 - Math.pow((((float) i / length) * 2 - 1), 2));
      hoopWidths[i] = (int) VisMath.map(wi, 0, 1, 0, radius);
      zOffsets[i] = radius / 2f - VisMath.map(i, 0, length, 0, radius);
    }
  }

  private boolean linesNeedRebuild(int length, int radius) {
    long rev = arrayModel.getVisualRevision();
    return cachedRevision != rev
        || cachedWidth != screenWidth
        || cachedHeight != screenHeight
        || cachedLength != length
        || cachedRadius != radius
        || cachedGradient != colorGradient
        || xyzxyz == null;
  }

  private void rebuildLines(int length, int radius, float centerX, float centerY, float centerZ) {
    int maxLines = length * SEGMENTS;
    if (xyzxyz == null || xyzxyz.length < maxLines * 6) {
      xyzxyz = new float[maxLines * 6];
      lineArgb = new int[maxLines];
    }

    float cosX = VisMath.COS_ROT_X_PI_3;
    float sinX = VisMath.SIN_ROT_X_PI_3;

    lineCount = 0;
    for (int i = 0; i < length; i++) {
      if (arrayModel.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      float w = hoopWidths[i];
      float zLocal = zOffsets[i];
      int rgb = colorGradient.getMarkerArgb(arrayModel.get(i), arrayModel.getMarker(i));
      float prevX = 0, prevY = 0, prevZ = 0;
      for (int seg = 0; seg <= SEGMENTS; seg++) {
        float lx = (w * 0.5f) * SEG_COS[seg];
        float ly = (w * 0.5f) * SEG_SIN[seg];
        float lz = zLocal;
        // rotateX then translate to center
        float y2 = cosX * ly - sinX * lz;
        float z2 = sinX * ly + cosX * lz;
        float wx = toWorldX(centerX + lx);
        float wy = toWorldY(centerY + y2);
        float wz = centerZ + z2;
        if (seg > 0) {
          int o = lineCount * 6;
          xyzxyz[o] = prevX;
          xyzxyz[o + 1] = prevY;
          xyzxyz[o + 2] = prevZ;
          xyzxyz[o + 3] = wx;
          xyzxyz[o + 4] = wy;
          xyzxyz[o + 5] = wz;
          lineArgb[lineCount] = rgb;
          lineCount++;
        }
        prevX = wx;
        prevY = wy;
        prevZ = wz;
      }
    }
    cachedRevision = arrayModel.getVisualRevision();
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedLength = length;
    cachedRadius = radius;
    cachedGradient = colorGradient;
  }

  @Override
  public void update(float delta) {
    SphereHoopsSettings s = settings;
    int screenMin = Math.min(screenHeight, screenWidth);
    int radius = (int) (screenMin * s.globeScale());
    int length = arrayModel.getLength();
    float centerZ = -(int) (screenMin / 10);
    float centerX = (float) screenWidth / 2;
    float centerY = (float) screenHeight / 2;

    rebuildGeometry(length, radius);
    if (linesNeedRebuild(length, radius)) {
      rebuildLines(length, radius, centerX, centerY, centerZ);
    }

    rs.begin3D();
    rs.strokeWeight(1f);
    rs.strokeLines3D(xyzxyz, lineArgb, lineCount);
    rs.end3D();
  }
}
