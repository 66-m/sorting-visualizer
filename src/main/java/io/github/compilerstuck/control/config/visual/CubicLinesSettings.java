package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `cubic-lines` visualization. */
public record CubicLinesSettings(
    double rotationSpeedRadPerSec, double sceneScaleDivisor, double markerSize, int lineOpacity)
    implements VisualizationSettings {

  public static final String ID = "cubic-lines";

  public static final double DEFAULT_ROTATION_SPEED_RAD_PER_SEC = Math.PI / 10;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MIN = 0.0;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MAX = Math.PI;
  public static final double DEFAULT_SCENE_SCALE_DIVISOR = 3.5;
  public static final double SCENE_SCALE_DIVISOR_MIN = 1.5;
  public static final double SCENE_SCALE_DIVISOR_MAX = 8.0;
  public static final double DEFAULT_MARKER_SIZE = 2.0;
  public static final double MARKER_SIZE_MIN = 1.0;
  public static final double MARKER_SIZE_MAX = 8.0;
  public static final int DEFAULT_LINE_OPACITY = 255;
  public static final int LINE_OPACITY_MIN = 50;
  public static final int LINE_OPACITY_MAX = 255;

  public CubicLinesSettings {
    rotationSpeedRadPerSec =
        clamp(
            rotationSpeedRadPerSec, ROTATION_SPEED_RAD_PER_SEC_MIN, ROTATION_SPEED_RAD_PER_SEC_MAX);
    sceneScaleDivisor = clamp(sceneScaleDivisor, SCENE_SCALE_DIVISOR_MIN, SCENE_SCALE_DIVISOR_MAX);
    markerSize = clamp(markerSize, MARKER_SIZE_MIN, MARKER_SIZE_MAX);
    lineOpacity = (int) Math.round(clamp(lineOpacity, LINE_OPACITY_MIN, LINE_OPACITY_MAX));
  }

  public static CubicLinesSettings defaults() {
    return new CubicLinesSettings(
        DEFAULT_ROTATION_SPEED_RAD_PER_SEC,
        DEFAULT_SCENE_SCALE_DIVISOR,
        DEFAULT_MARKER_SIZE,
        DEFAULT_LINE_OPACITY);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
