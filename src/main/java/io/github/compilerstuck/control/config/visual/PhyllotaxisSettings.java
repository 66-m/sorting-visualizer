package io.github.compilerstuck.control.config.visual;

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

  public static PhyllotaxisSettings defaults() {
    return new PhyllotaxisSettings(
        DEFAULT_ANGLE_STEP_DEG, DEFAULT_SCALE_DIVISOR, DEFAULT_POINT_SIZE);
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
