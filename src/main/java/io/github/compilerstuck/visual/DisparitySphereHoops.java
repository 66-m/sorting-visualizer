package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.DisparitySphereHoopsSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class DisparitySphereHoops extends Visualization implements ConfigurableVisualization {

  private volatile DisparitySphereHoopsSettings settings = DisparitySphereHoopsSettings.defaults();

  private static final int SEGMENTS = 48;

  private float[] wiBase;
  private float[] zOffsets;
  private int[] sphereWi;
  private int[] colorsRgb;
  private int cacheLength = -1;
  private int cacheRadius = -1;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedLength = -1;
  private int cachedRadius = -1;
  private ColorGradient cachedGradient;

  private float[] xyzxyz;
  private int[] lineArgb;

  public DisparitySphereHoops(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "3D - Disparity Sphere Hoops";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof DisparitySphereHoopsSettings s) {
      settings = s;
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
    if (wiBase == null || wiBase.length < length) {
      wiBase = new float[length];
      zOffsets = new float[length];
      sphereWi = new int[length];
      colorsRgb = new int[length];
    }
    for (int i = 0; i < length; i++) {
      wiBase[i] = (float) Math.sqrt(1 - Math.pow((float) i / length * 2 - 1, 2));
      zOffsets[i] = radius / 2f - VisMath.map(i, 0, length, 0, radius);
    }
  }

  private void ensureSphereWiAndColors(int length, int radius) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedLength == length
        && cachedRadius == radius
        && cachedGradient == colorGradient) {
      return;
    }
    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));

      float barHeight =
          -(float)
              (1f
                  / length
                  * (length
                      - 2
                          * Math.min(
                              Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                              Math.abs(i + length - value))));
      float wi = wiBase[i] * barHeight;
      sphereWi[i] = (int) VisMath.map(wi, 0, 1, 0, radius);

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      colorsRgb[i] = color.getRGB();
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedLength = length;
    cachedRadius = radius;
    cachedGradient = colorGradient;
  }

  @Override
  public void update(float delta) {
    DisparitySphereHoopsSettings s = settings;
    int screenMin = Math.min(screenHeight, screenWidth);
    int radius = (int) (screenMin * s.globeScale());
    int length = arrayController.getLength();
    float centerZ = -(int) (screenMin / 10);
    float centerX = (float) screenWidth / 2;
    float centerY = (float) screenHeight / 2;

    rebuildGeometry(length, radius);
    ensureSphereWiAndColors(length, radius);

    float rotX = VisMath.PI / 3;
    float cosX = (float) Math.cos(rotX);
    float sinX = (float) Math.sin(rotX);

    int maxLines = length * SEGMENTS;
    if (xyzxyz == null || xyzxyz.length < maxLines * 6) {
      xyzxyz = new float[maxLines * 6];
      lineArgb = new int[maxLines];
    }

    int lineCount = 0;
    for (int i = 0; i < length; i++) {
      float w = sphereWi[i];
      float zLocal = zOffsets[i];
      int rgb = colorsRgb[i];
      float prevX = 0, prevY = 0, prevZ = 0;
      for (int seg = 0; seg <= SEGMENTS; seg++) {
        float t = (float) (2 * Math.PI * seg / SEGMENTS);
        float lx = (w * 0.5f) * (float) Math.cos(t);
        float ly = (w * 0.5f) * (float) Math.sin(t);
        float lz = zLocal;
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

    rs.begin3D();
    rs.strokeWeight(1f);
    rs.strokeLines3D(xyzxyz, lineArgb, lineCount);
    rs.end3D();
  }
}
