package io.github.compilerstuck.visual;

import static java.lang.Math.floor;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class MosaicSquares extends Visualization {

  private final ColorBatch colorBatch = new ColorBatch();

  private int cachedDrawCount = -1;
  private int cachedNextN = -1;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private float[] tileX;
  private float[] tileY;
  private float cachedTileDimX;
  private float cachedTileDimY;

  public MosaicSquares(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Mosaic Squares";
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
      tileY[i] = (int) (i / squareRoot) * cachedTileDimY;
    }
    cachedDrawCount = drawCount;
    cachedNextN = nextN;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
  }

  @Override
  public void update() {
    super.update();

    int nextN = (int) (floor(Math.pow(arrayController.getLength(), 1 / 2.) + 0.1));
    float squareRoot = nextN;
    int drawCount = Math.min(arrayController.getLength(), nextN * nextN);

    ensureTileOrigins(drawCount, nextN, squareRoot);

    colorBatch.reset();
    for (int i = 0; i < drawCount; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      colorBatch.strokeAndFill(proc, color.getRGB());
      proc.rect(tileX[i], tileY[i], cachedTileDimX, cachedTileDimY);
    }
  }
}
