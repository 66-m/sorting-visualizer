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
    if (src == null || dst == null || width <= 0 || height <= 0 || length <= 0) {
      return;
    }
    if (horizontal) {
      remapHorizontal(src, dst, width, height, stripIndices, stripHighlight, length);
    } else {
      remapVertical(src, dst, width, height, stripIndices, stripHighlight, length);
    }
  }

  private static void remapHorizontal(
      int[] src,
      int[] dst,
      int width,
      int height,
      int[] stripIndices,
      boolean[] stripHighlight,
      int length) {
    int band = height / length;
    if (band <= 0) {
      return;
    }
    for (int i = 0; i < length; i++) {
      int srcStrip = clampIndex(stripIndices[i], length);
      int srcPos = srcStrip * band;
      boolean highlight = stripHighlight != null && stripHighlight[i];
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
            dst[dstRow + x] = WHITE;
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
      int length) {
    int band = width / length;
    if (band <= 0) {
      return;
    }
    for (int i = 0; i < length; i++) {
      int srcStrip = clampIndex(stripIndices[i], length);
      int srcPos = srcStrip * band;
      boolean highlight = stripHighlight != null && stripHighlight[i];
      for (int x = srcPos; x < srcPos + band; x++) {
        int dstX = x - srcPos + i * band;
        if (dstX < 0 || dstX >= width || x < 0 || x >= width) {
          continue;
        }
        if (highlight) {
          for (int y = 0; y < height; y++) {
            dst[dstX + y * width] = WHITE;
          }
        } else {
          for (int y = 0; y < height; y++) {
            dst[dstX + y * width] = src[x + y * width];
          }
        }
      }
    }
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
