package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `image-horizontal` visualization. */
public record ImageHorizontalSettings(FitMode fitMode, double highlightStrength)
    implements VisualizationSettings {

  public static final String ID = "image-horizontal";

  public enum FitMode {
    STRETCH,
    CONTAIN
  }

  public static final FitMode DEFAULT_FIT_MODE = FitMode.STRETCH;
  public static final double DEFAULT_HIGHLIGHT_STRENGTH = 1.0;
  public static final double HIGHLIGHT_STRENGTH_MIN = 0.0;
  public static final double HIGHLIGHT_STRENGTH_MAX = 1.0;

  public ImageHorizontalSettings {
    fitMode = fitMode == null ? DEFAULT_FIT_MODE : fitMode;
    highlightStrength = clamp(highlightStrength, HIGHLIGHT_STRENGTH_MIN, HIGHLIGHT_STRENGTH_MAX);
  }

  public static ImageHorizontalSettings defaults() {
    return new ImageHorizontalSettings(DEFAULT_FIT_MODE, DEFAULT_HIGHLIGHT_STRENGTH);
  }

  @Override
  public String visualizationId() {
    return ID;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
