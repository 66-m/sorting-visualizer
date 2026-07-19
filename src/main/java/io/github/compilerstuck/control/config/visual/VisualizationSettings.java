package io.github.compilerstuck.control.config.visual;

/** Per-visualization appearance settings. New visuals add a permitted record type. */
public sealed interface VisualizationSettings permits CubeSettings {

  String visualizationId();
}
