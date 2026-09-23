package io.github._66_m.control.config.visual;

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
    rotationSpeedRadPerSec =
        Numbers.clamp(rotationSpeedRadPerSec, ROTATION_SPEED_MIN, ROTATION_SPEED_MAX);
    fillOpacity = (int) Math.round(Numbers.clamp(fillOpacity, FILL_OPACITY_MIN, FILL_OPACITY_MAX));
    sceneScaleDivisor =
        Numbers.clamp(sceneScaleDivisor, SCENE_SCALE_DIVISOR_MIN, SCENE_SCALE_DIVISOR_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<CubeSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          CubeSettings.class,
          new SettingsSchema.DoubleParam(
              "sceneScaleDivisor",
              "Scene scale",
              "LAYOUT",
              SCENE_SCALE_DIVISOR_MIN,
              SCENE_SCALE_DIVISOR_MAX,
              DEFAULT_SCENE_SCALE_DIVISOR,
              "%.2f"),
          new SettingsSchema.DoubleParam(
              "rotationSpeedRadPerSec",
              "Rotation speed",
              "MOTION",
              ROTATION_SPEED_MIN,
              ROTATION_SPEED_MAX,
              DEFAULT_ROTATION_SPEED,
              "%.2f rad/s"),
          new SettingsSchema.IntParam(
              "fillOpacity",
              "Fill opacity",
              "APPEARANCE",
              FILL_OPACITY_MIN,
              FILL_OPACITY_MAX,
              DEFAULT_FILL_OPACITY),
          new SettingsSchema.BoolParam(
              "wireframeEnabled", "Wireframe", "APPEARANCE", DEFAULT_WIREFRAME));

  public static CubeSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
