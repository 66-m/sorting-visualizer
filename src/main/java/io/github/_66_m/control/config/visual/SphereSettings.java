package io.github._66_m.control.config.visual;

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
        Numbers.clamp(
            rotationSpeedRadPerSec, ROTATION_SPEED_RAD_PER_SEC_MIN, ROTATION_SPEED_RAD_PER_SEC_MAX);
    globeScale = Numbers.clamp(globeScale, GLOBE_SCALE_MIN, GLOBE_SCALE_MAX);
    pointSize = Numbers.clamp(pointSize, POINT_SIZE_MIN, POINT_SIZE_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<SphereSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          SphereSettings.class,
          new SettingsSchema.DoubleParam(
              "globeScale",
              "Scene scale",
              "LAYOUT",
              GLOBE_SCALE_MIN,
              GLOBE_SCALE_MAX,
              DEFAULT_GLOBE_SCALE,
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
              "pointSize",
              "Point size",
              "LAYOUT",
              POINT_SIZE_MIN,
              POINT_SIZE_MAX,
              DEFAULT_POINT_SIZE,
              "%.1f"));

  public static SphereSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
