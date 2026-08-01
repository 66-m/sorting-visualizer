package io.github.compilerstuck.control.config;

/** Application runtime configuration constants (window sizes, timings, layout ratios). */
public final class AppConfig {
  private AppConfig() {
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

  /** Minimum settings frame size so a stacked one-pager remains usable on small screens. */
  public static final int SETTINGS_MIN_WIDTH = 560;

  public static final int SETTINGS_MIN_HEIGHT = 360;

  /**
   * Viewport width below which the Settings body stacks columns vertically instead of side-by-side.
   * Measured against the ScrollPane viewport (already inset by shell padding); a 720-wide scene
   * stays side-by-side.
   */
  public static final int SETTINGS_STACK_BREAKPOINT = 640;

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

  /** Upper clamp for FreeType HUD font generation (safety against pathological window widths). */
  public static final int MAX_TEXT_SIZE = 128;

  // Timing (milliseconds)
  public static final int DELAY_BETWEEN_ALGORITHMS = 500;
  public static final int DELAY_AFTER_SORT_RESULT = 1500;
  public static final int SETUP_DELAY = 500;

  /** Longer setup delay when the user needs time to switch to the visualization window. */
  public static final int SETUP_DELAY_LONG = 5000;

  /**
   * Shuffle animation length in seconds. Pacing uses a fixed visual-step budget (not the sort speed
   * setting).
   */
  public static final float SHUFFLE_DURATION_SEC = 1.5f;

  /** Frame-gate steps fired across one shuffle animation ({@link #SHUFFLE_DURATION_SEC}). */
  public static final int SHUFFLE_VISUAL_STEPS = 1000;

  /**
   * Max wall time for the silent dry-run that counts visual steps before an equalized sort. On
   * timeout, step totals are extrapolated via {@code estimateRawSteps}. Matches {@link
   * #SHUFFLE_DURATION_SEC} so prep fits under the shuffle visual cover; {@code n log n} sorts
   * usually finish with an exact count inside this budget.
   */
  public static final long EQUALIZE_DRY_RUN_TIMEOUT_MS = Math.round(SHUFFLE_DURATION_SEC * 1000f);

  /**
   * Soft cap on equalize step credits granted per draw frame (plain {@code delay()} algorithms).
   * Prevents a single frame from executing hundreds of thousands of visualized ops.
   */
  public static final int EQUALIZE_MAX_STEPS_PER_FRAME = 500;

  /**
   * Cap on {@code delayStride}: max undelayed {@code delay()} calls between FrameGate waits.
   * Without this, Bubble/Shaker at large n use multi-million strides and freeze the UI for seconds
   * per frame.
   */
  public static final int EQUALIZE_MAX_DELAY_STRIDE = 250_000;

  /**
   * Soft cap on undelayed equalize work (delay×stride) credited in one draw frame when catch-up
   * grants are enabled. Keeps large-n strided runs nearer the slider target without multi-second
   * freezes.
   */
  public static final int EQUALIZE_MAX_WORK_PER_FRAME = 1_000_000;

  /**
   * Approx bar-update budget per frame when batching {@code delayFrame} algorithms (e.g. Gravity
   * rewrites all {@code n} bars each beat). Cap beats/frame as {@code budget / n}.
   */
  public static final int EQUALIZE_FRAME_BEAT_ELEMENT_BUDGET = 250_000;

  /** Upper bound on batched frame-beats granted in one draw frame. */
  public static final int EQUALIZE_MAX_FRAME_BEATS_PER_FRAME = 64;

  // Visualization / canvas
  /** Default (dark) canvas clear gray — RGB {@code #0F0F0F}. */
  public static final int CANVAS_BACKGROUND_DARK = 15;

  /** Alternate canvas clear gray — RGB {@code #FFFFFF}. */
  public static final int CANVAS_BACKGROUND_WHITE = 255;

  public static final int RESULTS_TABLE_BACKGROUND = CANVAS_BACKGROUND_DARK;
  public static final int RESULTS_TABLE_TEXT_COLOR = CANVAS_BACKGROUND_WHITE;
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

  /**
   * Lower-bound wall time for {@code frameBeats} whole-frame paces ({@code delayFrame}), assuming
   * {@link #TARGET_FRAME_RATE}.
   */
  public static float equalizeFrameFloorSec(int frameBeats) {
    if (frameBeats <= 0) {
      return 0f;
    }
    return frameBeats / (float) TARGET_FRAME_RATE;
  }

  /**
   * Effective equalize target: slider duration, but never shorter than the {@code delayFrame}
   * floor.
   */
  public static float effectiveEqualizeTargetSec(float sliderTargetSec, int frameBeats) {
    return Math.max(Math.max(0f, sliderTargetSec), equalizeFrameFloorSec(frameBeats));
  }

  /**
   * Steps to grant this frame so {@code totalSteps} complete in {@code effectiveTargetSec}. Used
   * when equalize-sort-duration mode is active (schedule correction may pass remaining steps/time).
   */
  public static int equalizedSortStepsForDelta(
      float deltaSeconds, int totalSteps, float effectiveTargetSec) {
    if (totalSteps <= 0) {
      return 1;
    }
    if (deltaSeconds <= 0f || effectiveTargetSec <= 0f) {
      return 1;
    }
    return Math.max(1, Math.round(totalSteps * deltaSeconds / effectiveTargetSec));
  }

  /**
   * Max {@code delayFrame} beats to batch into one published frame for an array of {@code
   * arrayLength}, keeping roughly {@link #EQUALIZE_FRAME_BEAT_ELEMENT_BUDGET} element updates per
   * frame.
   */
  public static int equalizeMaxFrameBeatsPerFrame(int arrayLength) {
    int n = Math.max(1, arrayLength);
    int byBudget = EQUALIZE_FRAME_BEAT_ELEMENT_BUDGET / n;
    return Math.max(1, Math.min(EQUALIZE_MAX_FRAME_BEATS_PER_FRAME, byBudget));
  }

  // Text positioning (design px at {@link #WINDOW_RATIO_WIDTH}; use {@link #scaleToWidth})
  public static final float TEXT_X_OFFSET = 5.0f;
  public static final float TEXT_Y_OFFSET = 23.0f;
  public static final float LINE_HEIGHT_OFFSET = 20.0f;
  public static final float WATERMARK_TEXT_SIZE = 25.0f;
  public static final float PERF_TEXT_SIZE = 16.0f;
  public static final float PERF_LINE_HEIGHT = 18.0f;
  public static final float PERF_MARGIN = 8.0f;
  public static final float TABLE_COLUMN_WIDTH_RATIO = 1.0f / 7.0f;
  public static final float TABLE_TOP_ROW = 50.0f;
  public static final float TABLE_CELL_PADDING = 10.0f;
}
