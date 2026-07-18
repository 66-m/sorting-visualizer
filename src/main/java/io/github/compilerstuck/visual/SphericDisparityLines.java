package io.github.compilerstuck.visual;

import static java.lang.Math.floor;
import static java.lang.Math.min;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;

public class SphericDisparityLines extends Visualization {

  int radius;
  float squareRoot;
  static float aa = 0;

  private final ColorBatch colorBatch = new ColorBatch();

  private int[] colorsRgb;
  private int[] pointRadii;
  private float[] unitX, unitY, unitZ;
  private float[] xCords, yCords, zCords;
  private int bufferCapacity;
  private int unitGridSize = -1;
  private int unitDrawCount = -1;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedDrawCount = -1;
  private int cachedLength = -1;
  private int cachedMaxRadius = -1;
  private ColorGradient cachedGradient;

  public SphericDisparityLines(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "3D - Spheric Disparity Lines";
  }

  private void ensureBuffers(int n) {
    if (colorsRgb != null && bufferCapacity >= n) return;
    bufferCapacity = n;
    colorsRgb = new int[n];
    pointRadii = new int[n];
    unitX = new float[n];
    unitY = new float[n];
    unitZ = new float[n];
    xCords = new float[n];
    yCords = new float[n];
    zCords = new float[n];
    unitGridSize = -1;
    unitDrawCount = -1;
  }

  private void rebuildUnitSphere(int drawCount, int gridSize) {
    if (unitDrawCount == drawCount && unitGridSize == gridSize) {
      return;
    }
    unitDrawCount = drawCount;
    unitGridSize = gridSize;

    float m = 0;
    float n = 0;
    float sq = gridSize;
    for (int i = 0; i < drawCount; i++) {
      float u = (m / sq) * 2f * (float) Math.PI;
      float v = (n / sq) * (float) Math.PI;

      unitZ[i] = (float) (Math.cos(u) * Math.sin(v));
      unitX[i] = (float) (Math.sin(u) * Math.sin(v));
      unitY[i] = (float) Math.cos(v);

      if (m == sq - 1) {
        if (n == sq - 1) {
          n = 0;
        } else {
          n++;
        }
        m = 0;
      } else {
        m++;
      }
    }
  }

  private void ensureColorsAndRadii(int drawCount, int length, int maxRadius) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedDrawCount == drawCount
        && cachedLength == length
        && cachedMaxRadius == maxRadius
        && cachedGradient == colorGradient) {
      return;
    }
    for (int i = 0; i < drawCount; i++) {
      int value = arrayController.get(i);

      if (arrayController.getMarker(value) == Marker.SET) {
        sound.playSound(value);
      }

      arrayController.setMarker(value, Marker.NORMAL);

      float barHeight =
          (((float) 100000
              / length
              * (length
                  - 2
                      * Math.min(
                          Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                          Math.abs(i + length - value)))));

      pointRadii[i] = (int) PApplet.map(barHeight, 0, 100000, 0, maxRadius);

      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));
      colorsRgb[i] = color.getRGB();
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedDrawCount = drawCount;
    cachedLength = length;
    cachedMaxRadius = maxRadius;
    cachedGradient = colorGradient;
  }

  @Override
  public void update() {
    super.update();

    proc.lights();

    int nextN = (int) (floor(Math.pow(arrayController.getLength(), 1 / 2.) + 0.1));
    squareRoot = nextN;
    int drawCount = Math.min(arrayController.getLength(), nextN * nextN);
    int length = arrayController.getLength();
    int maxRadius = (int) (min(screenHeight, screenWidth) / 2.3);
    float centerZ = -(int) (min(screenHeight, screenWidth) / 10);

    aa -= PApplet.PI / (10 * proc.frameRate());
    float sinAa = (float) Math.sin(aa);
    float cosAa = (float) Math.cos(aa);

    ensureBuffers(drawCount);
    rebuildUnitSphere(drawCount, nextN);
    ensureColorsAndRadii(drawCount, length, maxRadius);

    for (int i = 0; i < drawCount; i++) {
      int pointRadius = pointRadii[i];

      float xMapped = unitX[i] * pointRadius;
      float yMapped = unitY[i] * pointRadius;
      float zMapped = unitZ[i] * pointRadius;

      float zb = sinAa * xMapped + cosAa * zMapped;
      float x = cosAa * xMapped - sinAa * zMapped;
      float y = cosAa * yMapped - sinAa * zb;
      float z = sinAa * yMapped + cosAa * zb;

      zCords[i] = z;
      xCords[i] = x;
      yCords[i] = y;
    }

    proc.pushMatrix();
    proc.translate((float) screenWidth / 2, (float) screenHeight / 2, centerZ);
    colorBatch.reset();
    for (int i = 0; i < drawCount; i++) {
      colorBatch.strokeAndFill(proc, colorsRgb[i]);

      int target = arrayController.get(i);
      if (i == target || target < 0 || target >= drawCount) {
        proc.pushMatrix();
        proc.translate(xCords[i], yCords[i], zCords[i]);
        proc.circle(0, 0, 2);
        proc.popMatrix();
      } else {
        proc.line(xCords[i], yCords[i], zCords[i], xCords[target], yCords[target], zCords[target]);
      }
    }
    proc.popMatrix();
  }
}
