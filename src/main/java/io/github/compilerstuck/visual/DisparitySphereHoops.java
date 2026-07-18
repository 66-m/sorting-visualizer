package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;
import processing.core.PConstants;

public class DisparitySphereHoops extends Visualization {

  float angle = 0;

  public DisparitySphereHoops(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "3D - Disparity Sphere Hoops";
  }

  @Override
  public void update() {
    super.update();

    // int rectWidth = (screenWidth - (arrayController.getLength() - 1)) /
    // arrayController.getLength();
    int radius = (int) (Math.min(screenHeight, screenWidth) / 1.1);

    angle -= PApplet.PI / (15 * proc.frameRate());
    proc.lights();

    for (int i = 0; i < arrayController.getLength(); i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      float barHeight =
          -(float)
              ((1f
                  / arrayController.getLength()
                  * (arrayController.getLength()
                      - 2
                          * Math.min(
                              Math.min(
                                  Math.abs(i - arrayController.get(i)),
                                  Math.abs(
                                      i - arrayController.getLength() - arrayController.get(i))),
                              Math.abs(
                                  i + arrayController.getLength() - arrayController.get(i))))));
      float wi =
          (float) Math.sqrt(1 - Math.pow((((float) i / arrayController.getLength()) * 2 - 1), 2))
              * barHeight;

      int sphere_wi = (int) PApplet.map(wi, 0, 1, 0, radius);

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      proc.stroke(color.getRGB());
      proc.fill(color.getRGB());
      proc.noFill();

      // proc.rect(PApplet.map(i, 0, arrayController.getLength(), 0, screenWidth), screenHeight,
      // rectWidth, -1 * barHeight); //Classic bar

      proc.pushMatrix();

      proc.translate(
          (float) screenWidth / 2,
          (float) (screenHeight / 2),
          -(int) (Math.min(screenHeight, screenWidth) / 10));

      proc.rotateX(PConstants.PI / 3);
      // proc.rotateY(angle);

      proc.translate(0, 0, radius / 2 - PApplet.map(i, 0, arrayController.getLength(), 0, radius));

      proc.circle(0, 0, sphere_wi);

      proc.popMatrix();
    }
  }
}
