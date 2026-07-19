package io.github.compilerstuck.visual;

import static java.lang.Math.floor;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.InstanceData;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class DisparityPlane extends Visualization {

  private float angle = 0;
  float squareRoot;

  private final InstanceData quads = new InstanceData();

  private float[] tileX, tileY;
  private int[] colorsRgb;
  private int[] barHeights;
  private int tileDrawCount = -1;
  private int tileGridSize = -1;
  private int tileRadius = -1;
  private float cachedTileDim;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedDrawCount = -1;
  private int cachedLength = -1;
  private int cachedQuarterHeight = -1;
  private ColorGradient cachedGradient;

  public DisparityPlane(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "3D - Disparity Plane";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void rebuildTiles(int drawCount, int gridSize, int radius, float tileDim) {
    if (tileDrawCount == drawCount
        && tileGridSize == gridSize
        && tileRadius == radius
        && cachedTileDim == tileDim) {
      return;
    }
    tileDrawCount = drawCount;
    tileGridSize = gridSize;
    tileRadius = radius;
    cachedTileDim = tileDim;
    if (tileX == null || tileX.length < drawCount) {
      tileX = new float[drawCount];
      tileY = new float[drawCount];
      colorsRgb = new int[drawCount];
      barHeights = new int[drawCount];
    }
    float sq = gridSize;
    for (int i = 0; i < drawCount; i++) {
      tileX[i] = -radius / 2f + (int) floor(i / sq) * tileDim;
      tileY[i] = -radius / 2f + i % sq * tileDim;
    }
  }

  private void ensureBarHeightsAndColors(int drawCount, int length, int quarterHeight) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedDrawCount == drawCount
        && cachedLength == length
        && cachedQuarterHeight == quarterHeight
        && cachedGradient == colorGradient) {
      return;
    }
    for (int i = 0; i < drawCount; i++) {
      int value = arrayController.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));

      barHeights[i] =
          quarterHeight
              - (int)
                  ((quarterHeight - 10.)
                      / length
                      * (length
                          - 2
                              * Math.min(
                                  Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                                  Math.abs(i + length - value))));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      colorsRgb[i] = color.getRGB();
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedDrawCount = drawCount;
    cachedLength = length;
    cachedQuarterHeight = quarterHeight;
    cachedGradient = colorGradient;
  }

  @Override
  public void update(float delta) {
    int screenMin = Math.min(screenHeight, screenWidth);
    int radius = (int) (screenMin / 1.2);
    int length = arrayController.getLength();
    float centerY = (float) (screenHeight / 2.5);
    float centerZ = -(int) (screenMin / 10);
    float centerX = (float) screenWidth / 2;
    int quarterHeight = screenHeight / 4;

    angle += (float) (Math.PI / 15) * delta;

    int nextN = (int) floor(Math.pow(length, 1 / 2.) + 0.1);
    squareRoot = nextN;
    int drawCount = Math.min(length, nextN * nextN);
    float tileDim = radius / squareRoot;

    rebuildTiles(drawCount, nextN, radius, tileDim);
    ensureBarHeightsAndColors(drawCount, length, quarterHeight);

    float rotX = VisMath.PI / 3;
    float cosX = (float) Math.cos(rotX);
    float sinX = (float) Math.sin(rotX);
    float cosZ = (float) Math.cos(angle);
    float sinZ = (float) Math.sin(angle);

    quads.ensureCapacity(drawCount);
    for (int i = 0; i < drawCount; i++) {
      float tx = tileX[i];
      float ty = tileY[i];
      float tz = barHeights[i];

      float x1 = cosZ * tx - sinZ * ty;
      float y1 = sinZ * tx + cosZ * ty;
      float z1 = tz;
      float y2 = cosX * y1 - sinX * z1;
      float z2 = sinX * y1 + cosX * z1;

      quads.set(
          i,
          toWorldX(centerX + x1),
          toWorldY(centerY + y2),
          centerZ + z2,
          tileDim,
          tileDim,
          1f,
          -rotX,
          0f,
          -angle,
          colorsRgb[i]);
    }
    quads.count = drawCount;

    rs.begin3D();
    rs.drawQuads(quads);
    rs.end3D();
  }
}
