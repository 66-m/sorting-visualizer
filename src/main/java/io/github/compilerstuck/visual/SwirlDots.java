package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class SwirlDots extends Visualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();
  private float[] xyd;
  private int[] argb;

  public SwirlDots(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Swirl Dots";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  @Override
  public void update(float delta) {
    int length = arrayController.getLength();
    radius = (int) (Math.min(screenHeight, screenWidth) / 2.5);
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;
    phaseLut.ensure(length, 8.0);
    float[] sin = phaseLut.sin();
    float[] cos = phaseLut.cos();

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

      int scale = radius * value / length;
      int o = i * 3;
      xyd[o] = centerX + (int) (scale * sin[i]);
      xyd[o + 1] = centerY + (int) (scale * cos[i]);
      xyd[o + 2] = 5;
      argb[i] = color.getRGB();
    }
    rs.fillCircles(xyd, argb, length);
  }
}
