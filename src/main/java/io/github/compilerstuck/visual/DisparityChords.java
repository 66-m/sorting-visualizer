package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class DisparityChords extends Visualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();
  private final ColorBatch colorBatch = new ColorBatch();

  private int[] endX;
  private int[] endY;
  private int cacheLength = -1;
  private int cacheRadius = -1;
  private int cacheCenterX = -1;
  private int cacheCenterY = -1;

  public DisparityChords(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Disparity Chords";
  }

  private void rebuildRingPositions(int length, int radius, int centerX, int centerY) {
    if (cacheLength == length
        && cacheRadius == radius
        && cacheCenterX == centerX
        && cacheCenterY == centerY) {
      return;
    }
    cacheLength = length;
    cacheRadius = radius;
    cacheCenterX = centerX;
    cacheCenterY = centerY;

    if (endX == null || endX.length < length) {
      endX = new int[length];
      endY = new int[length];
    }

    phaseLut.ensure(length, 1.0);
    float[] sin = phaseLut.sin();
    float[] cos = phaseLut.cos();
    for (int i = 0; i < length; i++) {
      endX[i] = centerX + (int) (radius * sin[i]);
      endY[i] = centerY - (int) (radius * cos[i]);
    }
  }

  @Override
  public void update() {
    super.update();

    int length = arrayController.getLength();
    radius = (int) (Math.min(screenHeight, screenWidth) / 2.4);
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;

    rebuildRingPositions(length, radius, centerX, centerY);

    proc.noFill();
    colorBatch.reset();

    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));
      int rgb = color.getRGB();

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      int x = endX[i];
      int y = endY[i];
      int x2 = endX[value];
      int y2 = endY[value];

      if (x == x2 && y == y2) {
        colorBatch.strokeAndFill(proc, rgb);
        proc.ellipse(x, y, 1, 1);
      } else {
        colorBatch.stroke(proc, rgb);
        proc.line(x, y, x2, y2);
      }
    }
  }
}
