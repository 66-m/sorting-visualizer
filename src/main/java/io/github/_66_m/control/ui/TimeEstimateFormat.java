package io.github._66_m.control.ui;

import java.util.Locale;

/**
 * Formats accumulated behind-the-scenes sort time (nanoseconds of algorithm work, excluding
 * FrameGate / visual pacing waits) for the HUD and results table.
 */
public final class TimeEstimateFormat {
  private TimeEstimateFormat() {}

  /** Formats nanoseconds as milliseconds with two decimal places (comma decimal separator). */
  public static String format(double rawTimeNs) {
    return formatMillis(rawTimeNs).replace('.', ',');
  }

  /**
   * Formats nanoseconds as milliseconds with two decimal places and a dot decimal separator, for
   * machine-readable output such as CSV.
   */
  public static String formatMillis(double rawTimeNs) {
    if (!(rawTimeNs > 0) || Double.isNaN(rawTimeNs) || Double.isInfinite(rawTimeNs)) {
      return "0.00";
    }
    return String.format(Locale.ROOT, "%.2f", rawTimeNs / 1_000_000.0);
  }
}
