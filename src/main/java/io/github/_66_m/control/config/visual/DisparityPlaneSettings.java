package io.github._66_m.control.config.visual;

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

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<DisparityPlaneSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          DisparityPlaneSettings.class,
          new SettingsSchema.DoubleParam(
              "planeScale",
              "Scene scale",
              "LAYOUT",
              PLANE_SCALE_MIN,
              PLANE_SCALE_MAX,
              DEFAULT_PLANE_SCALE,
              "%.2f"),
          new SettingsSchema.DoubleParam(
              "rotationSpeedRadPerSec",
              "Rotation speed",
              "LAYOUT",
              ROTATION_SPEED_RAD_PER_SEC_MIN,
              ROTATION_SPEED_RAD_PER_SEC_MAX,
              DEFAULT_ROTATION_SPEED_RAD_PER_SEC,
              "%.2f rad/s"),
          new SettingsSchema.DoubleParam(
              "maxExtrusionFraction",
              "Max extrusion",
              "LAYOUT",
              MAX_EXTRUSION_FRACTION_MIN,
              MAX_EXTRUSION_FRACTION_MAX,
              DEFAULT_MAX_EXTRUSION_FRACTION,
              "%.2f"),
          new SettingsSchema.DoubleParam(
              "tileGap",
              "Tile gap",
              "LAYOUT",
              TILE_GAP_MIN,
              TILE_GAP_MAX,
              DEFAULT_TILE_GAP,
              "%.2f"));

  public static DisparityPlaneSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
