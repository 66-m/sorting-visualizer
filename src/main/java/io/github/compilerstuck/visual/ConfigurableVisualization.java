package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.VisualizationSettings;

/** Optional per-visualization appearance settings (hot-applied without recreating the visual). */
public interface ConfigurableVisualization {

  VisualizationSettings currentSettings();

  /** Applies settings when the type matches this visualization; otherwise no-op. */
  void applySettings(VisualizationSettings settings);
}
