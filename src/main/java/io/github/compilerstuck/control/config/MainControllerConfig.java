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
   * Fallback settings size when screen bounds are unavailable / before content is packed. Height
   * fits the collapsed one-pager; live content height is packed in {@code SettingsFxController}.
   */
  public static final int SETTINGS_DEFAULT_WIDTH = 960;

  public static final int SETTINGS_DEFAULT_HEIGHT = 560;

  // Frame rate and rendering
  public static final int TARGET_FRAME_RATE = 1000;

  /** Cap when FrameGate step engine is active so grants match display cadence. */
  public static final int STEP_ENGINE_FRAME_RATE = 60;

  public static final int MAX_TEXT_SIZE = 50;

  // Timing (milliseconds)
  public static final int DELAY_BETWEEN_ALGORITHMS = 500;
  public static final int DELAY_AFTER_SORT_RESULT = 1500;
  public static final int SETUP_DELAY = 500;

  // Visualization
  public static final int RESULTS_TABLE_BACKGROUND = 15;
  public static final int RESULTS_TABLE_TEXT_COLOR = 255;
  public static final int FONT_SIZE_RATIO = 20;
  public static final int WINDOW_RATIO_WIDTH = 1280;

  /**
   * Scales a design-time pixel value (at {@link #WINDOW_RATIO_WIDTH}) to the current canvas width.
   * Always at least 1 so Processing {@code textSize} never receives 0.
   */
  public static int scaleToWidth(float designPx, int width) {
    return Math.max(1, Math.round(designPx / (float) WINDOW_RATIO_WIDTH * width));
  }

  // Text positioning
  public static final float TEXT_X_OFFSET = 5.0f;
  public static final float TEXT_Y_OFFSET = 23.0f;
  public static final float LINE_HEIGHT_OFFSET = 20.0f;
  public static final float TABLE_COLUMN_WIDTH_RATIO = 1.0f / 7.0f;
  public static final float TABLE_TOP_ROW = 50.0f;

  // Default configuration (delegates to SettingsDefaults — single source of truth)
  public static final int DEFAULT_ARRAY_SIZE = SettingsDefaults.DEFAULT_ARRAY_SIZE;
}
