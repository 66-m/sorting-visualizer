package io.github._66_m.control.config.visual;

/** Tunable parameters for the `number-plot` visualization. */
public record NumberPlotSettings(double fontSize) implements VisualizationSettings {

  public static final String ID = "number-plot";

  public static final double DEFAULT_FONT_SIZE = 14.0;
  public static final double FONT_SIZE_MIN = 8.0;
  public static final double FONT_SIZE_MAX = 24.0;

  public NumberPlotSettings {
    fontSize = Numbers.clamp(fontSize, FONT_SIZE_MIN, FONT_SIZE_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<NumberPlotSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          NumberPlotSettings.class,
          new SettingsSchema.DoubleParam(
              "fontSize",
              "Font size",
              "DISPLAY",
              FONT_SIZE_MIN,
              FONT_SIZE_MAX,
              DEFAULT_FONT_SIZE,
              "%.0f"));

  public static NumberPlotSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
