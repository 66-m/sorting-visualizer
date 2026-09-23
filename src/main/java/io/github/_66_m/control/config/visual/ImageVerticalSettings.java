package io.github._66_m.control.config.visual;

/** Tunable parameters for the `image-vertical` visualization. */
public record ImageVerticalSettings(FitMode fitMode, double highlightStrength)
    implements VisualizationSettings {

  public static final String ID = "image-vertical";

  public enum FitMode {
    STRETCH,
    CONTAIN
  }

  public static final FitMode DEFAULT_FIT_MODE = FitMode.STRETCH;
  public static final double DEFAULT_HIGHLIGHT_STRENGTH = 1.0;
  public static final double HIGHLIGHT_STRENGTH_MIN = 0.0;
  public static final double HIGHLIGHT_STRENGTH_MAX = 1.0;

  public ImageVerticalSettings {
    fitMode = fitMode == null ? DEFAULT_FIT_MODE : fitMode;
    highlightStrength =
        Numbers.clamp(highlightStrength, HIGHLIGHT_STRENGTH_MIN, HIGHLIGHT_STRENGTH_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<ImageVerticalSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          ImageVerticalSettings.class,
          new SettingsSchema.EnumParam("fitMode", "Fit mode", "IMAGE", DEFAULT_FIT_MODE),
          new SettingsSchema.DoubleParam(
              "highlightStrength",
              "Highlight strength",
              "IMAGE",
              HIGHLIGHT_STRENGTH_MIN,
              HIGHLIGHT_STRENGTH_MAX,
              DEFAULT_HIGHLIGHT_STRENGTH,
              "%.2f"));

  public static ImageVerticalSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
