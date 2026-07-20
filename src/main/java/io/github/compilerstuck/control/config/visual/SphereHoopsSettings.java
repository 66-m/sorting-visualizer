package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `sphere-hoops` visualization. */
public record SphereHoopsSettings(double globeScale) implements VisualizationSettings {

  public static final String ID = "sphere-hoops";

  public static final double DEFAULT_GLOBE_SCALE = 1.0 / 1.5;
  public static final double GLOBE_SCALE_MIN = 0.4;
  public static final double GLOBE_SCALE_MAX = 0.9;

  public SphereHoopsSettings {
    globeScale = clamp(globeScale, GLOBE_SCALE_MIN, GLOBE_SCALE_MAX);
  }

  public static SphereHoopsSettings defaults() {
    return new SphereHoopsSettings(DEFAULT_GLOBE_SCALE);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
