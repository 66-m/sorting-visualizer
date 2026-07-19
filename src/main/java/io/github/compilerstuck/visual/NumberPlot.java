package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class NumberPlot extends Visualization {

  private final IndexXCache indexXCache = new IndexXCache();
  private final ColorBatch colorBatch = new ColorBatch();

  private int cachedLength = -1;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int[] cachedValues;
  private int[] barHeights;
  private String[] labels;

  public NumberPlot(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Number Plot";
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
  public void update() {
    super.update();

    int length = arrayController.getLength();
    int heightScale = screenHeight - 5;

    indexXCache.ensure(length, screenWidth);
    float[] xs = indexXCache.xs();
    ensureSlotCaches(length, heightScale);

    proc.textSize(14);
    proc.noStroke();
    colorBatch.reset();
    for (int i = 0; i < length; i++) {
      Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      colorBatch.fill(proc, color.getRGB());
      proc.text(labels[i], xs[i], screenHeight - barHeights[i]);
    }
  }
}
