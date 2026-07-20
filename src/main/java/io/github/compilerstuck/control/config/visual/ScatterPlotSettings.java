package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `scatter-plot` visualization. */
public record ScatterPlotSettings(double pointSize) implements VisualizationSettings {

  public static final String ID = "scatter-plot";

  public static final double DEFAULT_POINT_SIZE = 3.0;
  public static final double POINT_SIZE_MIN = 1.0;
  public static final double POINT_SIZE_MAX = 12.0;

  public ScatterPlotSettings {
    pointSize = clamp(pointSize, POINT_SIZE_MIN, POINT_SIZE_MAX);
  }

  public static ScatterPlotSettings defaults() {
    return new ScatterPlotSettings(DEFAULT_POINT_SIZE);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
