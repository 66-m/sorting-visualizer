package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `plane` visualization. */
public record PlaneSettings(double rotationSpeedRadPerSec, double planeScale, double tileGap)
    implements VisualizationSettings {

  public static final String ID = "plane";

  public static final double DEFAULT_ROTATION_SPEED_RAD_PER_SEC = Math.PI / 15;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MIN = 0.0;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MAX = Math.PI / 4;
  public static final double DEFAULT_PLANE_SCALE = 1.0 / 1.2;
  public static final double PLANE_SCALE_MIN = 0.4;
  public static final double PLANE_SCALE_MAX = 0.9;
  public static final double DEFAULT_TILE_GAP = 0.0;
  public static final double TILE_GAP_MIN = 0.0;
  public static final double TILE_GAP_MAX = 0.2;

  public PlaneSettings {
    rotationSpeedRadPerSec =
        clamp(
            rotationSpeedRadPerSec, ROTATION_SPEED_RAD_PER_SEC_MIN, ROTATION_SPEED_RAD_PER_SEC_MAX);
    planeScale = clamp(planeScale, PLANE_SCALE_MIN, PLANE_SCALE_MAX);
    tileGap = clamp(tileGap, TILE_GAP_MIN, TILE_GAP_MAX);
  }

  public static PlaneSettings defaults() {
    return new PlaneSettings(
        DEFAULT_ROTATION_SPEED_RAD_PER_SEC, DEFAULT_PLANE_SCALE, DEFAULT_TILE_GAP);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
