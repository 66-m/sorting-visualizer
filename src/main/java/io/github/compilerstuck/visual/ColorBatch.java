package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.render.RenderContext;

/** Avoids redundant {@code stroke}/{@code fill} calls when consecutive primitives share a color. */
final class ColorBatch {

  private int lastRgb;
  private boolean hasStroke;
  private boolean hasFill;

  void reset() {
    hasStroke = false;
    hasFill = false;
  }

  void stroke(RenderContext proc, int rgb) {
    if (!hasStroke || rgb != lastRgb) {
      proc.stroke(rgb);
      lastRgb = rgb;
      hasStroke = true;
    }
  }

  void fill(RenderContext proc, int rgb) {
    if (!hasFill || rgb != lastRgb) {
      proc.fill(rgb);
      lastRgb = rgb;
      hasFill = true;
    }
  }

  /** Sets both stroke and fill to the same RGB, batching when unchanged. */
  void strokeAndFill(RenderContext proc, int rgb) {
    if (!hasStroke || rgb != lastRgb) {
      proc.stroke(rgb);
      hasStroke = true;
    }
    if (!hasFill || rgb != lastRgb) {
      proc.fill(rgb);
      hasFill = true;
    }
    lastRgb = rgb;
  }
}
