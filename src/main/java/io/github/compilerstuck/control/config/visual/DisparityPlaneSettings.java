package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `disparity-plane` visualization. */
public record DisparityPlaneSettings(
    double rotationSpeedRadPerSec, double maxExtrusionFraction, double planeScale, double tileGap)
    implements VisualizationSettings {

  public static final String ID = "disparity-plane";

  public static final double DEFAULT_ROTATION_SPEED_RAD_PER_SEC = Math.PI / 15;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MIN = 0.0;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MAX = Math.PI / 4;
  public static final double DEFAULT_MAX_EXTRUSION_FRACTION = 0.25;
  public static final double MAX_EXTRUSION_FRACTION_MIN = 0.125;
  public static final double MAX_EXTRUSION_FRACTION_MAX = 0.5;
  public static final double DEFAULT_PLANE_SCALE = 1.0 / 1.2;
  public static final double PLANE_SCALE_MIN = 0.4;
  public static final double PLANE_SCALE_MAX = 0.9;
  public static final double DEFAULT_TILE_GAP = 0.0;
  public static final double TILE_GAP_MIN = 0.0;
  public static final double TILE_GAP_MAX = 0.2;

  public DisparityPlaneSettings {
    rotationSpeedRadPerSec =
        Numbers.clamp(
            rotationSpeedRadPerSec, ROTATION_SPEED_RAD_PER_SEC_MIN, ROTATION_SPEED_RAD_PER_SEC_MAX);
    maxExtrusionFraction =
        Numbers.clamp(maxExtrusionFraction, MAX_EXTRUSION_FRACTION_MIN, MAX_EXTRUSION_FRACTION_MAX);
    planeScale = Numbers.clamp(planeScale, PLANE_SCALE_MIN, PLANE_SCALE_MAX);
    tileGap = Numbers.clamp(tileGap, TILE_GAP_MIN, TILE_GAP_MAX);
  }

  public static DisparityPlaneSettings defaults() {
    return new DisparityPlaneSettings(
        DEFAULT_ROTATION_SPEED_RAD_PER_SEC,
        DEFAULT_MAX_EXTRUSION_FRACTION,
        DEFAULT_PLANE_SCALE,
        DEFAULT_TILE_GAP);
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
