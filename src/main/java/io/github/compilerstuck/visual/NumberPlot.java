package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

public class NumberPlot extends Visualization {

  private static final int LOD_TARGET_LABELS = 512;

  private final IndexXCache indexXCache = new IndexXCache();

  private int cachedLength = -1;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int[] cachedValues;
  private int[] barHeights;
  private String[] labels;
  private String[] drawLabels;
  private float[] drawXs;
  private float[] drawYs;

  public NumberPlot(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Number Plot";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void ensureSlotCaches(int length, int heightScale) {
    boolean layoutChanged =
        cachedLength != length || cachedWidth != screenWidth || cachedHeight != screenHeight;
    if (layoutChanged) {
      cachedLength = length;
      cachedWidth = screenWidth;
      cachedHeight = screenHeight;
      if (cachedValues == null || cachedValues.length < length) {
        cachedValues = new int[length];
        barHeights = new int[length];
        labels = new String[length];
        drawLabels = new String[length];
        drawXs = new float[length];
        drawYs = new float[length];
        for (int i = 0; i < length; i++) {
          cachedValues[i] = Integer.MIN_VALUE;
        }
      }
    }
    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      if (layoutChanged || cachedValues[i] != value) {
        cachedValues[i] = value;
        barHeights[i] = (value + 1) * heightScale / length;
        labels[i] = String.valueOf(value + 1);
      }
    }
  }

  @Override
  public void update(float delta) {
    int length = arrayController.getLength();
    int heightScale = screenHeight - 5;

    indexXCache.ensure(length, screenWidth);
    float[] xs = indexXCache.xs();
    ensureSlotCaches(length, heightScale);

    int step = 1;
    if (length > LOD_TARGET_LABELS) {
      step = (length + LOD_TARGET_LABELS - 1) / LOD_TARGET_LABELS;
    }

    int drawCount = 0;
    for (int i = 0; i < length; i++) {
      boolean highlight = arrayController.getMarker(i) == Marker.SET;
      if (highlight) {
        sound.playSound(i);
      }
      if (step > 1 && (i % step) != 0 && !highlight) {
        continue;
      }
      drawLabels[drawCount] = labels[i];
      drawXs[drawCount] = xs[i];
      drawYs[drawCount] = worldYToOverlayY(barHeights[i]);
      drawCount++;
    }

    if (drawCount > 0) {
      rs.drawTexts(drawLabels, drawXs, drawYs, 14, drawCount);
    }
  }
}
