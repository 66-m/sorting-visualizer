package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `spheric-disparity-lines` visualization. */
public record SphericDisparityLinesSettings(
    double rotationSpeedRadPerSec, double globeScale, int lineOpacity, double markerSize)
    implements VisualizationSettings {

  public static final String ID = "spheric-disparity-lines";

  public static final double DEFAULT_ROTATION_SPEED_RAD_PER_SEC = Math.PI / 10;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MIN = 0.0;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MAX = Math.PI / 2;
  public static final double DEFAULT_GLOBE_SCALE = 1.0 / 2.3;
  public static final double GLOBE_SCALE_MIN = 0.2;
  public static final double GLOBE_SCALE_MAX = 0.5;
  public static final int DEFAULT_LINE_OPACITY = 255;
  public static final int LINE_OPACITY_MIN = 50;
  public static final int LINE_OPACITY_MAX = 255;
  public static final double DEFAULT_MARKER_SIZE = 2.0;
  public static final double MARKER_SIZE_MIN = 1.0;
  public static final double MARKER_SIZE_MAX = 8.0;

  public SphericDisparityLinesSettings {
    rotationSpeedRadPerSec =
        clamp(
            rotationSpeedRadPerSec, ROTATION_SPEED_RAD_PER_SEC_MIN, ROTATION_SPEED_RAD_PER_SEC_MAX);
    globeScale = clamp(globeScale, GLOBE_SCALE_MIN, GLOBE_SCALE_MAX);
    lineOpacity = (int) Math.round(clamp(lineOpacity, LINE_OPACITY_MIN, LINE_OPACITY_MAX));
    markerSize = clamp(markerSize, MARKER_SIZE_MIN, MARKER_SIZE_MAX);
  }

  public static SphericDisparityLinesSettings defaults() {
    return new SphericDisparityLinesSettings(
        DEFAULT_ROTATION_SPEED_RAD_PER_SEC,
        DEFAULT_GLOBE_SCALE,
        DEFAULT_LINE_OPACITY,
        DEFAULT_MARKER_SIZE);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
