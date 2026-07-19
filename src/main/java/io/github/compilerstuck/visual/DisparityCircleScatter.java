package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class DisparityCircleScatter extends Visualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();

  private float[] scaledSin;
  private float[] scaledCos;
  private int cacheLength = -1;
  private int cacheRadius = -1;
  private float[] xyd;
  private int[] argb;

  public DisparityCircleScatter(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Disparity Circle Scatter";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
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
  public void update(float delta) {
    int length = arrayController.getLength();
    radius = (int) (Math.min(screenHeight, screenWidth) / 2.4);
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;

    rebuildScaledDirections(length, radius);

    if (xyd == null || xyd.length < length * 3) {
      xyd = new float[length * 3];
      argb = new int[length];
    }

    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      double barHeight =
          ((screenHeight - 10.)
                  / length
                  * (length
                      - 2
                          * Math.min(
                              Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                              Math.abs(i + length - value))))
              / screenHeight;

      int o = i * 3;
      xyd[o] = centerX + (int) (barHeight * scaledSin[i]);
      xyd[o + 1] = centerY + (int) (barHeight * scaledCos[i]);
      xyd[o + 2] = 4;
      argb[i] = color.getRGB();
    }
    rs.fillCircles(xyd, argb, length);
  }
}
