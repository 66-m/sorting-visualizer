package io.github.compilerstuck.control.render;

/**
 * Abstraction over a loaded raster image used by image-based visualizations. Visuals must not
 * depend on {@code processing.core.PImage} directly.
 */
public interface LoadedImage {
  int pixelWidth();

  int pixelHeight();

  int[] pixels();

  void loadPixels();

  void resize(int w, int h);
}
