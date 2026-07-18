package io.github.compilerstuck.visual;

import static java.lang.Math.floor;
import static java.lang.Math.min;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;

public class Cube extends Visualization {

  private static final float SIN_TILT = (float) Math.sin(-10);
  private static final float COS_TILT = (float) Math.cos(-10);

  int radius;
  static float aa = 0;

  private final ColorBatch colorBatch = new ColorBatch();

  private int[] colorsRgb;
  private float[] baseX, baseY, baseZ;
  private float[] xCords, yCords, zCords;
  private float[] sizes;
  private int bufferCapacity;
  private int latticeXSize = -1;
  private int latticeRadius = -1;
  private int latticeDrawCount = -1;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedDrawCount = -1;
  private int cachedLength = -1;
  private float cachedMaxBoxSize = -1;
  private ColorGradient cachedGradient;
  private int lastFillRgb;
  private boolean hasFillAlpha;

  public Cube(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "3D - Cube";
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
    sizes = new float[n];
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

  private void ensureSizesAndColors(int drawCount, int length, float maxBoxSize) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedDrawCount == drawCount
        && cachedLength == length
        && cachedMaxBoxSize == maxBoxSize
        && cachedGradient == colorGradient) {
      return;
    }
    for (int i = 0; i < drawCount; i++) {
      int value = arrayController.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));

      if (arrayController.getMarker(value) == Marker.SET) {
        sound.playSound(value);
      }

      arrayController.setMarker(value, Marker.NORMAL);

      float barHeight =
          (length
              - 2f
                  * Math.min(
                      Math.min(
                          Math.abs(i - value), Math.abs(i - length - value)),
                      Math.abs(i + length - value)));

      colorsRgb[i] = color.getRGB();
      sizes[i] = PApplet.map(barHeight, 0, length, 0, maxBoxSize);
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedDrawCount = drawCount;
    cachedLength = length;
    cachedMaxBoxSize = maxBoxSize;
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
    int length = arrayController.getLength();
    float maxBoxSize = radius * 2 / xSize;

    ensureBuffers(drawCount);
    rebuildLattice(drawCount, xSize, radius);
    ensureSizesAndColors(drawCount, length, maxBoxSize);

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
    hasFillAlpha = false;
    for (int i = 0; i < drawCount; i++) {
      int rgb = colorsRgb[i];
      colorBatch.stroke(proc, rgb);
      if (!hasFillAlpha || rgb != lastFillRgb) {
        proc.fill(rgb, 120f);
        lastFillRgb = rgb;
        hasFillAlpha = true;
      }

      proc.pushMatrix();
      proc.translate(xCords[i], yCords[i], zCords[i]);
      proc.rotateX(45);
      proc.rotateZ(-aa);
      proc.box(sizes[i], sizes[i], sizes[i]);
      proc.popMatrix();
    }
    proc.popMatrix();
  }
}
