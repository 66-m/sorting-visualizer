package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;
import processing.core.PConstants;

public class Pyramid extends Visualization {

  float angle = 0;

  private final ColorBatch colorBatch = new ColorBatch();

  private float[] zOffsets;
  private int[] barHeights;
  private int[] colorsRgb;
  private int zOffsetLength = -1;
  private int zOffsetRadius = -1;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedLength = -1;
  private int cachedRadius = -1;
  private ColorGradient cachedGradient;

  public Pyramid(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "3D - Pyramid";
  }

  private void rebuildZOffsets(int length, int radius) {
    if (zOffsetLength == length && zOffsetRadius == radius) {
      return;
    }
    zOffsetLength = length;
    zOffsetRadius = radius;
    if (zOffsets == null || zOffsets.length < length) {
      zOffsets = new float[length];
    }
    for (int i = 0; i < length; i++) {
      zOffsets[i] = radius / 2 - PApplet.map(i, 0, length, 0, radius);
    }
  }

  private void ensureBarHeightsAndColors(int length, int radius) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedLength == length
        && cachedRadius == radius
        && cachedGradient == colorGradient) {
      return;
    }
    if (barHeights == null || barHeights.length < length) {
      barHeights = new int[length];
      colorsRgb = new int[length];
    }
    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));

      barHeights[i] = (value + 1) * (radius - 5) / length;

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
    int radius = (int) (screenMin / 1.7);
    int length = arrayController.getLength();
    float centerY = (float) (screenHeight / 2.5);
    float centerZ = -(int) (screenMin / 10);

    angle -= PApplet.PI / (15 * proc.frameRate());
    proc.lights();

    rebuildZOffsets(length, radius);
    ensureBarHeightsAndColors(length, radius);

    proc.pushMatrix();
    proc.translate((float) screenWidth / 2, centerY, centerZ);
    proc.rotateX(PConstants.PI / 3);
    proc.rotateZ(angle);

    colorBatch.reset();
    for (int i = 0; i < length; i++) {
      int barHeight = barHeights[i];
      colorBatch.strokeAndFill(proc, colorsRgb[i]);

      proc.pushMatrix();
      proc.translate(0, 0, zOffsets[i]);
      proc.rect(-barHeight / 2, -barHeight / 2, barHeight, barHeight);
      proc.popMatrix();
    }

    proc.popMatrix();
  }
}
