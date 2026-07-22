package io.github.compilerstuck.control.config.visual;

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

  public static DisparityCircleScatterSettings defaults() {
    return new DisparityCircleScatterSettings(
        DEFAULT_POINT_SIZE, DEFAULT_RADIUS_SCALE, DEFAULT_START_ANGLE_DEG);
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
