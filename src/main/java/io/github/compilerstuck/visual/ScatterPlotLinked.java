package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class ScatterPlotLinked extends Visualization {

  private final IndexXCache indexXCache = new IndexXCache();
  private final ColorBatch colorBatch = new ColorBatch();

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedN = -1;
  private int[] barHeights;

  public ScatterPlotLinked(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Scatter Plot Linked";
  }

  private void ensureHeights(int n, int heightScale) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedN == n) {
      return;
    }
    if (barHeights == null || barHeights.length < n) {
      barHeights = new int[n];
    }
    for (int i = 0; i < n; i++) {
      barHeights[i] = (arrayController.get(i) + 1) * heightScale / n;
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedN = n;
  }

  @Override
  public void update() {
    super.update();

    int n = arrayController.getLength();
    int maxPrimitives = Math.min(Math.max(screenWidth * 2, 1), 4096);
    int stride = LodStride.forLength(n, maxPrimitives);
    int heightScale = screenHeight - 5;

    indexXCache.ensure(n, screenWidth);
    float[] xs = indexXCache.xs();
    ensureHeights(n, heightScale);

    colorBatch.reset();
    for (int i = 0; i < n - 1; i += stride) {
      int next = Math.min(i + stride, n - 1);
      Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      colorBatch.strokeAndFill(proc, color.getRGB());
      proc.line(
          xs[i],
          screenHeight - barHeights[i],
          xs[next],
          screenHeight - barHeights[next]);
    }
  }
}
