package io.github._66_m.control.config.visual;

/** Tunable parameters for the `color-gradient-graph` visualization. */
public record ColorGradientGraphSettings(boolean showIndexDividers)
    implements VisualizationSettings {

  public static final String ID = "color-gradient-graph";

  public static final boolean DEFAULT_SHOW_INDEX_DIVIDERS = false;

  public ColorGradientGraphSettings {
    // no clamps
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<ColorGradientGraphSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          ColorGradientGraphSettings.class,
          new SettingsSchema.BoolParam(
              "showIndexDividers", "Index dividers", "DISPLAY", DEFAULT_SHOW_INDEX_DIVIDERS));

  public static ColorGradientGraphSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
