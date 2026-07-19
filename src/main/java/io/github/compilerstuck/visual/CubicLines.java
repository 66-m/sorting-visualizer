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

public class CubicLines extends Visualization {

  private static final float SIN_TILT = (float) Math.sin(-10);
  private static final float COS_TILT = (float) Math.cos(-10);

  int radius;
  private float aa = 0;

  private final InstanceData points = new InstanceData();

  private int[] colorsRgb;
  private float[] baseX, baseY, baseZ;
  private float[] xCords, yCords, zCords;
  private int bufferCapacity;
  private int latticeXSize = -1;
  private int latticeRadius = -1;
  private int latticeDrawCount = -1;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedDrawCount = -1;
  private ColorGradient cachedGradient;

  private float[] xyzxyz;
  private int[] lineArgb;

  public CubicLines(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "3D - Cubic Lines";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void ensureBuffers(int n) {
    if (colorsRgb != null && bufferCapacity >= n) return;
    bufferCapacity = n;
    colorsRgb = new int[n];
    baseX = new float[n];
    baseY = new float[n];
    baseZ = new float[n];
    xCords = new float[n];
    yCords = new float[n];
    zCords = new float[n];
    latticeXSize = -1;
    latticeRadius = -1;
    latticeDrawCount = -1;
  }

  private void rebuildLattice(int drawCount, int xSize, int radius) {
    if (latticeDrawCount == drawCount && latticeXSize == xSize && latticeRadius == radius) {
      return;
    }
    latticeDrawCount = drawCount;
    latticeXSize = xSize;
    latticeRadius = radius;

    int xCnt = 0;
    int yCnt = 0;
    int zCnt = 0;
    for (int i = 0; i < drawCount; i++) {
      baseX[i] = VisMath.map(xCnt, 0, xSize, -radius, radius);
      baseY[i] = VisMath.map(yCnt, 0, xSize, -radius, radius);
      baseZ[i] = VisMath.map(zCnt, 0, xSize, -radius, radius);

      zCnt++;
      if (zCnt == xSize) {
        if (xCnt == xSize - 1) {
          yCnt += 1;
          zCnt = 0;
          xCnt = 0;
        } else {
          xCnt += 1;
          zCnt = 0;
        }
      }
    }
  }

  private void ensureColors(int drawCount) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev && cachedDrawCount == drawCount && cachedGradient == colorGradient) {
      return;
    }
    for (int i = 0; i < drawCount; i++) {
      int value = arrayController.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));

      if (arrayController.getMarker(value) == Marker.SET) {
        sound.playSound(value);
      }

      colorsRgb[i] = color.getRGB();
    }
    cachedRevision = rev;
    cachedDrawCount = drawCount;
    cachedGradient = colorGradient;
  }

  @Override
  public void update(float delta) {
    int screenMin = min(screenHeight, screenWidth);
    radius = (int) (screenMin / 3.5);
    float centerY = (float) screenHeight / 2 - (int) (screenMin / 10);
    float centerZ = -(int) (screenMin / 10);
    float centerX = (float) screenWidth / 2;

    aa -= (float) (Math.PI / 10) * delta;
    float sinAa = (float) Math.sin(aa);
    float cosAa = (float) Math.cos(aa);

    int xSize = (int) floor(Math.pow(arrayController.getLength(), 1 / 3f) + 0.1);
    if (xSize < 1) {
      xSize = 1;
    }
    int drawCount = Math.min(arrayController.getLength(), xSize * xSize * xSize);

    ensureBuffers(drawCount);
    rebuildLattice(drawCount, xSize, radius);
    ensureColors(drawCount);

    for (int i = 0; i < drawCount; i++) {
      float xa = baseX[i];
      float ya = baseY[i];
      float za = baseZ[i];

      float zb = sinAa * xa + cosAa * za;
      float x = cosAa * xa - sinAa * za;
      float z = SIN_TILT * ya + COS_TILT * zb;
      float y = COS_TILT * ya - SIN_TILT * zb;

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
      int target = arrayController.get(i);
      if (i == target || target < 0 || target >= drawCount) {
        points.set(pointCount, xCords[i], yCords[i], zCords[i], 2, 2, 2, 0, 0, 0, colorsRgb[i]);
        pointCount++;
      } else {
        int o = lineCount * 6;
        xyzxyz[o] = xCords[i];
        xyzxyz[o + 1] = yCords[i];
        xyzxyz[o + 2] = zCords[i];
        xyzxyz[o + 3] = xCords[target];
        xyzxyz[o + 4] = yCords[target];
        xyzxyz[o + 5] = zCords[target];
        lineArgb[lineCount] = colorsRgb[i];
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
