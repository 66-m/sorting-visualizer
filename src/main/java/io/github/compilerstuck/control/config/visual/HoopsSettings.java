package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `hoops` visualization. */
public record HoopsSettings(double radiusScale) implements VisualizationSettings {

  public static final String ID = "hoops";

  public static final double DEFAULT_RADIUS_SCALE = 1.0 / 1.1;
  public static final double RADIUS_SCALE_MIN = 0.5;
  public static final double RADIUS_SCALE_MAX = 1.0;

  public HoopsSettings {
    radiusScale = clamp(radiusScale, RADIUS_SCALE_MIN, RADIUS_SCALE_MAX);
  }

  public static HoopsSettings defaults() {
    return new HoopsSettings(DEFAULT_RADIUS_SCALE);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
