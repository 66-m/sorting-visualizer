package io.github._66_m.control.config.visual;

/** Tunable parameters for the `hoops` visualization. */
public record HoopsSettings(double radiusScale) implements VisualizationSettings {

  public static final String ID = "hoops";

  public static final double DEFAULT_RADIUS_SCALE = 1.0 / 1.1;
  public static final double RADIUS_SCALE_MIN = 0.5;
  public static final double RADIUS_SCALE_MAX = 1.0;

  public HoopsSettings {
    radiusScale = Numbers.clamp(radiusScale, RADIUS_SCALE_MIN, RADIUS_SCALE_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<HoopsSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          HoopsSettings.class,
          new SettingsSchema.DoubleParam(
              "radiusScale",
              "Radius",
              "LAYOUT",
              RADIUS_SCALE_MIN,
              RADIUS_SCALE_MAX,
              DEFAULT_RADIUS_SCALE,
              "%.3f"));

  public static HoopsSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
