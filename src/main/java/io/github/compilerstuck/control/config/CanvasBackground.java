package io.github.compilerstuck.control.config;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Canvas clear color choice: near-black default or pure white. */
public enum CanvasBackground {
  DARK("Dark", AppConfig.CANVAS_BACKGROUND_DARK, AppConfig.CANVAS_BACKGROUND_WHITE),
  WHITE("White", AppConfig.CANVAS_BACKGROUND_WHITE, AppConfig.CANVAS_BACKGROUND_DARK);

  private static final Logger LOGGER = Logger.getLogger(CanvasBackground.class.getName());

  private final String label;
  private final int clearGray;
  private final int overlayTextGray;

  CanvasBackground(String label, int clearGray, int overlayTextGray) {
    this.label = label;
    this.clearGray = clearGray;
    this.overlayTextGray = overlayTextGray;
  }

  public String label() {
    return label;
  }

  /** 0–255 gray channel used for {@code clear(r,g,b)}. */
  public int clearGray() {
    return clearGray;
  }

  public float clearComponent() {
    return clearGray / 255f;
  }

  /** 0–255 gray channel for HUD / results overlay text and grid lines. */
  public int overlayTextGray() {
    return overlayTextGray;
  }

  public boolean isLight() {
    return this == WHITE;
  }

  /**
   * When the canvas is light, opaque-white {@code Marker.SET} colors remapped to this so highlights
   * stay visible.
   */
  public Color markerSetFallback() {
    int g = AppConfig.CANVAS_BACKGROUND_DARK;
    return new Color(g, g, g);
  }

  @Override
  public String toString() {
    return label;
  }

  public static CanvasBackground fromName(String raw) {
    if (raw == null || raw.isBlank()) {
      return SettingsDefaults.DEFAULT_CANVAS_BACKGROUND;
    }
    try {
      return CanvasBackground.valueOf(raw.trim());
    } catch (IllegalArgumentException ex) {
      LOGGER.log(Level.WARNING, "Unknown canvasBackground ''{0}'', using default", raw);
      return SettingsDefaults.DEFAULT_CANVAS_BACKGROUND;
    }
  }
}
