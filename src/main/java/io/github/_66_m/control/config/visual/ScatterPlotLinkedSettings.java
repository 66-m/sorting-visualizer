package io.github._66_m.control.config.visual;

/** Tunable parameters for the `scatter-plot-linked` visualization. */
public record ScatterPlotLinkedSettings(double lineThickness) implements VisualizationSettings {

  public static final String ID = "scatter-plot-linked";

  public static final double DEFAULT_LINE_THICKNESS = 1.0;
  public static final double LINE_THICKNESS_MIN = 0.5;
  public static final double LINE_THICKNESS_MAX = 4.0;

  public ScatterPlotLinkedSettings {
    lineThickness = Numbers.clamp(lineThickness, LINE_THICKNESS_MIN, LINE_THICKNESS_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<ScatterPlotLinkedSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          ScatterPlotLinkedSettings.class,
          new SettingsSchema.DoubleParam(
              "lineThickness",
              "Line thickness",
              "LAYOUT",
              LINE_THICKNESS_MIN,
              LINE_THICKNESS_MAX,
              DEFAULT_LINE_THICKNESS,
              "%.2f"));

  public static ScatterPlotLinkedSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
