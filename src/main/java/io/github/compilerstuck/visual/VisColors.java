package io.github.compilerstuck.visual;

/** Shared ARGB helpers for visualization settings (opacity). */
public final class VisColors {

  private VisColors() {}

  /** Replace alpha of {@code argb} with {@code alpha} in 0–255. */
  public static int withAlpha(int argb, int alpha) {
    int a = Math.max(0, Math.min(255, alpha));
    return (argb & 0x00FFFFFF) | (a << 24);
  }

  /** Scale existing alpha by {@code factor} in 0–1. */
  public static int scaleAlpha(int argb, double factor) {
    int a = (argb >>> 24) & 0xFF;
    int next = (int) Math.round(a * Math.max(0.0, Math.min(1.0, factor)));
    return withAlpha(argb, next);
  }
}
