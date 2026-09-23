package io.github._66_m.control.config.visual;

/** Tunable parameters for the `sphere-hoops` visualization. */
public record SphereHoopsSettings(double globeScale) implements VisualizationSettings {

  public static final String ID = "sphere-hoops";

  public static final double DEFAULT_GLOBE_SCALE = 1.0 / 1.5;
  public static final double GLOBE_SCALE_MIN = 0.4;
  public static final double GLOBE_SCALE_MAX = 0.9;

  public SphereHoopsSettings {
    globeScale = Numbers.clamp(globeScale, GLOBE_SCALE_MIN, GLOBE_SCALE_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<SphereHoopsSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          SphereHoopsSettings.class,
          new SettingsSchema.DoubleParam(
              "globeScale",
              "Scene scale",
              "LAYOUT",
              GLOBE_SCALE_MIN,
              GLOBE_SCALE_MAX,
              DEFAULT_GLOBE_SCALE,
              "%.2f"));

  public static SphereHoopsSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
