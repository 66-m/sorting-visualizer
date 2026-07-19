package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class ColorGradientGraph extends Visualization {

  private static final float SEAM_OVERLAP = 1f;

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
    float slotWidth = (float) screenWidth / length;

    proc.noStroke();
    colorBatch.reset();
    proc.beginShape(RenderContext.QUADS);

    for (int i = 0; i < length; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }
      arrayController.setMarker(i, Marker.NORMAL);

      colorBatch.fill(proc, color.getRGB());

      float x0 = i * slotWidth;
      float x1 = Math.min(screenWidth, (i + 1) * slotWidth + SEAM_OVERLAP);
      float y0 = screenHeight;
      float y1 = 0;
      proc.vertex(x0, y0);
      proc.vertex(x1, y0);
      proc.vertex(x1, y1);
      proc.vertex(x0, y1);
    }

    proc.endShape();
  }
}
