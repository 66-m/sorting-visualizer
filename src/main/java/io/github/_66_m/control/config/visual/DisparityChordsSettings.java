package io.github._66_m.control.config.visual;

/** Tunable parameters for the `disparity-chords` visualization. */
public record DisparityChordsSettings(
    double radiusScale, double lineThickness, double coincidentMarkerSize, int chordOpacity)
    implements VisualizationSettings {

  public static final String ID = "disparity-chords";

  public static final double DEFAULT_RADIUS_SCALE = 1.0 / 2.4;
  public static final double RADIUS_SCALE_MIN = 0.15;
  public static final double RADIUS_SCALE_MAX = 0.5;
  public static final double DEFAULT_LINE_THICKNESS = 1.0;
  public static final double LINE_THICKNESS_MIN = 0.5;
  public static final double LINE_THICKNESS_MAX = 4.0;
  public static final double DEFAULT_COINCIDENT_MARKER_SIZE = 1.5;
  public static final double COINCIDENT_MARKER_SIZE_MIN = 1.5;
  public static final double COINCIDENT_MARKER_SIZE_MAX = 8.0;
  public static final int DEFAULT_CHORD_OPACITY = 255;
  public static final int CHORD_OPACITY_MIN = 50;
  public static final int CHORD_OPACITY_MAX = 255;

  public DisparityChordsSettings {
    radiusScale = Numbers.clamp(radiusScale, RADIUS_SCALE_MIN, RADIUS_SCALE_MAX);
    lineThickness = Numbers.clamp(lineThickness, LINE_THICKNESS_MIN, LINE_THICKNESS_MAX);
    coincidentMarkerSize =
        Numbers.clamp(coincidentMarkerSize, COINCIDENT_MARKER_SIZE_MIN, COINCIDENT_MARKER_SIZE_MAX);
    chordOpacity =
        (int) Math.round(Numbers.clamp(chordOpacity, CHORD_OPACITY_MIN, CHORD_OPACITY_MAX));
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<DisparityChordsSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          DisparityChordsSettings.class,
          new SettingsSchema.DoubleParam(
              "radiusScale",
              "Radius",
              "LAYOUT",
              RADIUS_SCALE_MIN,
              RADIUS_SCALE_MAX,
              DEFAULT_RADIUS_SCALE,
              "%.2f"),
          new SettingsSchema.DoubleParam(
              "lineThickness",
              "Line thickness",
              "LAYOUT",
              LINE_THICKNESS_MIN,
              LINE_THICKNESS_MAX,
              DEFAULT_LINE_THICKNESS,
              "%.2f"),
          new SettingsSchema.DoubleParam(
              "coincidentMarkerSize",
              "Marker size",
              "LAYOUT",
              COINCIDENT_MARKER_SIZE_MIN,
              COINCIDENT_MARKER_SIZE_MAX,
              DEFAULT_COINCIDENT_MARKER_SIZE,
              "%.1f"),
          new SettingsSchema.IntParam(
              "chordOpacity",
              "Chord opacity",
              "LAYOUT",
              CHORD_OPACITY_MIN,
              CHORD_OPACITY_MAX,
              DEFAULT_CHORD_OPACITY));

  public static DisparityChordsSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
