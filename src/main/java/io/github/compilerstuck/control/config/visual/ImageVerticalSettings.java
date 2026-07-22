package io.github.compilerstuck.control.config.visual;

/** Tunable parameters for the `image-vertical` visualization. */
public record ImageVerticalSettings(FitMode fitMode, double highlightStrength)
    implements VisualizationSettings {

  public static final String ID = "image-vertical";

  public enum FitMode {
    STRETCH,
    CONTAIN
  }

  public static final FitMode DEFAULT_FIT_MODE = FitMode.STRETCH;
  public static final double DEFAULT_HIGHLIGHT_STRENGTH = 1.0;
  public static final double HIGHLIGHT_STRENGTH_MIN = 0.0;
  public static final double HIGHLIGHT_STRENGTH_MAX = 1.0;

  public ImageVerticalSettings {
    fitMode = fitMode == null ? DEFAULT_FIT_MODE : fitMode;
    highlightStrength =
        Numbers.clamp(highlightStrength, HIGHLIGHT_STRENGTH_MIN, HIGHLIGHT_STRENGTH_MAX);
  }

  public static ImageVerticalSettings defaults() {
    return new ImageVerticalSettings(DEFAULT_FIT_MODE, DEFAULT_HIGHLIGHT_STRENGTH);
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
