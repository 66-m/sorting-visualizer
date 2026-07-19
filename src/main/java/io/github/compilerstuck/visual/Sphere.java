package io.github.compilerstuck.visual;

import static java.lang.Math.floor;
import static java.lang.Math.min;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.InstanceData;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class Sphere extends Visualization {

  int radius;
  float squareRoot;
  private float aa = 0;

  private final InstanceData spheres = new InstanceData();

  private int[] colorsRgb;
  private int[] pointRadii;
  private float[] unitX, unitY, unitZ;
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

  public Sphere(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "3D - Sphere";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void ensureBuffers(int n) {
    if (colorsRgb != null && bufferCapacity >= n) return;
    bufferCapacity = n;
    colorsRgb = new int[n];
    pointRadii = new int[n];
    unitX = new float[n];
    unitY = new float[n];
    unitZ = new float[n];
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

      float barHeight =
          (((float) 100000
              / length
              * (length
                  - 2
                      * Math.min(
                          Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                          Math.abs(i + length - value)))));

      pointRadii[i] = (int) VisMath.map(barHeight, 0, 100000, 0, maxRadius);

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
  public void update(float delta) {
    int nextN = (int) (floor(Math.pow(arrayController.getLength(), 1 / 2.) + 0.1));
    squareRoot = nextN;
    int drawCount = Math.min(arrayController.getLength(), nextN * nextN);
    int length = arrayController.getLength();
    int maxRadius = (int) (min(screenHeight, screenWidth) / 2.3);
    float centerZ = -(int) (min(screenHeight, screenWidth) / 10);

    aa -= (float) (Math.PI / 10) * delta;
    float sinAa = (float) Math.sin(aa);
    float cosAa = (float) Math.cos(aa);

    ensureBuffers(drawCount);
    rebuildUnitSphere(drawCount, nextN);
    ensureColorsAndRadii(drawCount, length, maxRadius);

    spheres.ensureCapacity(drawCount);
    for (int i = 0; i < drawCount; i++) {
      int pointRadius = pointRadii[i];

      float xMapped = unitX[i] * pointRadius;
      float yMapped = unitY[i] * pointRadius;
      float zMapped = unitZ[i] * pointRadius;

      float zb = sinAa * xMapped + cosAa * zMapped;
      float x = cosAa * xMapped - sinAa * zMapped;
      float y = cosAa * yMapped - sinAa * zb;
      float z = sinAa * yMapped + cosAa * zb;

      // Legacy center (W/2,H/2,centerZ)+offset → world (x, -y, centerZ+z)
      spheres.set(i, x, -y, centerZ + z, 3, 3, 3, 0, 0, 0, colorsRgb[i]);
    }
    spheres.count = drawCount;

    rs.begin3D();
    rs.drawSpheres(spheres);
    rs.end3D();
  }
}
