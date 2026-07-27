package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.DisparityCircleScatterLinkedSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class DisparityCircleScatterLinked extends Visualization
    implements ConfigurableVisualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();

  private volatile DisparityCircleScatterLinkedSettings settings =
      DisparityCircleScatterLinkedSettings.defaults();

  private float[] scaledSin;
  private float[] scaledCos;
  private double[] barHeights;
  private int cacheLength = -1;
  private int cacheRadius = -1;
  private float[] xyxy;
  private int[] argb;

  public DisparityCircleScatterLinked(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Disparity Circle Scatter Linked";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof DisparityCircleScatterLinkedSettings s) {
      settings = s;
      cacheLength = -1;
    }
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void rebuildScaledDirections(int length, int radius) {
    if (cacheLength == length && cacheRadius == radius) {
      return;
    }
    cacheLength = length;
    cacheRadius = radius;

    if (scaledSin == null || scaledSin.length < length) {
      scaledSin = new float[length];
      scaledCos = new float[length];
    }

    phaseLut.ensure(length, 1.0);
    float[] sin = phaseLut.sin();
    float[] cos = phaseLut.cos();
    for (int i = 0; i < length; i++) {
      scaledSin[i] = radius * sin[i];
      scaledCos[i] = radius * cos[i];
    }
  }

  private static double disparityBarHeight(int i, int value, int length, int screenHeight) {
    return ((screenHeight - 10.)
            / length
            * (length
                - 2
                    * Math.min(
                        Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                        Math.abs(i + length - value))))
        / screenHeight;
  }

  @Override
  public void update(float delta) {
    DisparityCircleScatterLinkedSettings s = settings;
    int length = arrayModel.getLength();
    radius = (int) (Math.min(screenHeight, screenWidth) * s.radiusScale());
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;

    rebuildScaledDirections(length, radius);

    if (barHeights == null || barHeights.length < length) {
      barHeights = new double[length];
    }

    for (int i = 0; i < length; i++) {
      int value = arrayModel.get(i);
      barHeights[i] = disparityBarHeight(i, value, length, screenHeight);
    }

    int lineCount = Math.max(0, length - 1);
    if (xyxy == null || xyxy.length < lineCount * 4) {
      xyxy = new float[Math.max(4, lineCount * 4)];
      argb = new int[Math.max(1, lineCount)];
    }

    for (int i = 0; i < lineCount; i++) {
      int value = arrayModel.get(i);
      int nextIndex = i + 1;
      Color color = colorGradient.getMarkerColor(value, arrayModel.getMarker(i));

      if (arrayModel.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      double barHeight1 = barHeights[i];
      double barHeight2 = barHeights[nextIndex];

      int o = i * 4;
      xyxy[o] = centerX + (int) (barHeight1 * scaledSin[i]);
      xyxy[o + 1] = centerY + (int) (barHeight1 * scaledCos[i]);
      xyxy[o + 2] = centerX + (int) (barHeight2 * scaledSin[nextIndex]);
      xyxy[o + 3] = centerY + (int) (barHeight2 * scaledCos[nextIndex]);
      argb[i] = color.getRGB();
    }
    rs.strokeWeight((float) s.lineThickness());
    rs.strokeLines(xyxy, argb, lineCount);
  }
}
