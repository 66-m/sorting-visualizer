package io.github._66_m.control.config.visual;

/** Tunable parameters for the `globe` visualization. */
public record GlobeSettings(
    Mode mode,
    Tiling tiling,
    boolean tileBorders,
    boolean lift,
    double liftHeight,
    double explode,
    Core core,
    double highlightStrength,
    double axialTiltDeg,
    double viewTiltDeg,
    double rotationSpeedRadPerSec,
    double sceneScale)
    implements VisualizationSettings {

  public static final String ID = "globe";

  public enum Mode {
    /** Latitude × longitude tiles; each tile shows the texture of its value's tile. */
    MOSAIC,
    /** Pole-to-pole wedges pushed outward by disparity, showing their value's texture. */
    WEDGES
  }

  public enum Tiling {
    /** Same number of tiles in every row (tiles shrink toward the poles). */
    EQUIRECT,
    /** Fewer tiles near the poles so all tiles cover about the same area. */
    EQUAL_AREA
  }

  /** Inner sphere visible through gaps. */
  public enum Core {
    NONE,
    DARK,
    MAGMA
  }

  public static final Mode DEFAULT_MODE = Mode.MOSAIC;
  public static final Tiling DEFAULT_TILING = Tiling.EQUAL_AREA;
  public static final boolean DEFAULT_TILE_BORDERS = false;
  public static final boolean DEFAULT_LIFT = true;
  public static final double DEFAULT_LIFT_HEIGHT = 0.15;
  public static final double DEFAULT_EXPLODE = 0.35;
  public static final Core DEFAULT_CORE = Core.NONE;
  public static final double DEFAULT_HIGHLIGHT_STRENGTH = 0.6;
  public static final double DEFAULT_AXIAL_TILT_DEG = 23.4;
  public static final double DEFAULT_VIEW_TILT_DEG = 10.0;
  public static final double DEFAULT_ROTATION_SPEED = 0.21;
  public static final double DEFAULT_SCENE_SCALE = 0.81;

  public GlobeSettings {
    mode = mode == null ? DEFAULT_MODE : mode;
    tiling = tiling == null ? DEFAULT_TILING : tiling;
    liftHeight = Numbers.clamp(liftHeight, 0.0, 0.5);
    explode = Numbers.clamp(explode, 0.0, 1.0);
    core = core == null ? DEFAULT_CORE : core;
    highlightStrength = Numbers.clamp(highlightStrength, 0.0, 1.0);
    axialTiltDeg = Numbers.clamp(axialTiltDeg, 0.0, 45.0);
    viewTiltDeg = Numbers.clamp(viewTiltDeg, -60.0, 60.0);
    rotationSpeedRadPerSec = Numbers.clamp(rotationSpeedRadPerSec, -2.0, 2.0);
    sceneScale = Numbers.clamp(sceneScale, 0.4, 1.0);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<GlobeSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          GlobeSettings.class,
          new SettingsSchema.EnumParam("mode", "Mode", "GLOBE", DEFAULT_MODE),
          new SettingsSchema.EnumParam("tiling", "Tiling", "GLOBE", DEFAULT_TILING),
          new SettingsSchema.BoolParam(
              "tileBorders", "Tile borders", "GLOBE", DEFAULT_TILE_BORDERS),
          new SettingsSchema.BoolParam("lift", "Lift tiles by disparity", "GLOBE", DEFAULT_LIFT),
          new SettingsSchema.DoubleParam(
              "liftHeight", "Lift height", "GLOBE", 0.0, 0.5, DEFAULT_LIFT_HEIGHT, "%.2f"),
          new SettingsSchema.DoubleParam(
              "explode", "Wedge explode", "GLOBE", 0.0, 1.0, DEFAULT_EXPLODE, "%.2f"),
          new SettingsSchema.EnumParam("core", "Core", "GLOBE", DEFAULT_CORE),
          new SettingsSchema.DoubleParam(
              "highlightStrength",
              "Highlight strength",
              "GLOBE",
              0.0,
              1.0,
              DEFAULT_HIGHLIGHT_STRENGTH,
              "%.2f"),
          new SettingsSchema.DoubleParam(
              "axialTiltDeg", "Axial tilt", "LAYOUT", 0.0, 45.0, DEFAULT_AXIAL_TILT_DEG, "%.1f°"),
          new SettingsSchema.DoubleParam(
              "viewTiltDeg", "View tilt", "LAYOUT", -60.0, 60.0, DEFAULT_VIEW_TILT_DEG, "%.0f°"),
          new SettingsSchema.DoubleParam(
              "rotationSpeedRadPerSec",
              "Rotation speed",
              "LAYOUT",
              -2.0,
              2.0,
              DEFAULT_ROTATION_SPEED,
              "%.2f rad/s"),
          new SettingsSchema.DoubleParam(
              "sceneScale", "Scene scale", "LAYOUT", 0.4, 1.0, DEFAULT_SCENE_SCALE, "%.2f"));

  public static GlobeSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
