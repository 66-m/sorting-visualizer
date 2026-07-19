package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class DisparityGraph extends Visualization {

  private static final float SEAM_OVERLAP = 1f;

  private final ColorBatch colorBatch = new ColorBatch();

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedLength = -1;
  private double cachedHeightFactor = -1;
  private ColorGradient cachedGradient;
  private int[] barHeights;
  private int[] barColorsRgb;

  public DisparityGraph(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Disparity Graph";
  }

  private void ensureBarCache(int length, double heightFactor) {
    long rev = arrayController.getVisualRevision();
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
      int value = arrayController.get(i);
      barHeights[i] =
          (int)
              (heightFactor
                  * (length
                      - 2
                          * Math.min(
                              Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                              Math.abs(i + length - value))));
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));
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
  public void update() {
    super.update();

    int length = arrayController.getLength();
    double heightFactor = (screenHeight - 10.) / length;
    float slotWidth = (float) screenWidth / length;

    ensureBarCache(length, heightFactor);

    proc.noStroke();
    colorBatch.reset();
    proc.beginShape(RenderContext.QUADS);

    for (int i = 0; i < length; i++) {
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
