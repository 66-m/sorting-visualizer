package io.github._66_m.control.config.visual;

/** Tunable parameters for the `disparity-circle-scatter-linked` visualization. */
public record DisparityCircleScatterLinkedSettings(double lineThickness, double radiusScale)
    implements VisualizationSettings {

  public static final String ID = "disparity-circle-scatter-linked";

  public static final double DEFAULT_LINE_THICKNESS = 1.0;
  public static final double LINE_THICKNESS_MIN = 0.5;
  public static final double LINE_THICKNESS_MAX = 4.0;
  public static final double DEFAULT_RADIUS_SCALE = 1.0 / 2.4;
  public static final double RADIUS_SCALE_MIN = 0.15;
  public static final double RADIUS_SCALE_MAX = 0.5;

  public DisparityCircleScatterLinkedSettings {
    lineThickness = Numbers.clamp(lineThickness, LINE_THICKNESS_MIN, LINE_THICKNESS_MAX);
    radiusScale = Numbers.clamp(radiusScale, RADIUS_SCALE_MIN, RADIUS_SCALE_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<DisparityCircleScatterLinkedSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          DisparityCircleScatterLinkedSettings.class,
          new SettingsSchema.DoubleParam(
              "radiusScale",
              "Radius",
              "LAYOUT",
              RADIUS_SCALE_MIN,
              RADIUS_SCALE_MAX,
              DEFAULT_RADIUS_SCALE,
              "%.2f"),
          new SettingsSchema.DoubleParam(
              "lineThickness",
              "Line thickness",
              "LAYOUT",
              LINE_THICKNESS_MIN,
              LINE_THICKNESS_MAX,
              DEFAULT_LINE_THICKNESS,
              "%.2f"));

  public static DisparityCircleScatterLinkedSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
