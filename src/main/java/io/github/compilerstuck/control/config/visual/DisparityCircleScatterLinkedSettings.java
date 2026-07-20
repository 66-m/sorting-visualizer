package io.github.compilerstuck.control.config.visual;

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
    lineThickness = clamp(lineThickness, LINE_THICKNESS_MIN, LINE_THICKNESS_MAX);
    radiusScale = clamp(radiusScale, RADIUS_SCALE_MIN, RADIUS_SCALE_MAX);
  }

  public static DisparityCircleScatterLinkedSettings defaults() {
    return new DisparityCircleScatterLinkedSettings(DEFAULT_LINE_THICKNESS, DEFAULT_RADIUS_SCALE);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
