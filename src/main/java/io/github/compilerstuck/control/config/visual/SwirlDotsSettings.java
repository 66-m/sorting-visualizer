package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `swirl-dots` visualization. */
public record SwirlDotsSettings(double spiralTurns, double radiusScale, double pointSize)
    implements VisualizationSettings {

  public static final String ID = "swirl-dots";

  public static final double DEFAULT_SPIRAL_TURNS = 8.0;
  public static final double SPIRAL_TURNS_MIN = 2.0;
  public static final double SPIRAL_TURNS_MAX = 16.0;
  public static final double DEFAULT_RADIUS_SCALE = 1.0 / 2.5;
  public static final double RADIUS_SCALE_MIN = 0.2;
  public static final double RADIUS_SCALE_MAX = 0.5;
  public static final double DEFAULT_POINT_SIZE = 5.0;
  public static final double POINT_SIZE_MIN = 1.0;
  public static final double POINT_SIZE_MAX = 12.0;

  public SwirlDotsSettings {
    spiralTurns = Numbers.clamp(spiralTurns, SPIRAL_TURNS_MIN, SPIRAL_TURNS_MAX);
    radiusScale = Numbers.clamp(radiusScale, RADIUS_SCALE_MIN, RADIUS_SCALE_MAX);
    pointSize = Numbers.clamp(pointSize, POINT_SIZE_MIN, POINT_SIZE_MAX);
  }

  public static SwirlDotsSettings defaults() {
    return new SwirlDotsSettings(DEFAULT_SPIRAL_TURNS, DEFAULT_RADIUS_SCALE, DEFAULT_POINT_SIZE);
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
