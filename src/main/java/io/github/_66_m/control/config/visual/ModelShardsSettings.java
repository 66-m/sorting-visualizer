package io.github._66_m.control.config.visual;

import io.github._66_m.control.config.visual.ModelOptions.AssemblyOrder;
import io.github._66_m.control.config.visual.ModelOptions.ColorSource;
import io.github._66_m.control.config.visual.ModelOptions.UpAxis;

/** Tunable parameters for the `model-shards` visualization. */
public record ModelShardsSettings(
    AssemblyOrder order,
    int pieceCount,
    ColorSource colorSource,
    UpAxis upAxis,
    double explode,
    double maxSpinDeg,
    double sceneScale,
    double tiltDeg,
    double rotationSpeedRadPerSec)
    implements VisualizationSettings {

  public static final String ID = "model-shards";

  /** {@code 0} = one shard per element, capped at {@link #PIECES_MAX}. */
  public static final int PIECES_AUTO = 0;

  public static final int PIECES_MAX = 4096;
  public static final AssemblyOrder DEFAULT_ORDER = AssemblyOrder.RADIAL;
  public static final int DEFAULT_PIECE_COUNT = 512;
  public static final ColorSource DEFAULT_COLOR_SOURCE = ColorSource.MODEL;
  public static final UpAxis DEFAULT_UP_AXIS = UpAxis.Y_UP;
  public static final double DEFAULT_EXPLODE = 0.6;
  public static final double DEFAULT_MAX_SPIN_DEG = 180.0;
  public static final double DEFAULT_SCENE_SCALE = 0.6;
  public static final double DEFAULT_TILT_DEG = 15.0;
  public static final double DEFAULT_ROTATION_SPEED = 0.3;

  public ModelShardsSettings {
    order = order == null ? DEFAULT_ORDER : order;
    pieceCount = Math.max(PIECES_AUTO, Math.min(PIECES_MAX, pieceCount));
    colorSource = colorSource == null ? DEFAULT_COLOR_SOURCE : colorSource;
    upAxis = upAxis == null ? DEFAULT_UP_AXIS : upAxis;
    explode = Numbers.clamp(explode, 0.0, 2.0);
    maxSpinDeg = Numbers.clamp(maxSpinDeg, 0.0, 720.0);
    sceneScale = Numbers.clamp(sceneScale, 0.3, 1.0);
    tiltDeg = Numbers.clamp(tiltDeg, -90.0, 90.0);
    rotationSpeedRadPerSec = Numbers.clamp(rotationSpeedRadPerSec, -2.0, 2.0);
  }

  /** Shards to cut for an array of {@code length} elements. */
  public int effectivePieces(int length) {
    int wanted = pieceCount == PIECES_AUTO ? length : pieceCount;
    return Math.max(1, Math.min(PIECES_MAX, wanted));
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<ModelShardsSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          ModelShardsSettings.class,
          new SettingsSchema.EnumParam("order", "Assembly order", "MODEL", DEFAULT_ORDER),
          new SettingsSchema.IntParam(
              "pieceCount",
              "Shards (0 = one per element)",
              "MODEL",
              PIECES_AUTO,
              PIECES_MAX,
              DEFAULT_PIECE_COUNT),
          new SettingsSchema.EnumParam("colorSource", "Colors", "MODEL", DEFAULT_COLOR_SOURCE),
          new SettingsSchema.EnumParam("upAxis", "Up axis", "MODEL", DEFAULT_UP_AXIS),
          new SettingsSchema.DoubleParam(
              "explode", "Explode distance", "LOOK", 0.0, 2.0, DEFAULT_EXPLODE, "%.2f"),
          new SettingsSchema.DoubleParam(
              "maxSpinDeg", "Max shard spin", "LOOK", 0.0, 720.0, DEFAULT_MAX_SPIN_DEG, "%.0f°"),
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

  public static ModelShardsSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
