package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class DisparityCircleScatterLinked extends Visualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();
  private final ColorBatch colorBatch = new ColorBatch();

  private float[] scaledSin;
  private float[] scaledCos;
  private double[] barHeights;
  private int cacheLength = -1;
  private int cacheRadius = -1;

  public DisparityCircleScatterLinked(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Disparity Circle Scatter Linked";
  }

  private void rebuildScaledDirections(int length, int radius) {
    if (cacheLength == length && cacheRadius == radius) {
      return;
    }
    cacheLength = length;
    cacheRadius = radius;

    if (scaledSin == null || scaledSin.length < length) {
      scaledSin = new float[length];
      scaledCos = new float[length];
    }

    phaseLut.ensure(length, 1.0);
    float[] sin = phaseLut.sin();
    float[] cos = phaseLut.cos();
    for (int i = 0; i < length; i++) {
      scaledSin[i] = radius * sin[i];
      scaledCos[i] = radius * cos[i];
    }
  }

  private static double disparityBarHeight(int i, int value, int length, int screenHeight) {
    return ((screenHeight - 10.)
            / length
            * (length
                - 2
                    * Math.min(
                        Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                        Math.abs(i + length - value))))
        / (screenHeight);
  }

  @Override
  public void update() {
    super.update();

    int length = arrayController.getLength();
    radius = (int) (Math.min(screenHeight, screenWidth) / 2.4);
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;

    rebuildScaledDirections(length, radius);

    if (barHeights == null || barHeights.length < length) {
      barHeights = new double[length];
    }

    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      barHeights[i] = disparityBarHeight(i, value, length, screenHeight);
    }

    proc.noFill();
    colorBatch.reset();

    for (int i = 0; i < length - 1; i++) {
      int value = arrayController.get(i);
      int nextIndex = i + 1;
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));
      int rgb = color.getRGB();

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      double barHeight1 = barHeights[i];
      double barHeight2 = barHeights[nextIndex];

      int x1 = centerX + (int) (barHeight1 * scaledSin[i]);
      int y1 = centerY - (int) (barHeight1 * scaledCos[i]);
      int x2 = centerX + (int) (barHeight2 * scaledSin[nextIndex]);
      int y2 = centerY - (int) (barHeight2 * scaledCos[nextIndex]);

      colorBatch.stroke(proc, rgb);
      proc.line(x1, y1, x2, y2);
    }
  }
}
