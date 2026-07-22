package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class DisparityGraphMirrored extends Visualization {

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedLength = -1;
  private double cachedHeightFactor = -1;
  private ColorGradient cachedGradient;
  private int[] barHeights;
  private int[] barColorsRgb;
  private float[] xywh;
  private int[] argb;

  public DisparityGraphMirrored(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Disparity Graph Mirrored";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void ensureBarCache(int length, double heightFactor) {
    long rev = arrayModel.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedLength == length
        && cachedHeightFactor == heightFactor
        && cachedGradient == colorGradient) {
      return;
    }
    if (barHeights == null || barHeights.length < length) {
      barHeights = new int[length];
      barColorsRgb = new int[length];
    }
    for (int i = 0; i < length; i++) {
      int value = arrayModel.get(i);
      barHeights[i] = (int) (heightFactor * (length - Math.abs(i - value)));
      Color color = colorGradient.getMarkerColor(value, arrayModel.getMarker(i));
      barColorsRgb[i] = color.getRGB();
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedLength = length;
    cachedHeightFactor = heightFactor;
    cachedGradient = colorGradient;
  }

  @Override
  public void update(float delta) {
    int length = arrayModel.getLength();
    double heightFactor = (screenHeight - 5.) / length;
    ensureBarCache(length, heightFactor);
    if (xywh == null || xywh.length < length * 4) {
      xywh = new float[length * 4];
      argb = new int[length];
    }
    BarColumnBatch.paintCentered(
        rs,
        sound,
        arrayModel,
        barHeights,
        barColorsRgb,
        xywh,
        argb,
        screenWidth,
        screenHeight / 2f);
  }
}
