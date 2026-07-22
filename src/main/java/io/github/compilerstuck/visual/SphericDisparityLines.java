package io.github.compilerstuck.visual;

import static java.lang.Math.floor;
import static java.lang.Math.min;

import io.github.compilerstuck.control.config.visual.SphericDisparityLinesSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.InstanceData;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class SphericDisparityLines extends Visualization implements ConfigurableVisualization {

  private volatile SphericDisparityLinesSettings settings =
      SphericDisparityLinesSettings.defaults();

  int radius;
  float squareRoot;
  private float aa = 0;

  private final InstanceData points = new InstanceData();

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

  private float[] xyzxyz;
  private int[] lineArgb;

  public SphericDisparityLines(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "3D - Spheric Disparity Lines";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof SphericDisparityLinesSettings s) {
      settings = s;
    }
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
    long rev = arrayModel.getVisualRevision();
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
      int value = arrayModel.get(i);

      if (arrayModel.getMarker(value) == Marker.SET) {
        sound.playSound(value);
      }

      float barHeight =
          100000f
              / length
              * (length
                  - 2
                      * Math.min(
                          Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                          Math.abs(i + length - value)));

      pointRadii[i] = (int) VisMath.map(barHeight, 0, 100000, 0, maxRadius);

      Color color = colorGradient.getMarkerColor(value, arrayModel.getMarker(i));
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
    SphericDisparityLinesSettings s = settings;
    int nextN = (int) floor(Math.pow(arrayModel.getLength(), 1 / 2.) + 0.1);
    squareRoot = nextN;
    int drawCount = Math.min(arrayModel.getLength(), nextN * nextN);
    int length = arrayModel.getLength();
    int maxRadius = (int) (min(screenHeight, screenWidth) * s.globeScale());
    float centerZ = -(int) (min(screenHeight, screenWidth) / 10);
    float centerX = (float) screenWidth / 2;
    float centerY = (float) screenHeight / 2;

    aa -= (float) s.rotationSpeedRadPerSec() * delta;
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

      xCords[i] = toWorldX(centerX + x);
      yCords[i] = toWorldY(centerY + y);
      zCords[i] = centerZ + z;
    }

    if (xyzxyz == null || xyzxyz.length < drawCount * 6) {
      xyzxyz = new float[drawCount * 6];
      lineArgb = new int[drawCount];
    }

    points.ensureCapacity(drawCount);
    int lineCount = 0;
    int pointCount = 0;

    for (int i = 0; i < drawCount; i++) {
      int target = arrayModel.get(i);
      if (i == target || target < 0 || target >= drawCount) {
        points.set(
            pointCount,
            xCords[i],
            yCords[i],
            zCords[i],
            (float) s.markerSize(),
            (float) s.markerSize(),
            (float) s.markerSize(),
            0,
            0,
            0,
            colorsRgb[i]);
        pointCount++;
      } else {
        int o = lineCount * 6;
        xyzxyz[o] = xCords[i];
        xyzxyz[o + 1] = yCords[i];
        xyzxyz[o + 2] = zCords[i];
        xyzxyz[o + 3] = xCords[target];
        xyzxyz[o + 4] = yCords[target];
        xyzxyz[o + 5] = zCords[target];
        lineArgb[lineCount] = VisColors.withAlpha(colorsRgb[i], s.lineOpacity());
        lineCount++;
      }
    }
    points.count = pointCount;

    rs.begin3D();
    rs.strokeLines3D(xyzxyz, lineArgb, lineCount);
    rs.drawSpheres(points);
    rs.end3D();
  }
}
