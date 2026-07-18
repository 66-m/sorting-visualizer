package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class Circle extends Visualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();
  private final ColorBatch colorBatch = new ColorBatch();

  private int[] endX;
  private int[] endY;
  private int[] frameColors;
  private int cacheLength = -1;
  private int cacheRadius = -1;
  private int cacheCenterX = -1;
  private int cacheCenterY = -1;

  public Circle(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Circle";
  }

  private void rebuildEndpoints(int length, int radius, int centerX, int centerY) {
    if (cacheLength == length
        && cacheRadius == radius
        && cacheCenterX == centerX
        && cacheCenterY == centerY) {
      return;
    }
    cacheLength = length;
    cacheRadius = radius;
    cacheCenterX = centerX;
    cacheCenterY = centerY;

    if (endX == null || endX.length < length) {
      endX = new int[length];
      endY = new int[length];
    }

    phaseLut.ensure(length, 1.0);
    float[] sin = phaseLut.sin();
    float[] cos = phaseLut.cos();
    for (int i = 0; i < length; i++) {
      endX[i] = centerX + (int) (radius * sin[i]);
      endY[i] = centerY - (int) (radius * cos[i]);
    }
  }

  @Override
  public void update() {
    super.update();

    int length = arrayController.getLength();
    radius = (int) (Math.min(screenHeight, screenWidth) / 2.4);
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;

    rebuildEndpoints(length, radius, centerX, centerY);

    if (frameColors == null || frameColors.length < length) {
      frameColors = new int[length];
    }

    for (int i = 0; i < length; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));
      frameColors[i] = color.getRGB();

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);
    }

    proc.noFill();
    colorBatch.reset();

    for (int i = 0; i < length; i++) {
      colorBatch.stroke(proc, frameColors[i]);
      proc.line(centerX, centerY, endX[i], endY[i]);
    }
  }
}
