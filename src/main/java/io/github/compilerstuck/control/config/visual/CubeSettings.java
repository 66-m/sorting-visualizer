package io.github.compilerstuck.control.config.visual;

/**
 * Tunable parameters for the Cube visualization. Defaults match the legacy hardcoded look in {@code
 * Cube}.
 */
public record CubeSettings(
    double rotationSpeedRadPerSec,
    int fillOpacity,
    boolean wireframeEnabled,
    double sceneScaleDivisor)
    implements VisualizationSettings {

  public static final String ID = "cube";

  public static final double DEFAULT_ROTATION_SPEED = Math.PI / 10;
  public static final int DEFAULT_FILL_OPACITY = 120;
  public static final boolean DEFAULT_WIREFRAME = true;
  public static final double DEFAULT_SCENE_SCALE_DIVISOR = 3.5;

  public static final double ROTATION_SPEED_MIN = 0.0;
  public static final double ROTATION_SPEED_MAX = Math.PI;
  public static final int FILL_OPACITY_MIN = 0;
  public static final int FILL_OPACITY_MAX = 254;
  public static final double SCENE_SCALE_DIVISOR_MIN = 1.5;
  public static final double SCENE_SCALE_DIVISOR_MAX = 8.0;

  public CubeSettings {
    rotationSpeedRadPerSec = clamp(rotationSpeedRadPerSec, ROTATION_SPEED_MIN, ROTATION_SPEED_MAX);
    fillOpacity = (int) Math.round(clamp(fillOpacity, FILL_OPACITY_MIN, FILL_OPACITY_MAX));
    sceneScaleDivisor = clamp(sceneScaleDivisor, SCENE_SCALE_DIVISOR_MIN, SCENE_SCALE_DIVISOR_MAX);
  }

  public static CubeSettings defaults() {
    return new CubeSettings(
        DEFAULT_ROTATION_SPEED,
        DEFAULT_FILL_OPACITY,
        DEFAULT_WIREFRAME,
        DEFAULT_SCENE_SCALE_DIVISOR);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
