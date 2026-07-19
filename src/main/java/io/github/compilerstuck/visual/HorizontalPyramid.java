package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class HorizontalPyramid extends Visualization {

  private static final float SEAM_OVERLAP = 1f;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedLength = -1;
  private int cachedHeightScale = -1;
  private ColorGradient cachedGradient;
  private int[] barHeights;
  private int[] barColorsRgb;
  private float[] xywh;
  private int[] argb;

  public HorizontalPyramid(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Horizontal Pyramid";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void ensureBarCache(int length, int heightScale) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedLength == length
        && cachedHeightScale == heightScale
        && cachedGradient == colorGradient) {
      return;
    }
    if (barHeights == null || barHeights.length < length) {
      barHeights = new int[length];
      barColorsRgb = new int[length];
    }
    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      barHeights[i] = (value + 1) * heightScale / length;
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));
      barColorsRgb[i] = color.getRGB();
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedLength = length;
    cachedHeightScale = heightScale;
    cachedGradient = colorGradient;
  }

  @Override
  public void update(float delta) {
    int length = arrayController.getLength();
    int heightScale = screenHeight - 5;
    float halfScreen = screenHeight / 2f;
    float slotWidth = (float) screenWidth / length;

    ensureBarCache(length, heightScale);

    if (xywh == null || xywh.length < length * 4) {
      xywh = new float[length * 4];
      argb = new int[length];
    }

    for (int i = 0; i < length; i++) {
      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      float x0 = i * slotWidth;
      float x1 = Math.min(screenWidth, (i + 1) * slotWidth + SEAM_OVERLAP);
      int o = i * 4;
      xywh[o] = x0;
      xywh[o + 1] = halfScreen - barHeights[i] / 2f;
      xywh[o + 2] = x1 - x0;
      xywh[o + 3] = barHeights[i];
      argb[i] = barColorsRgb[i];
    }
    rs.fillRects(xywh, argb, length);
  }
}
