package io.github.compilerstuck.control.ui;

/** Formats raw timing measurements for on-screen HUD and results table. */
public final class TimeEstimateFormat {
  private TimeEstimateFormat() {}

  /** Formats time estimate from raw measurement to readable milliseconds string. */
  public static String format(double rawTime) {
    double ms = Math.floor(rawTime / 10000.0) / 100;
    return String.valueOf(ms).replace(".", ",");
  }
}
