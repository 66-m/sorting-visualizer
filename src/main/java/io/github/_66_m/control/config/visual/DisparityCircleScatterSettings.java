package io.github._66_m.control.config.visual;

/** Tunable parameters for the `disparity-circle-scatter` visualization. */
public record DisparityCircleScatterSettings(
    double pointSize, double radiusScale, double startAngleDeg) implements VisualizationSettings {

  public static final String ID = "disparity-circle-scatter";

  public static final double DEFAULT_POINT_SIZE = 4.0;
  public static final double POINT_SIZE_MIN = 1.0;
  public static final double POINT_SIZE_MAX = 12.0;
  public static final double DEFAULT_RADIUS_SCALE = 1.0 / 2.4;
  public static final double RADIUS_SCALE_MIN = 0.15;
  public static final double RADIUS_SCALE_MAX = 0.5;
  public static final double DEFAULT_START_ANGLE_DEG = 0.0;
  public static final double START_ANGLE_DEG_MIN = 0.0;
  public static final double START_ANGLE_DEG_MAX = 360.0;

  public DisparityCircleScatterSettings {
    pointSize = Numbers.clamp(pointSize, POINT_SIZE_MIN, POINT_SIZE_MAX);
    radiusScale = Numbers.clamp(radiusScale, RADIUS_SCALE_MIN, RADIUS_SCALE_MAX);
    startAngleDeg = Numbers.clamp(startAngleDeg, START_ANGLE_DEG_MIN, START_ANGLE_DEG_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<DisparityCircleScatterSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          DisparityCircleScatterSettings.class,
          new SettingsSchema.DoubleParam(
              "radiusScale",
              "Radius",
              "LAYOUT",
              RADIUS_SCALE_MIN,
              RADIUS_SCALE_MAX,
              DEFAULT_RADIUS_SCALE,
              "%.2f"),
          new SettingsSchema.DoubleParam(
              "pointSize",
              "Point size",
              "LAYOUT",
              POINT_SIZE_MIN,
              POINT_SIZE_MAX,
              DEFAULT_POINT_SIZE,
              "%.1f"),
          new SettingsSchema.DoubleParam(
              "startAngleDeg",
              "Start angle",
              "LAYOUT",
              START_ANGLE_DEG_MIN,
              START_ANGLE_DEG_MAX,
              DEFAULT_START_ANGLE_DEG,
              "%.0f°"));

  public static DisparityCircleScatterSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
