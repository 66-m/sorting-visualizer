package io.github._66_m.control.config.visual;

/** Tunable parameters for the `scatter-plot` visualization. */
public record ScatterPlotSettings(double pointSize) implements VisualizationSettings {

  public static final String ID = "scatter-plot";

  public static final double DEFAULT_POINT_SIZE = 3.0;
  public static final double POINT_SIZE_MIN = 1.0;
  public static final double POINT_SIZE_MAX = 12.0;

  public ScatterPlotSettings {
    pointSize = Numbers.clamp(pointSize, POINT_SIZE_MIN, POINT_SIZE_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<ScatterPlotSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          ScatterPlotSettings.class,
          new SettingsSchema.DoubleParam(
              "pointSize",
              "Point size",
              "LAYOUT",
              POINT_SIZE_MIN,
              POINT_SIZE_MAX,
              DEFAULT_POINT_SIZE,
              "%.1f"));

  public static ScatterPlotSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
