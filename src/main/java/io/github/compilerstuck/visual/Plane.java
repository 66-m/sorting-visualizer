package io.github.compilerstuck.visual;

import static java.lang.Math.floor;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;
import processing.core.PConstants;

public class Plane extends Visualization {

  float angle = 0;
  float squareRoot;

  private final ColorBatch colorBatch = new ColorBatch();

  private float[] tileX, tileY;
  private int[] colorsRgb;
  private int tileDrawCount = -1;
  private int tileGridSize = -1;
  private int tileRadius = -1;
  private float cachedTileDim;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedDrawCount = -1;
  private ColorGradient cachedGradient;

  public Plane(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "3D - Plane";
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
    }
    float sq = gridSize;
    for (int i = 0; i < drawCount; i++) {
      tileX[i] = -radius / 2 + (int) floor(i / sq) * tileDim;
      tileY[i] = -radius / 2 + i % sq * tileDim;
    }
  }

  private void ensureColors(int drawCount) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev && cachedDrawCount == drawCount && cachedGradient == colorGradient) {
      return;
    }
    for (int i = 0; i < drawCount; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));
      colorsRgb[i] = color.getRGB();

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);
    }
    cachedRevision = rev;
    cachedDrawCount = drawCount;
    cachedGradient = colorGradient;
  }

  @Override
  public void update() {
    super.update();

    int screenMin = Math.min(screenHeight, screenWidth);
    int radius = (int) (screenMin / 1.2);
    float centerY = (float) (screenHeight / 2.5);
    float centerZ = -(int) (screenMin / 10);

    angle += PApplet.PI / (15 * proc.frameRate());
    proc.lights();

    int nextN = (int) (floor(Math.pow(arrayController.getLength(), 1 / 2.) + 0.1));
    squareRoot = nextN;
    int drawCount = Math.min(arrayController.getLength(), nextN * nextN);
    float tileDim = radius / squareRoot;

    rebuildTiles(drawCount, nextN, radius, tileDim);
    ensureColors(drawCount);
    float halfTile = tileDim / 2f;

    proc.pushMatrix();
    proc.translate((float) screenWidth / 2, centerY, centerZ);
    proc.rotateX(PConstants.PI / 3);
    proc.rotateZ(angle);

    colorBatch.reset();
    for (int i = 0; i < drawCount; i++) {
      colorBatch.strokeAndFill(proc, colorsRgb[i]);

      proc.pushMatrix();
      proc.translate(tileX[i], tileY[i], 0);
      proc.rect(-halfTile, -halfTile, tileDim, tileDim);
      proc.popMatrix();
    }

    proc.popMatrix();
  }
}
