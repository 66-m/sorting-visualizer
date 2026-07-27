package io.github.compilerstuck.control.render.asset;

/** CPU strip remap for Overlay image visuals (Fake / fallback path). */
public final class ImageStripRemap {
  private static final int WHITE = 0xFFFFFFFF;

  private ImageStripRemap() {}

  public static void remap(
      int[] src,
      int[] dst,
      int width,
      int height,
      int[] stripIndices,
      boolean[] stripHighlight,
      int length,
      boolean horizontal) {
    remap(src, dst, width, height, stripIndices, stripHighlight, length, horizontal, 1f);
  }

  public static void remap(
      int[] src,
      int[] dst,
      int width,
      int height,
      int[] stripIndices,
      boolean[] stripHighlight,
      int length,
      boolean horizontal,
      float highlightStrength) {
    if (src == null || dst == null || width <= 0 || height <= 0 || length <= 0) {
      return;
    }
    float strength = Math.max(0f, Math.min(1f, highlightStrength));
    if (horizontal) {
      remapHorizontal(src, dst, width, height, stripIndices, stripHighlight, length, strength);
    } else {
      remapVertical(src, dst, width, height, stripIndices, stripHighlight, length, strength);
    }
  }

  private static void remapHorizontal(
      int[] src,
      int[] dst,
      int width,
      int height,
      int[] stripIndices,
      boolean[] stripHighlight,
      int length,
      float strength) {
    int band = height / length;
    if (band <= 0) {
      return;
    }
    for (int i = 0; i < length; i++) {
      int srcStrip = clampIndex(stripIndices[i], length);
      int srcPos = srcStrip * band;
      boolean highlight = stripHighlight != null && stripHighlight[i] && strength > 0f;
      for (int y = srcPos; y < srcPos + band; y++) {
        if (y < 0 || y >= height) {
          continue;
        }
        int dstY = y - srcPos + i * band;
        if (dstY < 0 || dstY >= height) {
          continue;
        }
        int srcRow = y * width;
        int dstRow = dstY * width;
        if (highlight) {
          for (int x = 0; x < width; x++) {
            dst[dstRow + x] = blendTowardWhite(src[srcRow + x], strength);
          }
        } else {
          System.arraycopy(src, srcRow, dst, dstRow, width);
        }
      }
    }
  }

  private static void remapVertical(
      int[] src,
      int[] dst,
      int width,
      int height,
      int[] stripIndices,
      boolean[] stripHighlight,
      int length,
      float strength) {
    int band = width / length;
    if (band <= 0) {
      return;
    }
    for (int i = 0; i < length; i++) {
      int srcStrip = clampIndex(stripIndices[i], length);
      int srcPos = srcStrip * band;
      boolean highlight = stripHighlight != null && stripHighlight[i] && strength > 0f;
      for (int x = srcPos; x < srcPos + band; x++) {
        int dstX = x - srcPos + i * band;
        if (dstX < 0 || dstX >= width || x < 0 || x >= width) {
          continue;
        }
        if (highlight) {
          for (int y = 0; y < height; y++) {
            dst[dstX + y * width] = blendTowardWhite(src[x + y * width], strength);
          }
        } else {
          for (int y = 0; y < height; y++) {
            dst[dstX + y * width] = src[x + y * width];
          }
        }
      }
    }
  }

  private static int blendTowardWhite(int argb, float strength) {
    if (strength >= 0.999f) {
      return WHITE;
    }
    if (strength <= 0.001f) {
      return argb;
    }
    int a = (argb >>> 24) & 0xFF;
    int r = (argb >>> 16) & 0xFF;
    int g = (argb >>> 8) & 0xFF;
    int b = argb & 0xFF;
    r = r + Math.round((255 - r) * strength);
    g = g + Math.round((255 - g) * strength);
    b = b + Math.round((255 - b) * strength);
    return (a << 24) | (r << 16) | (g << 8) | b;
  }

  private static int clampIndex(int index, int length) {
    if (index < 0) {
      return 0;
    }
    if (index >= length) {
      return length - 1;
    }
    return index;
  }
}
