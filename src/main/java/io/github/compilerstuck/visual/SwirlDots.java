package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class SwirlDots extends Visualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();
  private final ColorBatch colorBatch = new ColorBatch();

  public SwirlDots(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Swirl Dots";
  }

  @Override
  public void update() {
    super.update();

    int length = arrayController.getLength();
    radius = (int) (Math.min(screenHeight, screenWidth) / 2.5);
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;
    // Original: phase = 16 * PI * i / length == 8 turns of 2π
    phaseLut.ensure(length, 8.0);
    float[] sin = phaseLut.sin();
    float[] cos = phaseLut.cos();

    colorBatch.reset();
    proc.noStroke();

    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));
      int rgb = color.getRGB();

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      // Keep integer division order from original: (radius * value / length) * sin
      int scale = radius * value / length;
      int x = centerX + (int) (scale * sin[i]);
      int y = centerY + (int) (scale * cos[i]);

      colorBatch.fill(proc, rgb);
      proc.circle(x, y, 5);
    }
  }
}
