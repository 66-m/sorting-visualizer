package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;
import processing.core.PConstants;

public class DisparitySphereHoops extends Visualization {

  private final ColorBatch colorBatch = new ColorBatch();

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

  public DisparitySphereHoops(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "3D - Disparity Sphere Hoops";
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
      wiBase[i] = (float) Math.sqrt(1 - Math.pow((((float) i / length) * 2 - 1), 2));
      zOffsets[i] = radius / 2 - PApplet.map(i, 0, length, 0, radius);
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
              ((1f
                  / length
                  * (length
                      - 2
                          * Math.min(
                              Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                              Math.abs(i + length - value)))));
      float wi = wiBase[i] * barHeight;
      sphereWi[i] = (int) PApplet.map(wi, 0, 1, 0, radius);

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

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
  public void update() {
    super.update();

    int screenMin = Math.min(screenHeight, screenWidth);
    int radius = (int) (screenMin / 1.1);
    int length = arrayController.getLength();
    float centerZ = -(int) (screenMin / 10);

    proc.lights();

    rebuildGeometry(length, radius);
    ensureSphereWiAndColors(length, radius);

    proc.noFill();
    proc.pushMatrix();
    proc.translate((float) screenWidth / 2, (float) (screenHeight / 2), centerZ);
    proc.rotateX(PConstants.PI / 3);

    colorBatch.reset();
    for (int i = 0; i < length; i++) {
      colorBatch.stroke(proc, colorsRgb[i]);

      proc.pushMatrix();
      proc.translate(0, 0, zOffsets[i]);
      proc.circle(0, 0, sphereWi[i]);
      proc.popMatrix();
    }

    proc.popMatrix();
  }
}
