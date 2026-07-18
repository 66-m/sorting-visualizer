package io.github.compilerstuck.control.render;

import processing.core.PImage;

/**
 * Adapter that exposes a Processing {@link PImage} as a {@link LoadedImage}. Extends {@link PImage}
 * so {@code MainController.loadImage} can covariantly override {@code PApplet.loadImage} while
 * returning this type.
 */
public class ProcessingLoadedImage extends PImage implements LoadedImage {

  public ProcessingLoadedImage(PImage source) {
    super(source.width, source.height, source.format, source.pixelDensity);
    this.pixels = source.pixels;
    this.pixelWidth = source.pixelWidth;
    this.pixelHeight = source.pixelHeight;
    this.parent = source.parent;
    this.loaded = source.loaded;
  }

  @Override
  public int pixelWidth() {
    return pixelWidth;
  }

  @Override
  public int pixelHeight() {
    return pixelHeight;
  }

  @Override
  public int[] pixels() {
    return pixels;
  }
}
