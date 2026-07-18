package io.github.compilerstuck.visual;

public final class LodStride {
  private LodStride() {}
  /** Returns stride >= 1 so that roughly maxPrimitives samples cover length. */
  public static int forLength(int length, int maxPrimitives) {
    if (length <= 0 || maxPrimitives <= 0) return 1;
    if (length <= maxPrimitives) return 1;
    return (length + maxPrimitives - 1) / maxPrimitives;
  }
}
