package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class Bars extends Visualization {

  /** Extra width so adjacent fill quads overlap and hide AA hairlines. */
  private static final float SEAM_OVERLAP = 1f;

  private final ColorBatch colorBatch = new ColorBatch();

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedN = -1;
  private ColorGradient cachedGradient;
  private int[] barHeights;
  private int[] barColorsRgb;

  public Bars(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Bars";
  }

  private void ensureBarCache(int n, int heightScale) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedN == n
        && cachedGradient == colorGradient) {
      return;
    }
    if (barHeights == null || barHeights.length < n) {
      barHeights = new int[n];
      barColorsRgb = new int[n];
    }
    for (int i = 0; i < n; i++) {
      int value = arrayController.get(i);
      barHeights[i] = (value + 1) * heightScale / n;
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));
      barColorsRgb[i] = color.getRGB();
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedN = n;
    cachedGradient = colorGradient;
  }

  @Override
  public void update() {
    super.update();

    int n = arrayController.getLength();
    float slotWidth = (float) screenWidth / n;
    int heightScale = screenHeight - 5;

    ensureBarCache(n, heightScale);

    proc.noStroke();
    colorBatch.reset();
    proc.beginShape(RenderContext.QUADS);

    for (int i = 0; i < n; i++) {
      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }
      arrayController.setMarker(i, Marker.NORMAL);

      colorBatch.fill(proc, barColorsRgb[i]);
      float x0 = i * slotWidth;
      float x1 = Math.min(screenWidth, (i + 1) * slotWidth + SEAM_OVERLAP);
      float y0 = screenHeight;
      float y1 = screenHeight - barHeights[i];
      proc.vertex(x0, y0);
      proc.vertex(x1, y0);
      proc.vertex(x1, y1);
      proc.vertex(x0, y1);
    }

    proc.endShape();
  }
}
