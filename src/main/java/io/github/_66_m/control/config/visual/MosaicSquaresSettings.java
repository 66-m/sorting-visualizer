package io.github._66_m.control.config.visual;

/** Tunable parameters for the `mosaic-squares` visualization. */
public record MosaicSquaresSettings(double tileGapPx) implements VisualizationSettings {

  public static final String ID = "mosaic-squares";

  public static final double DEFAULT_TILE_GAP_PX = 0.0;
  public static final double TILE_GAP_PX_MIN = 0.0;
  public static final double TILE_GAP_PX_MAX = 4.0;

  public MosaicSquaresSettings {
    tileGapPx = Numbers.clamp(tileGapPx, TILE_GAP_PX_MIN, TILE_GAP_PX_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<MosaicSquaresSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          MosaicSquaresSettings.class,
          new SettingsSchema.DoubleParam(
              "tileGapPx",
              "Tile gap",
              "LAYOUT",
              TILE_GAP_PX_MIN,
              TILE_GAP_PX_MAX,
              DEFAULT_TILE_GAP_PX,
              "%.1f"));

  public static MosaicSquaresSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
