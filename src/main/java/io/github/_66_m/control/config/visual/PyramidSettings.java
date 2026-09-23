package io.github._66_m.control.config.visual;

/** Tunable parameters for the `pyramid` visualization. */
public record PyramidSettings(double rotationSpeedRadPerSec, double stackScale)
    implements VisualizationSettings {

  public static final String ID = "pyramid";

  public static final double DEFAULT_ROTATION_SPEED_RAD_PER_SEC = Math.PI / 15;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MIN = 0.0;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MAX = Math.PI / 4;
  public static final double DEFAULT_STACK_SCALE = 1.0 / 1.7;
  public static final double STACK_SCALE_MIN = 0.3;
  public static final double STACK_SCALE_MAX = 0.7;

  public PyramidSettings {
    rotationSpeedRadPerSec =
        Numbers.clamp(
            rotationSpeedRadPerSec, ROTATION_SPEED_RAD_PER_SEC_MIN, ROTATION_SPEED_RAD_PER_SEC_MAX);
    stackScale = Numbers.clamp(stackScale, STACK_SCALE_MIN, STACK_SCALE_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<PyramidSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          PyramidSettings.class,
          new SettingsSchema.DoubleParam(
              "stackScale",
              "Scene scale",
              "LAYOUT",
              STACK_SCALE_MIN,
              STACK_SCALE_MAX,
              DEFAULT_STACK_SCALE,
              "%.2f"),
          new SettingsSchema.DoubleParam(
              "rotationSpeedRadPerSec",
              "Rotation speed",
              "LAYOUT",
              ROTATION_SPEED_RAD_PER_SEC_MIN,
              ROTATION_SPEED_RAD_PER_SEC_MAX,
              DEFAULT_ROTATION_SPEED_RAD_PER_SEC,
              "%.2f rad/s"));

  public static PyramidSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
