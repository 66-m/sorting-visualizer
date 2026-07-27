package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `scatter-plot-linked` visualization. */
public record ScatterPlotLinkedSettings(double lineThickness) implements VisualizationSettings {

  public static final String ID = "scatter-plot-linked";

  public static final double DEFAULT_LINE_THICKNESS = 1.0;
  public static final double LINE_THICKNESS_MIN = 0.5;
  public static final double LINE_THICKNESS_MAX = 4.0;

  public ScatterPlotLinkedSettings {
    lineThickness = Numbers.clamp(lineThickness, LINE_THICKNESS_MIN, LINE_THICKNESS_MAX);
  }

  public static ScatterPlotLinkedSettings defaults() {
    return new ScatterPlotLinkedSettings(DEFAULT_LINE_THICKNESS);
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
