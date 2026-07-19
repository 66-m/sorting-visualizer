package io.github.compilerstuck.visual;

import static java.lang.Math.floor;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class MosaicSquares extends Visualization {

  private int cachedDrawCount = -1;
  private int cachedNextN = -1;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private float[] tileX;
  private float[] tileY;
  private float cachedTileDimX;
  private float cachedTileDimY;
  private float[] xywh;
  private int[] argb;

  public MosaicSquares(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Mosaic Squares";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void ensureTileOrigins(int drawCount, int nextN, float squareRoot) {
    if (cachedDrawCount == drawCount
        && cachedNextN == nextN
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight) {
      return;
    }
    cachedTileDimX = screenWidth / squareRoot;
    cachedTileDimY = screenHeight / squareRoot;
    if (tileX == null || tileX.length < drawCount) {
      tileX = new float[drawCount];
      tileY = new float[drawCount];
    }
    for (int i = 0; i < drawCount; i++) {
      tileX[i] = (i % squareRoot) * cachedTileDimX;
      tileY[i] = screenHeight - ((int) (i / squareRoot) + 1) * cachedTileDimY;
    }
    cachedDrawCount = drawCount;
    cachedNextN = nextN;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
  }

  @Override
  public void update(float delta) {
    int nextN = (int) floor(Math.pow(arrayController.getLength(), 1 / 2.) + 0.1);
    float squareRoot = nextN;
    int drawCount = Math.min(arrayController.getLength(), nextN * nextN);

    ensureTileOrigins(drawCount, nextN, squareRoot);

    if (xywh == null || xywh.length < drawCount * 4) {
      xywh = new float[drawCount * 4];
      argb = new int[drawCount];
    }

    for (int i = 0; i < drawCount; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      int o = i * 4;
      xywh[o] = tileX[i];
      xywh[o + 1] = tileY[i];
      xywh[o + 2] = cachedTileDimX;
      xywh[o + 3] = cachedTileDimY;
      argb[i] = color.getRGB();
    }
    rs.fillRects(xywh, argb, drawCount);
  }
}
