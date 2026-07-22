package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;

/** Shared seam-overlapping column fill for bar-style visualizations. */
final class BarColumnBatch {

  static final float SEAM_OVERLAP = 1f;

  private BarColumnBatch() {}

  /** Bottom-aligned columns (y = 0). */
  static void paintBottomAligned(
      RenderSystem rs,
      Sound sound,
      ArrayModel arrayModel,
      int[] heights,
      int[] colorsArgb,
      float[] xywh,
      int[] argb,
      float screenWidth) {
    paint(rs, sound, arrayModel, heights, colorsArgb, xywh, argb, screenWidth, true, 0f);
  }

  /** Vertically centered columns around {@code centerY}. */
  static void paintCentered(
      RenderSystem rs,
      Sound sound,
      ArrayModel arrayModel,
      int[] heights,
      int[] colorsArgb,
      float[] xywh,
      int[] argb,
      float screenWidth,
      float centerY) {
    paint(rs, sound, arrayModel, heights, colorsArgb, xywh, argb, screenWidth, false, centerY);
  }

  private static void paint(
      RenderSystem rs,
      Sound sound,
      ArrayModel arrayModel,
      int[] heights,
      int[] colorsArgb,
      float[] xywh,
      int[] argb,
      float screenWidth,
      boolean bottomAligned,
      float centerY) {
    int n = arrayModel.getLength();
    float slotWidth = screenWidth / n;
    for (int i = 0; i < n; i++) {
      if (arrayModel.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }
      float x0 = i * slotWidth;
      float x1 = Math.min(screenWidth, (i + 1) * slotWidth + SEAM_OVERLAP);
      float h = heights[i];
      int o = i * 4;
      xywh[o] = x0;
      xywh[o + 1] = bottomAligned ? 0f : centerY - h / 2f;
      xywh[o + 2] = x1 - x0;
      xywh[o + 3] = h;
      argb[i] = colorsArgb[i];
    }
    rs.fillRects(xywh, argb, n);
  }
}
