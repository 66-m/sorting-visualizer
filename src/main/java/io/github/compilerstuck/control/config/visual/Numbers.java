package io.github.compilerstuck.control.config.visual;

/** Shared numeric helpers for visualization settings records. */
public final class Numbers {
  private Numbers() {}

  public static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  public static int clampInt(int value, int min, int max) {
    return (int) clamp(value, min, max);
  }

  /** Round then clamp to an inclusive int range (opacity-style fields). */
  public static int clampRoundedInt(double value, int min, int max) {
    return clampInt((int) Math.round(value), min, max);
  }
}
