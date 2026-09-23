package io.github._66_m.control.config.visual;

/** Tunable parameters for the `phyllotaxis` visualization. */
public record PhyllotaxisSettings(double angleStepDeg, double scaleDivisor, double pointSize)
    implements VisualizationSettings {

  public static final String ID = "phyllotaxis";

  public static final double DEFAULT_ANGLE_STEP_DEG = 180.5;
  public static final double ANGLE_STEP_DEG_MIN = 137.5;
  public static final double ANGLE_STEP_DEG_MAX = 180.5;
  public static final double DEFAULT_SCALE_DIVISOR = 70.0;
  public static final double SCALE_DIVISOR_MIN = 40.0;
  public static final double SCALE_DIVISOR_MAX = 120.0;
  public static final double DEFAULT_POINT_SIZE = 5.0;
  public static final double POINT_SIZE_MIN = 1.0;
  public static final double POINT_SIZE_MAX = 12.0;

  public PhyllotaxisSettings {
    angleStepDeg = Numbers.clamp(angleStepDeg, ANGLE_STEP_DEG_MIN, ANGLE_STEP_DEG_MAX);
    scaleDivisor = Numbers.clamp(scaleDivisor, SCALE_DIVISOR_MIN, SCALE_DIVISOR_MAX);
    pointSize = Numbers.clamp(pointSize, POINT_SIZE_MIN, POINT_SIZE_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<PhyllotaxisSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          PhyllotaxisSettings.class,
          new SettingsSchema.DoubleParam(
              "scaleDivisor",
              "Scene scale",
              "LAYOUT",
              SCALE_DIVISOR_MIN,
              SCALE_DIVISOR_MAX,
              DEFAULT_SCALE_DIVISOR,
              "%.0f"),
          new SettingsSchema.DoubleParam(
              "angleStepDeg",
              "Angle step",
              "LAYOUT",
              ANGLE_STEP_DEG_MIN,
              ANGLE_STEP_DEG_MAX,
              DEFAULT_ANGLE_STEP_DEG,
              "%.1f°"),
          new SettingsSchema.DoubleParam(
              "pointSize",
              "Point size",
              "LAYOUT",
              POINT_SIZE_MIN,
              POINT_SIZE_MAX,
              DEFAULT_POINT_SIZE,
              "%.1f"));

  public static PhyllotaxisSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
