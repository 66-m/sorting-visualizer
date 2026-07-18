package io.github.compilerstuck.control.config;

import java.awt.Color;

/**
 * Single source of truth for Settings / session defaults (G10). Referenced by {@link
 * UserPreferences}, {@link MainControllerConfig}, and {@link
 * io.github.compilerstuck.control.AppContext} so magic indices are not duplicated.
 */
public final class SettingsDefaults {

  public static final String DEFAULT_ALGORITHM_ID = "quicksort-middle";
  public static final String DEFAULT_VISUALIZATION_ID = "bars";
  public static final int DEFAULT_ARRAY_SIZE = 1280;
  public static final int DEFAULT_SPEED_LEVEL = 3;
  public static final boolean DEFAULT_USE_STEP_ENGINE = false;
  public static final boolean DEFAULT_MUTED = false;

  /** Phase 4 additive defaults (missing prefs keys resolve to these — no version bump). */
  public static final ShuffleType DEFAULT_SHUFFLE_TYPE = ShuffleType.RANDOM;

  public static final boolean DEFAULT_PRINT_MEASUREMENTS = true;
  public static final boolean DEFAULT_SHOW_COMPARISON_TABLE = false;
  public static final String DEFAULT_IMAGE_PATH = "";
  public static final String DEFAULT_GRADIENT_NAME = "Black -> Red";
  public static final int DEFAULT_GRADIENT_COLOR1_RGB = Color.PINK.getRGB();
  public static final int DEFAULT_GRADIENT_COLOR2_RGB = Color.BLACK.getRGB();
  public static final boolean DEFAULT_RUN_ALL = false;
  public static final String DEFAULT_RUN_ALL_ENTRIES = "";

  public static final int ARRAY_SIZE_MIN = 3;
  public static final int ARRAY_SIZE_MAX = 20_000;
  public static final int SPEED_LEVEL_MIN = 1;
  public static final int SPEED_LEVEL_MAX = 5;

  /** Speed levels 1–5 → delay millis when the step engine is off. */
  public static final int[] DELAY_TIME = {50, 10, 1, 1, 1};

  /** Speed levels 1–5 → delay factor when the step engine is off. */
  public static final double[] DELAY_FACTOR = {1.0, 1.0, 1.0, 0.12, 0.02};

  /** Speed levels 1–5 → steps per frame when the step engine is enabled. */
  public static final int[] STEPS_PER_FRAME = {1, 5, 25, 200, 2000};

  private SettingsDefaults() {}

  public static int clampArraySize(int size) {
    return Math.max(ARRAY_SIZE_MIN, Math.min(ARRAY_SIZE_MAX, size));
  }

  public static int clampSpeedLevel(int level) {
    return Math.max(SPEED_LEVEL_MIN, Math.min(SPEED_LEVEL_MAX, level));
  }
}
