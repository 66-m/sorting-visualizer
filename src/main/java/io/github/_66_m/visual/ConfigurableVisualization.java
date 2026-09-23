package io.github._66_m.visual;

import io.github._66_m.control.config.visual.VisualizationSettings;

/** Optional per-visualization appearance settings (hot-applied without recreating the visual). */
public interface ConfigurableVisualization {

  VisualizationSettings currentSettings();

  /** Applies settings when the type matches this visualization; otherwise no-op. */
  void applySettings(VisualizationSettings settings);
}
