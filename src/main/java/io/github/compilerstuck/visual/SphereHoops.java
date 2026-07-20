package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.SphereHoopsSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class SphereHoops extends Visualization implements ConfigurableVisualization {

  private volatile SphereHoopsSettings settings = SphereHoopsSettings.defaults();

  private static final int SEGMENTS = 48;

  private float[] hoopWidths;
  private float[] zOffsets;
  private int cacheLength = -1;
  private int cacheRadius = -1;

  private float[] xyzxyz;
  private int[] lineArgb;

  public SphereHoops(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
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

  @Override
  public void update(float delta) {
    SphereHoopsSettings s = settings;
    int screenMin = Math.min(screenHeight, screenWidth);
    int radius = (int) (screenMin * s.globeScale());
    int length = arrayController.getLength();
    float centerZ = -(int) (screenMin / 10);
    float centerX = (float) screenWidth / 2;
    float centerY = (float) screenHeight / 2;

    rebuildGeometry(length, radius);

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
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      float w = hoopWidths[i];
      float zLocal = zOffsets[i];
      int rgb = color.getRGB();
      float prevX = 0, prevY = 0, prevZ = 0;
      for (int seg = 0; seg <= SEGMENTS; seg++) {
        float t = (float) (2 * Math.PI * seg / SEGMENTS);
        float lx = (w * 0.5f) * (float) Math.cos(t);
        float ly = (w * 0.5f) * (float) Math.sin(t);
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

    rs.begin3D();
    rs.strokeWeight(1f);
    rs.strokeLines3D(xyzxyz, lineArgb, lineCount);
    rs.end3D();
  }
}
