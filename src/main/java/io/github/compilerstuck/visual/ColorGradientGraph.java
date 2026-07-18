package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class ColorGradientGraph extends Visualization {

  private final IndexXCache indexXCache = new IndexXCache();
  private final ColorBatch colorBatch = new ColorBatch();

  public ColorGradientGraph(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Color Gradient Graph";
  }

  @Override
  public void update() {
    super.update();

    int length = arrayController.getLength();
    int rectWidth = (screenWidth - (length - 1)) / length;
    int negHeight = -screenHeight;

    indexXCache.ensure(length, screenWidth);
    float[] xs = indexXCache.xs();

    colorBatch.reset();
    for (int i = 0; i < length; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      colorBatch.strokeAndFill(proc, color.getRGB());
      proc.rect(xs[i], screenHeight, rectWidth, negHeight);
    }
  }
}
