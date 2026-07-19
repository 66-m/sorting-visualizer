package io.github.compilerstuck.control.render.asset;

import java.util.Objects;

/** Loaded image ready for Overlay remapping. Holds CPU ARGB source pixels. */
public final class ImageHandle {
  private final int width;
  private final int height;
  private final int[] argb;
  private final String path;
  private int generation;

  public ImageHandle(String path, int width, int height, int[] argb) {
    this.path = path == null ? "" : path;
    this.width = Math.max(1, width);
    this.height = Math.max(1, height);
    this.argb = Objects.requireNonNull(argb, "argb");
    if (argb.length < this.width * this.height) {
      throw new IllegalArgumentException("argb too short");
    }
    this.generation = 1;
  }

  public String path() {
    return path;
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  /** Source pixels in ARGB; do not mutate. */
  public int[] argb() {
    return argb;
  }

  /** Bumps when content is replaced; used as remap/upload cache key. */
  public int generation() {
    return generation;
  }

  void bumpGeneration() {
    generation++;
  }
}
