package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class DisparityChords extends Visualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();

  private int[] endX;
  private int[] endY;
  private int cacheLength = -1;
  private int cacheRadius = -1;
  private int cacheCenterX = -1;
  private int cacheCenterY = -1;
  private float[] xyxy;
  private int[] lineArgb;
  private float[] ellipseXywh;
  private int[] ellipseArgb;

  public DisparityChords(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Disparity Chords";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void rebuildRingPositions(int length, int radius, int centerX, int centerY) {
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
      endY[i] = centerY + (int) (radius * cos[i]);
    }
  }

  @Override
  public void update(float delta) {
    int length = arrayController.getLength();
    radius = (int) (Math.min(screenHeight, screenWidth) / 2.4);
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;

    rebuildRingPositions(length, radius, centerX, centerY);

    if (xyxy == null || xyxy.length < length * 4) {
      xyxy = new float[length * 4];
      lineArgb = new int[length];
      ellipseXywh = new float[length * 4];
      ellipseArgb = new int[length];
    }

    int lineCount = 0;
    int ellipseCount = 0;

    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));
      int rgb = color.getRGB();

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      int x = endX[i];
      int y = endY[i];
      int x2 = endX[value];
      int y2 = endY[value];

      if (x == x2 && y == y2) {
        int o = ellipseCount * 4;
        ellipseXywh[o] = x;
        ellipseXywh[o + 1] = y;
        ellipseXywh[o + 2] = 1;
        ellipseXywh[o + 3] = 1;
        ellipseArgb[ellipseCount] = rgb;
        ellipseCount++;
      } else {
        int o = lineCount * 4;
        xyxy[o] = x;
        xyxy[o + 1] = y;
        xyxy[o + 2] = x2;
        xyxy[o + 3] = y2;
        lineArgb[lineCount] = rgb;
        lineCount++;
      }
    }
    rs.strokeLines(xyxy, lineArgb, lineCount);
    rs.strokeEllipses(ellipseXywh, ellipseArgb, ellipseCount);
  }
}
