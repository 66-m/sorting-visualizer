package io.github._66_m.visual;

import io.github._66_m.control.config.visual.SwirlDotsSettings;
import io.github._66_m.control.config.visual.VisualizationSettings;
import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.render.CoordinateSpace;
import io.github._66_m.control.render.RenderSystem;
import io.github._66_m.sound.Sound;
import io.github._66_m.visual.gradient.ColorGradient;
import java.awt.Color;

public class SwirlDots extends Visualization implements ConfigurableVisualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();

  private volatile SwirlDotsSettings settings = SwirlDotsSettings.defaults();

  private float[] xyd;
  private int[] argb;
  private double cacheTurns = Double.NaN;
  private int cacheLength = -1;

  public SwirlDots(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Swirl Dots";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof SwirlDotsSettings s) {
      settings = s;
      cacheLength = -1;
    }
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  @Override
  public void update(float delta) {
    SwirlDotsSettings s = settings;
    int length = arrayModel.getLength();
    radius = (int) (Math.min(screenHeight, screenWidth) * s.radiusScale());
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;
    double turns = s.spiralTurns();
    if (cacheLength != length || cacheTurns != turns) {
      phaseLut.ensure(length, turns);
      cacheLength = length;
      cacheTurns = turns;
    }
    float[] sin = phaseLut.sin();
    float[] cos = phaseLut.cos();
    float pointSize = (float) s.pointSize();

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

      int scale = radius * value / length;
      int o = i * 3;
      xyd[o] = centerX + (int) (scale * sin[i]);
      xyd[o + 1] = centerY + (int) (scale * cos[i]);
      xyd[o + 2] = pointSize;
      argb[i] = color.getRGB();
    }
    rs.fillCircles(xyd, argb, length);
  }
}
