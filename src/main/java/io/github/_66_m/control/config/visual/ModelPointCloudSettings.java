package io.github._66_m.control.config.visual;

import io.github._66_m.control.config.visual.ModelOptions.AssemblyOrder;
import io.github._66_m.control.config.visual.ModelOptions.ColorSource;
import io.github._66_m.control.config.visual.ModelOptions.UpAxis;

/** Tunable parameters for the `model-points` visualization. */
public record ModelPointCloudSettings(
    Mode mode,
    AssemblyOrder order,
    ColorSource colorSource,
    UpAxis upAxis,
    double dotSize,
    double explode,
    double sceneScale,
    double tiltDeg,
    double rotationSpeedRadPerSec)
    implements VisualizationSettings {

  public static final String ID = "model-points";

  /** What the element's value controls. */
  public enum Mode {
    /** Position along the assembly axis comes from the value (3D scatter plot). */
    SCATTER,
    /** Dots stay in place; their color comes from the value's sample. */
    COLOR,
    /** Dots sit at the value's home, pushed outward along the surface normal by disparity. */
    DISPARITY
  }

  public static final Mode DEFAULT_MODE = Mode.DISPARITY;
  public static final AssemblyOrder DEFAULT_ORDER = AssemblyOrder.HEIGHT;
  public static final ColorSource DEFAULT_COLOR_SOURCE = ColorSource.MODEL;
  public static final UpAxis DEFAULT_UP_AXIS = UpAxis.Y_UP;
  public static final double DEFAULT_DOT_SIZE = 2.5;
  public static final double DEFAULT_EXPLODE = 0.5;
  public static final double DEFAULT_SCENE_SCALE = 0.6;
  public static final double DEFAULT_TILT_DEG = 15.0;
  public static final double DEFAULT_ROTATION_SPEED = 0.3;

  public ModelPointCloudSettings {
    mode = mode == null ? DEFAULT_MODE : mode;
    order = order == null ? DEFAULT_ORDER : order;
    colorSource = colorSource == null ? DEFAULT_COLOR_SOURCE : colorSource;
    upAxis = upAxis == null ? DEFAULT_UP_AXIS : upAxis;
    dotSize = Numbers.clamp(dotSize, 0.5, 8.0);
    explode = Numbers.clamp(explode, 0.0, 2.0);
    sceneScale = Numbers.clamp(sceneScale, 0.3, 1.0);
    tiltDeg = Numbers.clamp(tiltDeg, -90.0, 90.0);
    rotationSpeedRadPerSec = Numbers.clamp(rotationSpeedRadPerSec, -2.0, 2.0);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<ModelPointCloudSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          ModelPointCloudSettings.class,
          new SettingsSchema.EnumParam("mode", "Mode", "MODEL", DEFAULT_MODE),
          new SettingsSchema.EnumParam("order", "Assembly order", "MODEL", DEFAULT_ORDER),
          new SettingsSchema.EnumParam("colorSource", "Colors", "MODEL", DEFAULT_COLOR_SOURCE),
          new SettingsSchema.EnumParam("upAxis", "Up axis", "MODEL", DEFAULT_UP_AXIS),
          new SettingsSchema.DoubleParam(
              "dotSize", "Dot size", "LOOK", 0.5, 8.0, DEFAULT_DOT_SIZE, "%.1f"),
          new SettingsSchema.DoubleParam(
              "explode", "Explode distance", "LOOK", 0.0, 2.0, DEFAULT_EXPLODE, "%.2f"),
          new SettingsSchema.DoubleParam(
              "sceneScale", "Scene scale", "LAYOUT", 0.3, 1.0, DEFAULT_SCENE_SCALE, "%.2f"),
          new SettingsSchema.DoubleParam(
              "tiltDeg", "Tilt", "LAYOUT", -90.0, 90.0, DEFAULT_TILT_DEG, "%.0f°"),
          new SettingsSchema.DoubleParam(
              "rotationSpeedRadPerSec",
              "Rotation speed",
              "LAYOUT",
              -2.0,
              2.0,
              DEFAULT_ROTATION_SPEED,
              "%.2f rad/s"));

  public static ModelPointCloudSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
