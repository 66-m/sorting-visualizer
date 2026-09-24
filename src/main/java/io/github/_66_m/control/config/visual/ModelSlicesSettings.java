package io.github._66_m.control.config.visual;

import io.github._66_m.control.config.visual.ModelOptions.ColorSource;
import io.github._66_m.control.config.visual.ModelOptions.UpAxis;

/** Tunable parameters for the `model-slices` visualization. */
public record ModelSlicesSettings(
    Axis axis,
    int pieceCount,
    ColorSource colorSource,
    UpAxis upAxis,
    double sliceGap,
    double sceneScale,
    double tiltDeg,
    double rotationSpeedRadPerSec)
    implements VisualizationSettings {

  public static final String ID = "model-slices";

  /** Slicing direction in model space (after the up-axis fix). */
  public enum Axis {
    X,
    Y,
    Z
  }

  /** {@code 0} = one slice per element, capped at {@link #PIECES_MAX}. */
  public static final int PIECES_AUTO = 0;

  public static final int PIECES_MAX = 4096;
  public static final Axis DEFAULT_AXIS = Axis.Y;
  public static final int DEFAULT_PIECE_COUNT = PIECES_AUTO;
  public static final ColorSource DEFAULT_COLOR_SOURCE = ColorSource.MODEL;
  public static final UpAxis DEFAULT_UP_AXIS = UpAxis.Y_UP;
  public static final double DEFAULT_SLICE_GAP = 0.0;
  public static final double DEFAULT_SCENE_SCALE = 0.7;
  public static final double DEFAULT_TILT_DEG = 15.0;
  public static final double DEFAULT_ROTATION_SPEED = 0.3;

  public ModelSlicesSettings {
    axis = axis == null ? DEFAULT_AXIS : axis;
    pieceCount = Math.max(PIECES_AUTO, Math.min(PIECES_MAX, pieceCount));
    colorSource = colorSource == null ? DEFAULT_COLOR_SOURCE : colorSource;
    upAxis = upAxis == null ? DEFAULT_UP_AXIS : upAxis;
    sliceGap = Numbers.clamp(sliceGap, 0.0, 2.0);
    sceneScale = Numbers.clamp(sceneScale, 0.3, 1.0);
    tiltDeg = Numbers.clamp(tiltDeg, -90.0, 90.0);
    rotationSpeedRadPerSec = Numbers.clamp(rotationSpeedRadPerSec, -2.0, 2.0);
  }

  /** Slices to cut for an array of {@code length} elements. */
  public int effectivePieces(int length) {
    int wanted = pieceCount == PIECES_AUTO ? length : pieceCount;
    return Math.max(1, Math.min(PIECES_MAX, wanted));
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<ModelSlicesSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          ModelSlicesSettings.class,
          new SettingsSchema.EnumParam("axis", "Slice axis", "MODEL", DEFAULT_AXIS),
          new SettingsSchema.IntParam(
              "pieceCount",
              "Slices (0 = one per element)",
              "MODEL",
              PIECES_AUTO,
              PIECES_MAX,
              DEFAULT_PIECE_COUNT),
          new SettingsSchema.EnumParam("colorSource", "Colors", "MODEL", DEFAULT_COLOR_SOURCE),
          new SettingsSchema.EnumParam("upAxis", "Up axis", "MODEL", DEFAULT_UP_AXIS),
          new SettingsSchema.DoubleParam(
              "sliceGap", "Slice gap", "LOOK", 0.0, 2.0, DEFAULT_SLICE_GAP, "%.2f"),
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

  public static ModelSlicesSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
