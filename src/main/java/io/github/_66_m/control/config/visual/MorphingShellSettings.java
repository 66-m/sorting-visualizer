package io.github._66_m.control.config.visual;

/** Tunable parameters for the `morphing-shell` visualization. */
public record MorphingShellSettings(
    double rotationSpeedRadPerSec, double sphereSize, double shellRadiusScale)
    implements VisualizationSettings {

  public static final String ID = "morphing-shell";

  public static final double DEFAULT_ROTATION_SPEED_RAD_PER_SEC = Math.PI / 10;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MIN = 0.0;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MAX = Math.PI / 2;
  public static final double DEFAULT_SPHERE_SIZE = 15.0;
  public static final double SPHERE_SIZE_MIN = 5.0;
  public static final double SPHERE_SIZE_MAX = 30.0;
  public static final double DEFAULT_SHELL_RADIUS_SCALE = 0.5;
  public static final double SHELL_RADIUS_SCALE_MIN = 0.25;
  public static final double SHELL_RADIUS_SCALE_MAX = 0.75;

  public MorphingShellSettings {
    rotationSpeedRadPerSec =
        Numbers.clamp(
            rotationSpeedRadPerSec, ROTATION_SPEED_RAD_PER_SEC_MIN, ROTATION_SPEED_RAD_PER_SEC_MAX);
    sphereSize = Numbers.clamp(sphereSize, SPHERE_SIZE_MIN, SPHERE_SIZE_MAX);
    shellRadiusScale =
        Numbers.clamp(shellRadiusScale, SHELL_RADIUS_SCALE_MIN, SHELL_RADIUS_SCALE_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<MorphingShellSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          MorphingShellSettings.class,
          new SettingsSchema.DoubleParam(
              "shellRadiusScale",
              "Scene scale",
              "LAYOUT",
              SHELL_RADIUS_SCALE_MIN,
              SHELL_RADIUS_SCALE_MAX,
              DEFAULT_SHELL_RADIUS_SCALE,
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
              "sphereSize",
              "Sphere size",
              "LAYOUT",
              SPHERE_SIZE_MIN,
              SPHERE_SIZE_MAX,
              DEFAULT_SPHERE_SIZE,
              "%.1f"));

  public static MorphingShellSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
