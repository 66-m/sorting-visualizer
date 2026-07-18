package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;

public class Hoops extends Visualization {

  int radius;

  private int[] radii;
  private final ColorBatch colorBatch = new ColorBatch();
  private int cacheLength = -1;
  private int cacheMaxRadius = -1;

  public Hoops(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Hoops";
  }

  private void rebuildRadii(int length, int maxRadius) {
    if (cacheLength == length && cacheMaxRadius == maxRadius) {
      return;
    }
    cacheLength = length;
    cacheMaxRadius = maxRadius;
    if (radii == null || radii.length < length) {
      radii = new int[length];
    }
    for (int i = 0; i < length; i++) {
      radii[i] = (int) PApplet.map(i, 0, length, 0, (float) maxRadius);
    }
  }

  @Override
  public void update() {
    super.update();

    int length = arrayController.getLength();
    int maxRadius = (int) (Math.min(screenHeight, screenWidth) / 1.1);
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;
    rebuildRadii(length, maxRadius);

    proc.noFill();
    colorBatch.reset();

    for (int i = 0; i < length; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));
      int rgb = color.getRGB();

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      radius = radii[i];
      colorBatch.stroke(proc, rgb);
      proc.ellipse(centerX, centerY, radius, radius);
    }
  }
}
