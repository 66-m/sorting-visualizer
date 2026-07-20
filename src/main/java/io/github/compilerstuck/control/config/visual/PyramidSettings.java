package io.github.compilerstuck.control.config.visual;

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
        clamp(
            rotationSpeedRadPerSec, ROTATION_SPEED_RAD_PER_SEC_MIN, ROTATION_SPEED_RAD_PER_SEC_MAX);
    stackScale = clamp(stackScale, STACK_SCALE_MIN, STACK_SCALE_MAX);
  }

  public static PyramidSettings defaults() {
    return new PyramidSettings(DEFAULT_ROTATION_SPEED_RAD_PER_SEC, DEFAULT_STACK_SCALE);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
