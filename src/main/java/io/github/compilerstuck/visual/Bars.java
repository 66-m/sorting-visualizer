package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;

public class Bars extends Visualization {

  private final IndexXCache indexXCache = new IndexXCache();
  private final ColorBatch colorBatch = new ColorBatch();

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedN = -1;
  private int cachedStride = -1;
  private int cachedBucketCount = -1;
  private ColorGradient cachedGradient;
  private int[] barHeights;
  private int[] barColorsRgb;

  public Bars(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Bars";
  }

  private void ensureBucketCache(int n, int stride, int bucketCount, int heightScale) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedN == n
        && cachedStride == stride
        && cachedBucketCount == bucketCount
        && cachedGradient == colorGradient) {
      return;
    }
    if (barHeights == null || barHeights.length < bucketCount) {
      barHeights = new int[bucketCount];
      barColorsRgb = new int[bucketCount];
    }
    for (int b = 0, i = 0; b < bucketCount; b++, i += stride) {
      int bucketEnd = Math.min(i + stride, n);
      int maxValuePlusOne = 0;
      int colorIndex = i;

      for (int j = i; j < bucketEnd; j++) {
        int valuePlusOne = arrayController.get(j) + 1;
        if (valuePlusOne > maxValuePlusOne) {
          maxValuePlusOne = valuePlusOne;
          colorIndex = j;
        }
      }

      barHeights[b] = maxValuePlusOne * heightScale / n;

      Color color =
          colorGradient.getMarkerColor(
              arrayController.get(colorIndex), arrayController.getMarker(colorIndex));
      barColorsRgb[b] = color.getRGB();
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedN = n;
    cachedStride = stride;
    cachedBucketCount = bucketCount;
    cachedGradient = colorGradient;
  }

  @Override
  public void update() {
    super.update();

    int n = arrayController.getLength();
    int maxPrimitives = Math.max(1, Math.min(screenWidth, 2048));
    int stride = LodStride.forLength(n, maxPrimitives);
    int bucketCount = (n + stride - 1) / stride;
    int rectWidth = Math.max(1, (screenWidth - (bucketCount - 1)) / bucketCount);
    int heightScale = screenHeight - 5;

    indexXCache.ensure(n, screenWidth);
    float[] xs = indexXCache.xs();
    ensureBucketCache(n, stride, bucketCount, heightScale);

    colorBatch.reset();
    for (int b = 0, i = 0; b < bucketCount; b++, i += stride) {
      int bucketEnd = Math.min(i + stride, n);
      int soundIndex = -1;

      for (int j = i; j < bucketEnd; j++) {
        if (arrayController.getMarker(j) == Marker.SET) {
          soundIndex = j;
        }
      }

      if (soundIndex >= 0) {
        sound.playSound(soundIndex);
      }

      for (int j = i; j < bucketEnd; j++) {
        arrayController.setMarker(j, Marker.NORMAL);
      }

      colorBatch.strokeAndFill(proc, barColorsRgb[b]);
      proc.rect(xs[i], screenHeight, rectWidth, -1 * barHeights[b]);
    }
  }
}
