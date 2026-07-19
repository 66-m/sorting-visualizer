package io.github.compilerstuck.control.ui;

import java.util.Locale;

/**
 * Formats accumulated behind-the-scenes sort time (nanoseconds of algorithm work, excluding
 * FrameGate / visual pacing waits) for the HUD and results table.
 */
public final class TimeEstimateFormat {
  private TimeEstimateFormat() {}

  /** Formats nanoseconds as milliseconds with two decimal places (comma decimal separator). */
  public static String format(double rawTimeNs) {
    if (!(rawTimeNs > 0) || Double.isNaN(rawTimeNs) || Double.isInfinite(rawTimeNs)) {
      return "0,00";
    }
    double ms = rawTimeNs / 1_000_000.0;
    return String.format(Locale.ROOT, "%.2f", ms).replace('.', ',');
  }
}
