package io.github.compilerstuck.control.config;

/**
 * Configuration constants for MainController. Centralizes all magic numbers and configuration
 * values for easier maintenance.
 */
public final class MainControllerConfig {
  private MainControllerConfig() {
    // Utility class
  }

  // Window dimensions (design / aspect references; launch sizes are screen-relative)
  public static final int STANDARD_WIDTH = 1280;
  public static final int STANDARD_HEIGHT = 720;
  public static final int PORTRAIT_WIDTH = 576;
  public static final int PORTRAIT_HEIGHT = 1024;

  /** Minimum size when the visualization window is user-resizable (windowed mode). */
  public static final int MIN_WINDOW_WIDTH = 640;

  public static final int MIN_WINDOW_HEIGHT = 360;

  /** Minimum settings frame size so 1/4-area windows remain usable on small screens. */
  public static final int SETTINGS_MIN_WIDTH = 480;

  public static final int SETTINGS_MIN_HEIGHT = 360;

  /**
   * Fallback settings size when the primary screen is unavailable. Live size is 3/4 of {@code
   * Screen.getPrimary().getVisualBounds()} in {@code SettingsFxController}.
   */
  public static final int SETTINGS_DEFAULT_WIDTH = 960;

  public static final int SETTINGS_DEFAULT_HEIGHT = 560;

  /** Settings window default size as a fraction of the screen it opens on. */
  public static final double SETTINGS_SCREEN_FRACTION = 0.75;

  // Frame rate and rendering (display cadence; sort pacing uses FrameGate)
  public static final int TARGET_FRAME_RATE = 60;

  public static final int MAX_TEXT_SIZE = 50;

  // Timing (milliseconds)
  public static final int DELAY_BETWEEN_ALGORITHMS = 500;
  public static final int DELAY_AFTER_SORT_RESULT = 1500;
  public static final int SETUP_DELAY = 500;

  /**
   * Shuffle animation length in seconds. Pacing uses a fixed visual-step budget (not the sort speed
   * setting).
   */
  public static final float SHUFFLE_DURATION_SEC = 1f;

  /** Frame-gate steps fired across one shuffle animation ({@link #SHUFFLE_DURATION_SEC}). */
  public static final int SHUFFLE_VISUAL_STEPS = 1000;

  // Visualization
  public static final int RESULTS_TABLE_BACKGROUND = 15;
  public static final int RESULTS_TABLE_TEXT_COLOR = 255;
  public static final int FONT_SIZE_RATIO = 20;
  public static final int WINDOW_RATIO_WIDTH = 1280;

  /**
   * Scales a design-time pixel value (at {@link #WINDOW_RATIO_WIDTH}) to the current canvas width.
   * Always at least 1 so text size / font lookup never receives 0.
   */
  public static int scaleToWidth(float designPx, int width) {
    return Math.max(1, Math.round(designPx / (float) WINDOW_RATIO_WIDTH * width));
  }

  /**
   * Steps to grant this frame so {@link #SHUFFLE_VISUAL_STEPS} complete in {@link
   * #SHUFFLE_DURATION_SEC}, independent of the sort speed setting.
   */
  public static int shuffleStepsForDelta(float deltaSeconds) {
    if (deltaSeconds <= 0f) {
      return 1;
    }
    return Math.max(1, Math.round(SHUFFLE_VISUAL_STEPS * deltaSeconds / SHUFFLE_DURATION_SEC));
  }

  // Text positioning
  public static final float TEXT_X_OFFSET = 5.0f;
  public static final float TEXT_Y_OFFSET = 23.0f;
  public static final float LINE_HEIGHT_OFFSET = 20.0f;
  public static final float TABLE_COLUMN_WIDTH_RATIO = 1.0f / 7.0f;
  public static final float TABLE_TOP_ROW = 50.0f;

  // Default configuration (delegates to SettingsDefaults, single source of truth)
  public static final int DEFAULT_ARRAY_SIZE = SettingsDefaults.DEFAULT_ARRAY_SIZE;
}
