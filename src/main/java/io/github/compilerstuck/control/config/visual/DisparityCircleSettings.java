package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `disparity-circle` visualization. */
public record DisparityCircleSettings(
    double radiusScale, double lineThickness, double startAngleDeg)
    implements VisualizationSettings {

  public static final String ID = "disparity-circle";

  public static final double DEFAULT_RADIUS_SCALE = 1.0 / 2.4;
  public static final double RADIUS_SCALE_MIN = 0.15;
  public static final double RADIUS_SCALE_MAX = 0.5;
  public static final double DEFAULT_LINE_THICKNESS = 1.0;
  public static final double LINE_THICKNESS_MIN = 0.5;
  public static final double LINE_THICKNESS_MAX = 4.0;
  public static final double DEFAULT_START_ANGLE_DEG = 0.0;
  public static final double START_ANGLE_DEG_MIN = 0.0;
  public static final double START_ANGLE_DEG_MAX = 360.0;

  public DisparityCircleSettings {
    radiusScale = clamp(radiusScale, RADIUS_SCALE_MIN, RADIUS_SCALE_MAX);
    lineThickness = clamp(lineThickness, LINE_THICKNESS_MIN, LINE_THICKNESS_MAX);
    startAngleDeg = clamp(startAngleDeg, START_ANGLE_DEG_MIN, START_ANGLE_DEG_MAX);
  }

  public static DisparityCircleSettings defaults() {
    return new DisparityCircleSettings(
        DEFAULT_RADIUS_SCALE, DEFAULT_LINE_THICKNESS, DEFAULT_START_ANGLE_DEG);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
