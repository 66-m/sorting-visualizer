package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.CircleSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class Circle extends Visualization implements ConfigurableVisualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();

  private volatile CircleSettings settings = CircleSettings.defaults();

  private int[] endX;
  private int[] endY;
  private int[] frameColors;
  private int cacheLength = -1;
  private int cacheRadius = -1;
  private int cacheCenterX = -1;
  private int cacheCenterY = -1;
  private double cacheStartAngle = Double.NaN;
  private float[] xyxy;
  private int[] argb;

  public Circle(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Circle";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof CircleSettings s) {
      settings = s;
      cacheLength = -1;
    }
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void rebuildEndpoints(
      int length, int radius, int centerX, int centerY, double startAngleDeg) {
    if (cacheLength == length
        && cacheRadius == radius
        && cacheCenterX == centerX
        && cacheCenterY == centerY
        && cacheStartAngle == startAngleDeg) {
      return;
    }
    cacheLength = length;
    cacheRadius = radius;
    cacheCenterX = centerX;
    cacheCenterY = centerY;
    cacheStartAngle = startAngleDeg;

    if (endX == null || endX.length < length) {
      endX = new int[length];
      endY = new int[length];
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
      endX[i] = centerX + (int) (x * cosA - y * sinA);
      endY[i] = centerY + (int) (x * sinA + y * cosA);
    }
  }

  @Override
  public void update(float delta) {
    CircleSettings s = settings;
    int length = arrayController.getLength();
    radius = (int) (Math.min(screenHeight, screenWidth) * s.radiusScale());
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;

    rebuildEndpoints(length, radius, centerX, centerY, s.startAngleDeg());

    if (frameColors == null || frameColors.length < length) {
      frameColors = new int[length];
    }
    if (xyxy == null || xyxy.length < length * 4) {
      xyxy = new float[length * 4];
      argb = new int[length];
    }

    for (int i = 0; i < length; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));
      frameColors[i] = color.getRGB();

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      int o = i * 4;
      xyxy[o] = centerX;
      xyxy[o + 1] = centerY;
      xyxy[o + 2] = endX[i];
      xyxy[o + 3] = endY[i];
      argb[i] = frameColors[i];
    }
    rs.strokeWeight((float) s.lineThickness());
    rs.strokeLines(xyxy, argb, length);
  }
}
