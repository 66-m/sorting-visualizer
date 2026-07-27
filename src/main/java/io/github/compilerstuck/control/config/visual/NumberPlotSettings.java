package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `number-plot` visualization. */
public record NumberPlotSettings(double fontSize) implements VisualizationSettings {

  public static final String ID = "number-plot";

  public static final double DEFAULT_FONT_SIZE = 14.0;
  public static final double FONT_SIZE_MIN = 8.0;
  public static final double FONT_SIZE_MAX = 24.0;

  public NumberPlotSettings {
    fontSize = Numbers.clamp(fontSize, FONT_SIZE_MIN, FONT_SIZE_MAX);
  }

  public static NumberPlotSettings defaults() {
    return new NumberPlotSettings(DEFAULT_FONT_SIZE);
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
