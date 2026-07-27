package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `mosaic-squares` visualization. */
public record MosaicSquaresSettings(double tileGapPx) implements VisualizationSettings {

  public static final String ID = "mosaic-squares";

  public static final double DEFAULT_TILE_GAP_PX = 0.0;
  public static final double TILE_GAP_PX_MIN = 0.0;
  public static final double TILE_GAP_PX_MAX = 4.0;

  public MosaicSquaresSettings {
    tileGapPx = Numbers.clamp(tileGapPx, TILE_GAP_PX_MIN, TILE_GAP_PX_MAX);
  }

  public static MosaicSquaresSettings defaults() {
    return new MosaicSquaresSettings(DEFAULT_TILE_GAP_PX);
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
