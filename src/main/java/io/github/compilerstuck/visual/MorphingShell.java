package io.github.compilerstuck.visual;

import static java.lang.Math.min;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.InstanceData;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class MorphingShell extends Visualization {

  int radius;
  private float aa = 0;

  private final InstanceData spheres = new InstanceData();

  private float[] lonSin, lonCos, latSin, latCos;
  private int[] colorsRgb;
  private int shellColSize = -1;
  private int shellLength = -1;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedColSize = -1;
  private ColorGradient cachedGradient;

  public MorphingShell(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "3D - Morphing Shell";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
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
      colorsRgb = new int[length];
    }

    int rowCnt = 0;
    int colCnt = 0;
    for (int i = 0; i < length; i++) {
      float lonBase = -VisMath.PI + rowCnt * (2f * VisMath.PI) / colSize;
      float latBase = -VisMath.PI + colCnt * (2f * VisMath.PI) / colSize;
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
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));
      colorsRgb[i] = color.getRGB();

      if (arrayController.getMarker(markerIndex) == Marker.SET) {
        sound.playSound(markerIndex);
      }

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
  public void update(float delta) {
    radius = Math.min(screenHeight, screenWidth) / 2;
    float centerZ = -(int) (min(screenHeight, screenWidth) / 10);
    float halfW = screenWidth * 0.5f;

    aa += (float) (Math.PI / 10) * delta;
    float sinAa = (float) Math.sin(aa);
    float cosAa = (float) Math.cos(aa);

    int length = arrayController.getLength();
    int colSize = (int) Math.sqrt(length);

    rebuildAngleBases(length, colSize);
    ensureColors(length, colSize);

    float radiusThird = radius / 3f;

    spheres.ensureCapacity(length);
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

      // Legacy Processing pos (W/4 + y/2, H/2 + x/2, centerZ+z) → world
      float wx = y / 2f - halfW * 0.5f;
      float wy = -x / 2f;
      spheres.set(i, wx, wy, centerZ + z, 15, 15, 15, 0, 0, 0, colorsRgb[i]);

      colCnt++;
      if (colCnt == colSize) {
        rowCnt += 1;
        colCnt = 0;
      }
    }
    spheres.count = length;

    rs.begin3D();
    rs.drawSpheres(spheres);
    rs.end3D();
  }
}
