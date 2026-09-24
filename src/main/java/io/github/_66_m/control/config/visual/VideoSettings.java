package io.github._66_m.control.config.visual;

/** Tunable parameters for the `video` visualization. */
public record VideoSettings(
    Layout layout,
    FitMode fitMode,
    boolean gridLines,
    double highlightStrength,
    Playback playback,
    double soundtrackVolume,
    double sortToneVolume)
    implements VisualizationSettings {

  public static final String ID = "video";

  /** How the frame is cut into elements. */
  public enum Layout {
    COLUMNS,
    ROWS,
    GRID
  }

  public enum FitMode {
    CONTAIN,
    STRETCH
  }

  /** When the video plays. */
  public enum Playback {
    /** Restarts with every Run and stops when the session ends. */
    SYNC_WITH_RUN,
    /** Plays continuously, looping at the end. */
    LOOP
  }

  public static final Layout DEFAULT_LAYOUT = Layout.COLUMNS;
  public static final FitMode DEFAULT_FIT_MODE = FitMode.CONTAIN;
  public static final boolean DEFAULT_GRID_LINES = false;
  public static final double DEFAULT_HIGHLIGHT_STRENGTH = 0.6;
  public static final Playback DEFAULT_PLAYBACK = Playback.SYNC_WITH_RUN;
  public static final double DEFAULT_SOUNDTRACK_VOLUME = 0.8;
  public static final double DEFAULT_SORT_TONE_VOLUME = 0.4;

  public VideoSettings {
    layout = layout == null ? DEFAULT_LAYOUT : layout;
    fitMode = fitMode == null ? DEFAULT_FIT_MODE : fitMode;
    highlightStrength = Numbers.clamp(highlightStrength, 0.0, 1.0);
    playback = playback == null ? DEFAULT_PLAYBACK : playback;
    soundtrackVolume = Numbers.clamp(soundtrackVolume, 0.0, 1.0);
    sortToneVolume = Numbers.clamp(sortToneVolume, 0.0, 1.0);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<VideoSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          VideoSettings.class,
          new SettingsSchema.EnumParam("layout", "Layout", "VIDEO", DEFAULT_LAYOUT),
          new SettingsSchema.EnumParam("fitMode", "Fit mode", "VIDEO", DEFAULT_FIT_MODE),
          new SettingsSchema.BoolParam("gridLines", "Grid lines", "VIDEO", DEFAULT_GRID_LINES),
          new SettingsSchema.DoubleParam(
              "highlightStrength",
              "Highlight strength",
              "VIDEO",
              0.0,
              1.0,
              DEFAULT_HIGHLIGHT_STRENGTH,
              "%.2f"),
          new SettingsSchema.EnumParam("playback", "Playback", "PLAYBACK", DEFAULT_PLAYBACK),
          new SettingsSchema.DoubleParam(
              "soundtrackVolume",
              "Soundtrack volume",
              "PLAYBACK",
              0.0,
              1.0,
              DEFAULT_SOUNDTRACK_VOLUME,
              "%.2f"),
          new SettingsSchema.DoubleParam(
              "sortToneVolume",
              "Sort tone volume",
              "PLAYBACK",
              0.0,
              1.0,
              DEFAULT_SORT_TONE_VOLUME,
              "%.2f"));

  public static VideoSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
