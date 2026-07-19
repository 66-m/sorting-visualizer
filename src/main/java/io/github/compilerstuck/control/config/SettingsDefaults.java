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
  public static final boolean DEFAULT_MUTED = false;

  /** Phase 4 additive defaults (missing prefs keys resolve to these, no version bump). */
  public static final ShuffleType DEFAULT_SHUFFLE_TYPE = ShuffleType.RANDOM;

  public static final boolean DEFAULT_PRINT_MEASUREMENTS = true;
  public static final boolean DEFAULT_SHOW_COMPARISON_TABLE = false;
  public static final String DEFAULT_IMAGE_PATH = "";
  public static final String DEFAULT_GRADIENT_NAME = "Black -> Red";
  public static final int DEFAULT_GRADIENT_COLOR1_RGB = Color.PINK.getRGB();
  public static final int DEFAULT_GRADIENT_COLOR2_RGB = Color.BLACK.getRGB();
  public static final boolean DEFAULT_RUN_ALL = false;
  public static final String DEFAULT_RUN_ALL_ENTRIES = "";
  public static final boolean DEFAULT_PERF_STATS = false;

  /** JSON map of visualizationId → settings object (see VisualizationSettingsCodec). */
  public static final String DEFAULT_VISUAL_SETTINGS_BY_ID = "{}";

  public static final int ARRAY_SIZE_MIN = 3;
  public static final int ARRAY_SIZE_MAX = 100_000;

  /** Warn in Settings when idle preview FPS falls below this while choosing array size. */
  public static final int ARRAY_SIZE_FPS_WARNING_THRESHOLD = 24;

  /** Crossing above this size from at-or-below shows a one-shot lag warning in Settings. */
  public static final int ARRAY_SIZE_HIGH_WARNING_THRESHOLD = 20_000;
  public static final int SPEED_LEVEL_MIN = 1;
  public static final int SPEED_LEVEL_MAX = 5;

  /** Speed levels 1–5 → steps granted per draw frame. */
  public static final int[] STEPS_PER_FRAME = {1, 5, 25, 200, 2000};

  private SettingsDefaults() {}

  public static int clampArraySize(int size) {
    return Math.max(ARRAY_SIZE_MIN, Math.min(ARRAY_SIZE_MAX, size));
  }

  public static int clampSpeedLevel(int level) {
    return Math.max(SPEED_LEVEL_MIN, Math.min(SPEED_LEVEL_MAX, level));
  }
}
