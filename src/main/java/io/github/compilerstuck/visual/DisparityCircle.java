package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class DisparityCircle extends Visualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();
  private final ColorBatch colorBatch = new ColorBatch();

  private float[] scaledSin;
  private float[] scaledCos;
  private int cacheLength = -1;
  private int cacheRadius = -1;

  public DisparityCircle(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Disparity Circle";
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

  @Override
  public void update() {
    super.update();

    int length = arrayController.getLength();
    radius = (int) (Math.min(screenHeight, screenWidth) / 2.4);
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;

    rebuildScaledDirections(length, radius);

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

      double barHeight =
          ((screenHeight - 0.1)
                  / length
                  * (length
                      - 2
                          * Math.min(
                              Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                              Math.abs(i + length - value))))
              / (screenHeight);

      int x = centerX + (int) (barHeight * scaledSin[i]);
      int y = centerY - (int) (barHeight * scaledCos[i]);

      colorBatch.stroke(proc, rgb);
      proc.line(centerX, centerY, x, y);
    }
  }
}
