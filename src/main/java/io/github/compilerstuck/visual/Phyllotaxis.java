package io.github.compilerstuck.visual;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class Phyllotaxis extends Visualization {

  int radius;
  int c;

  private float[] angleCos;
  private float[] angleSin;
  private float[] mappedRadius;
  private int angleLength = -1;
  private int radiusLutLength = -1;
  private int radiusLutC = -1;
  private int radiusLutScreenMin = -1;
  private float[] xyd;
  private int[] argb;

  public Phyllotaxis(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Phyllotaxis";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
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
    float step = VisMath.radians(180.5f);
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
      mappedRadius[value] = VisMath.map(r, 0f, maxR, 0, mapMax);
    }
  }

  @Override
  public void update(float delta) {
    int length = arrayController.getLength();
    int screenMin = Math.min(screenHeight, screenWidth);
    radius = (int) (screenMin / 2.5);
    c = screenMin / 70;
    float centerX = screenWidth / 2f;
    float centerY = screenHeight / 2f;

    rebuildAngles(length);
    rebuildMappedRadius(length, c, screenMin);

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

      float r = mappedRadius[value];
      int o = i * 3;
      xyd[o] = centerX + r * angleCos[i];
      xyd[o + 1] = centerY + r * angleSin[i];
      xyd[o + 2] = 5;
      argb[i] = color.getRGB();
    }
    rs.fillCircles(xyd, argb, length);
  }
}
