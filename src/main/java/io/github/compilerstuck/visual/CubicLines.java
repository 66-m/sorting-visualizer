package io.github.compilerstuck.visual;

import static java.lang.Math.floor;
import static java.lang.Math.min;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;

public class CubicLines extends Visualization {

  private static final float SIN_TILT = (float) Math.sin(-10);
  private static final float COS_TILT = (float) Math.cos(-10);

  int radius;
  static float aa = 0;

  private final ColorBatch colorBatch = new ColorBatch();

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

  public CubicLines(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "3D - Cubic Lines";
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
      baseX[i] = PApplet.map(xCnt, 0, xSize, -radius, radius);
      baseY[i] = PApplet.map(yCnt, 0, xSize, -radius, radius);
      baseZ[i] = PApplet.map(zCnt, 0, xSize, -radius, radius);

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

      arrayController.setMarker(value, Marker.NORMAL);

      colorsRgb[i] = color.getRGB();
    }
    cachedRevision = rev;
    cachedDrawCount = drawCount;
    cachedGradient = colorGradient;
  }

  @Override
  public void update() {
    super.update();

    proc.lights();

    int screenMin = min(screenHeight, screenWidth);
    radius = (int) (screenMin / 3.5);
    float centerY = (float) screenHeight / 2 - (int) (screenMin / 10);
    float centerZ = -(int) (screenMin / 10);

    aa -= PApplet.PI / (10 * proc.frameRate());
    float sinAa = (float) Math.sin(aa);
    float cosAa = (float) Math.cos(aa);

    int xSize = (int) (floor(Math.pow(arrayController.getLength(), 1 / 3f) + 0.1));
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

      xCords[i] = x;
      yCords[i] = y;
      zCords[i] = z;
    }

    proc.pushMatrix();
    proc.translate((float) screenWidth / 2, centerY, centerZ);
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
