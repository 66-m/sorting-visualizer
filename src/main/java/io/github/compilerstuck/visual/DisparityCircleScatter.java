package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.DisparityCircleScatterSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class DisparityCircleScatter extends Visualization implements ConfigurableVisualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();

  private volatile DisparityCircleScatterSettings settings =
      DisparityCircleScatterSettings.defaults();

  private float[] scaledSin;
  private float[] scaledCos;
  private int cacheLength = -1;
  private int cacheRadius = -1;
  private double cacheStartAngle = Double.NaN;
  private float[] xyd;
  private int[] argb;

  public DisparityCircleScatter(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Disparity Circle Scatter";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof DisparityCircleScatterSettings s) {
      settings = s;
      cacheLength = -1;
    }
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void rebuildScaledDirections(int length, int radius, double startAngleDeg) {
    if (cacheLength == length && cacheRadius == radius && cacheStartAngle == startAngleDeg) {
      return;
    }
    cacheLength = length;
    cacheRadius = radius;
    cacheStartAngle = startAngleDeg;

    if (scaledSin == null || scaledSin.length < length) {
      scaledSin = new float[length];
      scaledCos = new float[length];
    }

    phaseLut.ensure(length, 1.0);
    float[] sin = phaseLut.sin();
    float[] cos = phaseLut.cos();
    double rad = Math.toRadians(startAngleDeg);
    float cosA = (float) Math.cos(rad);
    float sinA = (float) Math.sin(rad);
    for (int i = 0; i < length; i++) {
      float x = radius * sin[i];
      float y = radius * cos[i];
      scaledSin[i] = x * cosA - y * sinA;
      scaledCos[i] = x * sinA + y * cosA;
    }
  }

  @Override
  public void update(float delta) {
    DisparityCircleScatterSettings s = settings;
    float pointSize = (float) s.pointSize();
    int length = arrayModel.getLength();
    radius = (int) (Math.min(screenHeight, screenWidth) * s.radiusScale());
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;

    rebuildScaledDirections(length, radius, s.startAngleDeg());

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

      double barHeight =
          ((screenHeight - 10.)
                  / length
                  * (length
                      - 2
                          * Math.min(
                              Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                              Math.abs(i + length - value))))
              / screenHeight;

      int o = i * 3;
      xyd[o] = centerX + (int) (barHeight * scaledSin[i]);
      xyd[o + 1] = centerY + (int) (barHeight * scaledCos[i]);
      xyd[o + 2] = pointSize;
      argb[i] = color.getRGB();
    }
    rs.fillCircles(xyd, argb, length);
  }
}
