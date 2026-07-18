package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class DisparityGraphMirrored extends Visualization {

  private final IndexXCache indexXCache = new IndexXCache();
  private final ColorBatch colorBatch = new ColorBatch();

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedLength = -1;
  private int[] barHeights;

  public DisparityGraphMirrored(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Disparity Graph Mirrored";
  }

  private void ensureHeights(int length, double heightFactor) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedLength == length) {
      return;
    }
    if (barHeights == null || barHeights.length < length) {
      barHeights = new int[length];
    }
    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      barHeights[i] = (int) (heightFactor * (length - Math.abs(i - value)));
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedLength = length;
  }

  @Override
  public void update() {
    super.update();

    int length = arrayController.getLength();
    int rectWidth = (screenWidth - (length - 1)) / length;
    double heightFactor = (screenHeight - 5.) / length;
    float halfScreen = proc.getHeight() / 2f;

    indexXCache.ensure(length, screenWidth);
    float[] xs = indexXCache.xs();
    ensureHeights(length, heightFactor);

    colorBatch.reset();
    for (int i = 0; i < length; i++) {
      Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      colorBatch.strokeAndFill(proc, color.getRGB());
      proc.rect(xs[i], halfScreen + barHeights[i] / 2f, rectWidth, -1 * barHeights[i]);
    }
  }
}
