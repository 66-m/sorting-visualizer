package io.github.compilerstuck.visual;

import static java.lang.Math.min;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;

public class MorphingShell extends Visualization {

  int radius;
  static float aa = 0;
  float angle = 0;

  private final ColorBatch colorBatch = new ColorBatch();

  private float[] lonSin, lonCos, latSin, latCos;
  private float[] posX, posY, posZ;
  private int[] colorsRgb;
  private int shellColSize = -1;
  private int shellLength = -1;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedColSize = -1;
  private ColorGradient cachedGradient;

  public MorphingShell(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "3D - Morphing Shell";
  }

  private void rebuildAngleBases(int length, int colSize) {
    if (shellLength == length && shellColSize == colSize) {
      return;
    }
    shellLength = length;
    shellColSize = colSize;
    if (lonSin == null || lonSin.length < length) {
      lonSin = new float[length];
      lonCos = new float[length];
      latSin = new float[length];
      latCos = new float[length];
      posX = new float[length];
      posY = new float[length];
      posZ = new float[length];
      colorsRgb = new int[length];
    }

    int rowCnt = 0;
    int colCnt = 0;
    for (int i = 0; i < length; i++) {
      float lonBase = -PApplet.PI + rowCnt * (2f * PApplet.PI) / colSize;
      float latBase = -PApplet.PI + colCnt * (2f * PApplet.PI) / colSize;
      lonSin[i] = (float) Math.sin(lonBase);
      lonCos[i] = (float) Math.cos(lonBase);
      latSin[i] = (float) Math.sin(latBase);
      latCos[i] = (float) Math.cos(latBase);

      colCnt++;
      if (colCnt == colSize) {
        rowCnt += 1;
        colCnt = 0;
      }
    }
  }

  private void ensureColors(int length, int colSize) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev && cachedColSize == colSize && cachedGradient == colorGradient) {
      return;
    }
    int rowCnt = 0;
    int colCnt = 0;
    for (int i = 0; i < length; i++) {
      int markerIndex = rowCnt + colCnt * colSize;
      Color color =
          colorGradient.getMarkerColor(
              arrayController.get(i), arrayController.getMarker(i));
      colorsRgb[i] = color.getRGB();

      if (arrayController.getMarker(markerIndex) == Marker.SET) {
        sound.playSound(markerIndex);
      }

      arrayController.setMarker(markerIndex, Marker.NORMAL);

      colCnt++;
      if (colCnt == colSize) {
        rowCnt += 1;
        colCnt = 0;
      }
    }
    cachedRevision = rev;
    cachedColSize = colSize;
    cachedGradient = colorGradient;
  }

  @Override
  public void update() {
    super.update();
    proc.lights();

    radius = Math.min(screenHeight, screenWidth) / 2;
    float centerZ = -(int) (min(screenHeight, screenWidth) / 10);

    aa += PApplet.PI / (proc.frameRate() * 10);
    float sinAa = (float) Math.sin(aa);
    float cosAa = (float) Math.cos(aa);

    int length = arrayController.getLength();
    int colSize = (int) Math.sqrt(length);

    angle += PApplet.PI / (7.5 * proc.frameRate());

    rebuildAngleBases(length, colSize);
    ensureColors(length, colSize);

    float radiusThird = radius / 3f;

    int rowCnt = 0;
    int colCnt = 0;
    for (int i = 0; i < length; i++) {
      int markerIndex = rowCnt + colCnt * colSize;
      double barHeight = arrayController.get(markerIndex);

      float sinLon = sinAa * lonCos[i] + cosAa * lonSin[i];
      float cosLon = cosAa * lonCos[i] - sinAa * lonSin[i];
      float sinLat = sinAa * latCos[i] + cosAa * latSin[i];
      float cosLat = cosAa * latCos[i] - sinAa * latSin[i];

      float z = radiusThird * sinLon * cosLat;
      float x = radius * sinLon * sinLat;
      float y = (float) (radius * cosLon + barHeight);

      posZ[i] = z;
      posX[i] = x;
      posY[i] = y;

      colCnt++;
      if (colCnt == colSize) {
        rowCnt += 1;
        colCnt = 0;
      }
    }

    proc.noStroke();
    proc.pushMatrix();
    proc.translate((float) screenWidth / 4, (float) screenHeight / 2, centerZ);
    colorBatch.reset();
    for (int i = 0; i < length; i++) {
      colorBatch.fill(proc, colorsRgb[i]);

      proc.pushMatrix();
      proc.translate(posY[i] / 2, posX[i] / 2, posZ[i]);
      proc.ellipse(0, 0, 15, 15);
      proc.popMatrix();
    }
    proc.popMatrix();
  }
}
