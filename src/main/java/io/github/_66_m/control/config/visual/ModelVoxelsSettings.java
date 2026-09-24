package io.github._66_m.control.config.visual;

import io.github._66_m.control.config.visual.ModelOptions.AssemblyOrder;
import io.github._66_m.control.config.visual.ModelOptions.ColorSource;
import io.github._66_m.control.config.visual.ModelOptions.UpAxis;

/** Tunable parameters for the `model-voxels` visualization. */
public record ModelVoxelsSettings(
    Mode mode,
    AssemblyOrder order,
    Fill fill,
    Direction direction,
    ColorSource colorSource,
    UpAxis upAxis,
    double explode,
    double voxelGap,
    double sceneScale,
    double tiltDeg,
    double rotationSpeedRadPerSec)
    implements VisualizationSettings {

  public static final String ID = "model-voxels";

  /** What the element's value controls. */
  public enum Mode {
    SCATTER,
    COLOR,
    DISPARITY
  }

  /** Which cells become voxels. */
  public enum Fill {
    SHELL,
    SOLID
  }

  /** Where displaced voxels move in {@link Mode#DISPARITY}. */
  public enum Direction {
    /** Away from the model center (explosion). */
    RADIAL,
    /** Along the surface normal. */
    NORMAL,
    /** Down to the floor (melting). */
    DOWN
  }

  public static final Mode DEFAULT_MODE = Mode.DISPARITY;
  public static final AssemblyOrder DEFAULT_ORDER = AssemblyOrder.HEIGHT;
  public static final Fill DEFAULT_FILL = Fill.SHELL;
  public static final Direction DEFAULT_DIRECTION = Direction.RADIAL;
  public static final ColorSource DEFAULT_COLOR_SOURCE = ColorSource.MODEL;
  public static final UpAxis DEFAULT_UP_AXIS = UpAxis.Y_UP;
  public static final double DEFAULT_EXPLODE = 0.5;
  public static final double DEFAULT_VOXEL_GAP = 0.1;
  public static final double DEFAULT_SCENE_SCALE = 0.6;
  public static final double DEFAULT_TILT_DEG = 15.0;
  public static final double DEFAULT_ROTATION_SPEED = 0.3;

  public ModelVoxelsSettings {
    mode = mode == null ? DEFAULT_MODE : mode;
    order = order == null ? DEFAULT_ORDER : order;
    fill = fill == null ? DEFAULT_FILL : fill;
    direction = direction == null ? DEFAULT_DIRECTION : direction;
    colorSource = colorSource == null ? DEFAULT_COLOR_SOURCE : colorSource;
    upAxis = upAxis == null ? DEFAULT_UP_AXIS : upAxis;
    explode = Numbers.clamp(explode, 0.0, 2.0);
    voxelGap = Numbers.clamp(voxelGap, 0.0, 0.5);
    sceneScale = Numbers.clamp(sceneScale, 0.3, 1.0);
    tiltDeg = Numbers.clamp(tiltDeg, -90.0, 90.0);
    rotationSpeedRadPerSec = Numbers.clamp(rotationSpeedRadPerSec, -2.0, 2.0);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<ModelVoxelsSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          ModelVoxelsSettings.class,
          new SettingsSchema.EnumParam("mode", "Mode", "MODEL", DEFAULT_MODE),
          new SettingsSchema.EnumParam("order", "Assembly order", "MODEL", DEFAULT_ORDER),
          new SettingsSchema.EnumParam("fill", "Fill", "MODEL", DEFAULT_FILL),
          new SettingsSchema.EnumParam(
              "direction", "Disparity direction", "MODEL", DEFAULT_DIRECTION),
          new SettingsSchema.EnumParam("colorSource", "Colors", "MODEL", DEFAULT_COLOR_SOURCE),
          new SettingsSchema.EnumParam("upAxis", "Up axis", "MODEL", DEFAULT_UP_AXIS),
          new SettingsSchema.DoubleParam(
              "explode", "Explode distance", "LOOK", 0.0, 2.0, DEFAULT_EXPLODE, "%.2f"),
          new SettingsSchema.DoubleParam(
              "voxelGap", "Voxel gap", "LOOK", 0.0, 0.5, DEFAULT_VOXEL_GAP, "%.2f"),
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

  public static ModelVoxelsSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
