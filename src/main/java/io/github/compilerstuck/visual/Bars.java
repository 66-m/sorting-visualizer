package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class Bars extends Visualization {

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedN = -1;
  private ColorGradient cachedGradient;
  private int[] barHeights;
  private int[] barColorsArgb;
  private float[] xywh;
  private int[] argb;

  public Bars(ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Bars";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void ensureBarCache(int n, int heightScale) {
    long rev = arrayModel.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedN == n
        && cachedGradient == colorGradient) {
      return;
    }
    if (barHeights == null || barHeights.length < n) {
      barHeights = new int[n];
      barColorsArgb = new int[n];
    }
    for (int i = 0; i < n; i++) {
      int value = arrayModel.get(i);
      barHeights[i] = (value + 1) * heightScale / n;
      Color color = colorGradient.getMarkerColor(value, arrayModel.getMarker(i));
      barColorsArgb[i] = color.getRGB();
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedN = n;
    cachedGradient = colorGradient;
  }

  @Override
  public void update(float delta) {
    int n = arrayModel.getLength();
    int heightScale = screenHeight - 5;
    ensureBarCache(n, heightScale);
    if (xywh == null || xywh.length < n * 4) {
      xywh = new float[n * 4];
      argb = new int[n];
    }
    BarColumnBatch.paintBottomAligned(
        rs, sound, arrayModel, barHeights, barColorsArgb, xywh, argb, screenWidth);
  }
}
