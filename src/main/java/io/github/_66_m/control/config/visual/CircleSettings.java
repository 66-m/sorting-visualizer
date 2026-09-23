package io.github._66_m.control.config.visual;

/** Tunable parameters for the `circle` visualization. */
public record CircleSettings(double radiusScale, double startAngleDeg, double lineThickness)
    implements VisualizationSettings {

  public static final String ID = "circle";

  public static final double DEFAULT_RADIUS_SCALE = 1.0 / 2.4;
  public static final double RADIUS_SCALE_MIN = 0.2;
  public static final double RADIUS_SCALE_MAX = 0.5;
  public static final double DEFAULT_START_ANGLE_DEG = 0.0;
  public static final double START_ANGLE_DEG_MIN = 0.0;
  public static final double START_ANGLE_DEG_MAX = 360.0;
  public static final double DEFAULT_LINE_THICKNESS = 1.0;
  public static final double LINE_THICKNESS_MIN = 0.5;
  public static final double LINE_THICKNESS_MAX = 4.0;

  public CircleSettings {
    radiusScale = Numbers.clamp(radiusScale, RADIUS_SCALE_MIN, RADIUS_SCALE_MAX);
    startAngleDeg = Numbers.clamp(startAngleDeg, START_ANGLE_DEG_MIN, START_ANGLE_DEG_MAX);
    lineThickness = Numbers.clamp(lineThickness, LINE_THICKNESS_MIN, LINE_THICKNESS_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<CircleSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          CircleSettings.class,
          new SettingsSchema.DoubleParam(
              "radiusScale",
              "Radius",
              "LAYOUT",
              RADIUS_SCALE_MIN,
              RADIUS_SCALE_MAX,
              DEFAULT_RADIUS_SCALE,
              "%.2f"),
          new SettingsSchema.DoubleParam(
              "startAngleDeg",
              "Start angle",
              "LAYOUT",
              START_ANGLE_DEG_MIN,
              START_ANGLE_DEG_MAX,
              DEFAULT_START_ANGLE_DEG,
              "%.0f°"),
          new SettingsSchema.DoubleParam(
              "lineThickness",
              "Line thickness",
              "LAYOUT",
              LINE_THICKNESS_MIN,
              LINE_THICKNESS_MAX,
              DEFAULT_LINE_THICKNESS,
              "%.2f"));

  public static CircleSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
