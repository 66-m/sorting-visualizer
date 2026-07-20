package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `sphere` visualization. */
public record SphereSettings(double rotationSpeedRadPerSec, double globeScale, double pointSize)
    implements VisualizationSettings {

  public static final String ID = "sphere";

  public static final double DEFAULT_ROTATION_SPEED_RAD_PER_SEC = Math.PI / 10;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MIN = 0.0;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MAX = Math.PI / 2;
  public static final double DEFAULT_GLOBE_SCALE = 1.0 / 2.3;
  public static final double GLOBE_SCALE_MIN = 0.2;
  public static final double GLOBE_SCALE_MAX = 0.5;
  public static final double DEFAULT_POINT_SIZE = 3.0;
  public static final double POINT_SIZE_MIN = 1.0;
  public static final double POINT_SIZE_MAX = 10.0;

  public SphereSettings {
    rotationSpeedRadPerSec =
        clamp(
            rotationSpeedRadPerSec, ROTATION_SPEED_RAD_PER_SEC_MIN, ROTATION_SPEED_RAD_PER_SEC_MAX);
    globeScale = clamp(globeScale, GLOBE_SCALE_MIN, GLOBE_SCALE_MAX);
    pointSize = clamp(pointSize, POINT_SIZE_MIN, POINT_SIZE_MAX);
  }

  public static SphereSettings defaults() {
    return new SphereSettings(
        DEFAULT_ROTATION_SPEED_RAD_PER_SEC, DEFAULT_GLOBE_SCALE, DEFAULT_POINT_SIZE);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
