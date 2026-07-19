package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class HorizontalPyramid extends Visualization {

  private static final float SEAM_OVERLAP = 1f;

  private final ColorBatch colorBatch = new ColorBatch();

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedLength = -1;
  private int cachedHeightScale = -1;
  private ColorGradient cachedGradient;
  private int[] barHeights;
  private int[] barColorsRgb;

  public HorizontalPyramid(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);

    name = "Horizontal Pyramid";
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
  public void update() {
    super.update();

    int length = arrayController.getLength();
    int heightScale = screenHeight - 5;
    float halfScreen = proc.getHeight() / 2f;
    float slotWidth = (float) screenWidth / length;

    ensureBarCache(length, heightScale);

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
      float y0 = halfScreen + barHeights[i] / 2f;
      float y1 = halfScreen - barHeights[i] / 2f;
      proc.vertex(x0, y0);
      proc.vertex(x1, y0);
      proc.vertex(x1, y1);
      proc.vertex(x0, y1);
    }

    proc.endShape();
  }
}
