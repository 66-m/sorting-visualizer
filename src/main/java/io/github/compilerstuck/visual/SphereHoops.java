package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;
import processing.core.PConstants;

public class SphereHoops extends Visualization {

  private final ColorBatch colorBatch = new ColorBatch();

  private float[] hoopWidths;
  private float[] zOffsets;
  private int cacheLength = -1;
  private int cacheRadius = -1;

  public SphereHoops(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "3D - Sphere Hoops";
  }

  private void rebuildGeometry(int length, int radius) {
    if (cacheLength == length && cacheRadius == radius) {
      return;
    }
    cacheLength = length;
    cacheRadius = radius;
    if (hoopWidths == null || hoopWidths.length < length) {
      hoopWidths = new float[length];
      zOffsets = new float[length];
    }
    for (int i = 0; i < length; i++) {
      float wi = (float) Math.sqrt(1 - Math.pow((((float) i / length) * 2 - 1), 2));
      hoopWidths[i] = (int) PApplet.map(wi, 0, 1, 0, radius);
      zOffsets[i] = radius / 2 - PApplet.map(i, 0, length, 0, radius);
    }
  }

  @Override
  public void update() {
    super.update();

    int screenMin = Math.min(screenHeight, screenWidth);
    int radius = (int) (screenMin / 1.5);
    int length = arrayController.getLength();
    float centerZ = -(int) (screenMin / 10);

    proc.lights();

    rebuildGeometry(length, radius);

    proc.noFill();
    proc.pushMatrix();
    proc.translate((float) screenWidth / 2, (float) (screenHeight / 2), centerZ);
    proc.rotateX(PConstants.PI / 3);

    colorBatch.reset();
    for (int i = 0; i < length; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      colorBatch.stroke(proc, color.getRGB());

      proc.pushMatrix();
      proc.translate(0, 0, zOffsets[i]);
      proc.circle(0, 0, hoopWidths[i]);
      proc.popMatrix();
    }

    proc.popMatrix();
  }
}
