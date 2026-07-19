package io.github.compilerstuck.visual;

import static java.lang.Math.*;
import static processing.core.PApplet.radians;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;

public class Phyllotaxis extends Visualization {

  int radius;
  int c;

  private float[] angleCos;
  private float[] angleSin;
  private float[] mappedRadius;
  private final ColorBatch colorBatch = new ColorBatch();
  private int angleLength = -1;
  private int radiusLutLength = -1;
  private int radiusLutC = -1;
  private int radiusLutScreenMin = -1;

  public Phyllotaxis(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Phyllotaxis";
  }

  private void rebuildAngles(int length) {
    if (angleLength == length) {
      return;
    }
    angleLength = length;
    if (angleCos == null || angleCos.length < length) {
      angleCos = new float[length];
      angleSin = new float[length];
    }
    float step = radians(180.5f);
    for (int i = 0; i < length; i++) {
      float a = i * step;
      angleCos[i] = (float) cos(a);
      angleSin[i] = (float) sin(a);
    }
  }

  private void rebuildMappedRadius(int length, int c, int screenMin) {
    if (radiusLutLength == length && radiusLutC == c && radiusLutScreenMin == screenMin) {
      return;
    }
    radiusLutLength = length;
    radiusLutC = c;
    radiusLutScreenMin = screenMin;

    if (mappedRadius == null || mappedRadius.length < length) {
      mappedRadius = new float[length];
    }

    float maxR = c * (float) sqrt(length);
    float mapMax = screenMin / 2f - 20;
    for (int value = 0; value < length; value++) {
      float r = (float) (c * sqrt(value));
      mappedRadius[value] = PApplet.map(r, 0f, maxR, 0, mapMax);
    }
  }

  @Override
  public void update() {
    super.update();

    int length = arrayController.getLength();
    int screenMin = Math.min(screenHeight, screenWidth);
    radius = (int) (screenMin / 2.5);
    c = screenMin / 70;
    float centerX = screenWidth / 2f;
    float centerY = screenHeight / 2f;

    rebuildAngles(length);
    rebuildMappedRadius(length, c, screenMin);

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

      float r = mappedRadius[value];
      float x = r * angleCos[i];
      float y = r * angleSin[i];

      colorBatch.fill(proc, rgb);
      proc.circle(centerX + x, centerY + y, 5);
    }
  }
}
