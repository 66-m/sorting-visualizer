package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `color-gradient-graph` visualization. */
public record ColorGradientGraphSettings(boolean showIndexDividers)
    implements VisualizationSettings {

  public static final String ID = "color-gradient-graph";

  public static final boolean DEFAULT_SHOW_INDEX_DIVIDERS = false;

  public ColorGradientGraphSettings {
    // no clamps
  }

  public static ColorGradientGraphSettings defaults() {
    return new ColorGradientGraphSettings(DEFAULT_SHOW_INDEX_DIVIDERS);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
