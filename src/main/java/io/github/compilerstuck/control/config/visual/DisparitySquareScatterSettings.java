package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `disparity-square-scatter` visualization. */
public record DisparitySquareScatterSettings(double pointSize, double perimeterScale)
    implements VisualizationSettings {

  public static final String ID = "disparity-square-scatter";

  public static final double DEFAULT_POINT_SIZE = 6.0;
  public static final double POINT_SIZE_MIN = 2.0;
  public static final double POINT_SIZE_MAX = 16.0;
  public static final double DEFAULT_PERIMETER_SCALE = 1.0 / 1.2;
  public static final double PERIMETER_SCALE_MIN = 0.4;
  public static final double PERIMETER_SCALE_MAX = 1.0;

  public DisparitySquareScatterSettings {
    pointSize = Numbers.clamp(pointSize, POINT_SIZE_MIN, POINT_SIZE_MAX);
    perimeterScale = Numbers.clamp(perimeterScale, PERIMETER_SCALE_MIN, PERIMETER_SCALE_MAX);
  }

  public static DisparitySquareScatterSettings defaults() {
    return new DisparitySquareScatterSettings(DEFAULT_POINT_SIZE, DEFAULT_PERIMETER_SCALE);
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
