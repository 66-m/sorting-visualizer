package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `disparity-sphere-hoops` visualization. */
public record DisparitySphereHoopsSettings(double globeScale) implements VisualizationSettings {

  public static final String ID = "disparity-sphere-hoops";

  public static final double DEFAULT_GLOBE_SCALE = 1.0 / 1.1;
  public static final double GLOBE_SCALE_MIN = 0.5;
  public static final double GLOBE_SCALE_MAX = 1.0;

  public DisparitySphereHoopsSettings {
    globeScale = clamp(globeScale, GLOBE_SCALE_MIN, GLOBE_SCALE_MAX);
  }

  public static DisparitySphereHoopsSettings defaults() {
    return new DisparitySphereHoopsSettings(DEFAULT_GLOBE_SCALE);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
