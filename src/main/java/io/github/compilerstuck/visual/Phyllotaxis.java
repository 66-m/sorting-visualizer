package io.github.compilerstuck.visual;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import io.github.compilerstuck.control.config.visual.PhyllotaxisSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class Phyllotaxis extends Visualization implements ConfigurableVisualization {

  private volatile PhyllotaxisSettings settings = PhyllotaxisSettings.defaults();

  private float[] angleCos;
  private float[] angleSin;
  private float[] mappedRadius;
  private int angleLength = -1;
  private double angleStepCached = Double.NaN;
  private int radiusLutLength = -1;
  private double radiusLutScaleDivisor = Double.NaN;
  private int radiusLutScreenMin = -1;
  private float[] xyd;
  private int[] argb;

  public Phyllotaxis(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Phyllotaxis";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof PhyllotaxisSettings s) {
      settings = s;
      angleLength = -1;
      radiusLutLength = -1;
    }
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void rebuildAngles(int length, double angleStepDeg) {
    if (angleLength == length && angleStepCached == angleStepDeg) {
      return;
    }
    angleLength = length;
    angleStepCached = angleStepDeg;
    if (angleCos == null || angleCos.length < length) {
      angleCos = new float[length];
      angleSin = new float[length];
    }
    float step = VisMath.radians((float) angleStepDeg);
    for (int i = 0; i < length; i++) {
      float a = i * step;
      angleCos[i] = (float) cos(a);
      angleSin[i] = (float) sin(a);
    }
  }

  private void rebuildMappedRadius(int length, double scaleDivisor, int screenMin) {
    if (radiusLutLength == length
        && radiusLutScaleDivisor == scaleDivisor
        && radiusLutScreenMin == screenMin) {
      return;
    }
    radiusLutLength = length;
    radiusLutScaleDivisor = scaleDivisor;
    radiusLutScreenMin = screenMin;

    if (mappedRadius == null || mappedRadius.length < length) {
      mappedRadius = new float[length];
    }

    // Higher scaleDivisor → smaller pattern. Default 70 fills screenMin/2 - 20.
    float mapMax =
        (screenMin / 2f - 20) * (float) (PhyllotaxisSettings.DEFAULT_SCALE_DIVISOR / scaleDivisor);
    float maxSqrt = (float) sqrt(length);
    for (int value = 0; value < length; value++) {
      mappedRadius[value] = VisMath.map((float) sqrt(value), 0f, maxSqrt, 0, mapMax);
    }
  }

  @Override
  public void update(float delta) {
    PhyllotaxisSettings s = settings;
    int length = arrayModel.getLength();
    int screenMin = Math.min(screenHeight, screenWidth);
    float centerX = screenWidth / 2f;
    float centerY = screenHeight / 2f;
    float pointSize = (float) s.pointSize();

    rebuildAngles(length, s.angleStepDeg());
    rebuildMappedRadius(length, s.scaleDivisor(), screenMin);

    if (xyd == null || xyd.length < length * 3) {
      xyd = new float[length * 3];
      argb = new int[length];
    }

    for (int i = 0; i < length; i++) {
      int value = arrayModel.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayModel.getMarker(i));

      if (arrayModel.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      float r = mappedRadius[value];
      int o = i * 3;
      xyd[o] = centerX + r * angleCos[i];
      xyd[o + 1] = centerY + r * angleSin[i];
      xyd[o + 2] = pointSize;
      argb[i] = color.getRGB();
    }
    rs.fillCircles(xyd, argb, length);
  }
}
