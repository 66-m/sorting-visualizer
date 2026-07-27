package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.NumberPlotSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

public class NumberPlot extends Visualization implements ConfigurableVisualization {

  private final IndexXCache indexXCache = new IndexXCache();

  private volatile NumberPlotSettings settings = NumberPlotSettings.defaults();

  private int cachedLength = -1;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int[] cachedValues;
  private int[] barHeights;
  private String[] labels;
  private float[] drawYs;

  public NumberPlot(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Number Plot";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof NumberPlotSettings s) {
      settings = s;
    }
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
        drawYs = new float[length];
        for (int i = 0; i < length; i++) {
          cachedValues[i] = Integer.MIN_VALUE;
        }
      }
    }
    for (int i = 0; i < length; i++) {
      int value = arrayModel.get(i);
      if (layoutChanged || cachedValues[i] != value) {
        cachedValues[i] = value;
        barHeights[i] = (value + 1) * heightScale / length;
        labels[i] = String.valueOf(value + 1);
      }
    }
  }

  @Override
  public void update(float delta) {
    NumberPlotSettings s = settings;
    int length = arrayModel.getLength();
    int heightScale = screenHeight - 5;

    indexXCache.ensure(length, screenWidth);
    float[] xs = indexXCache.xs();
    ensureSlotCaches(length, heightScale);

    for (int i = 0; i < length; i++) {
      if (arrayModel.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }
      drawYs[i] = worldYToOverlayY(barHeights[i]);
    }

    if (length > 0) {
      rs.drawTexts(labels, xs, drawYs, (float) s.fontSize(), length);
    }
  }
}
