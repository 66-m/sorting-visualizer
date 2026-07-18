package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;

public class Hoops extends Visualization {

  int radius;

  public Hoops(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Hoops";
  }

  @Override
  public void update() {
    super.update();
    for (int i = 0; i < arrayController.getLength(); i++) {

      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      proc.stroke(color.getRGB());

      proc.noFill();

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      radius =
          (int)
              PApplet.map(
                  i,
                  0,
                  arrayController.getLength(),
                  0,
                  (float) (Math.min(screenHeight, screenWidth) / 1.1));

      proc.ellipse(screenWidth / 2, screenHeight / 2, radius, radius);
    }
  }
}
