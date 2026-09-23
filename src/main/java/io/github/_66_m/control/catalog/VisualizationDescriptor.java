package io.github._66_m.control.catalog;

import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.render.RenderSystem;
import io.github._66_m.sound.Sound;
import io.github._66_m.visual.Visualization;
import io.github._66_m.visual.gradient.ColorGradient;

/** Describes an available visualization, its array-size constraints, and how to instantiate it. */
public record VisualizationDescriptor(
    String id, String displayName, VisualConstraints constraints, VisualizationFactory factory) {

  @FunctionalInterface
  public interface VisualizationFactory {
    Visualization create(ArrayModel array, ColorGradient gradient, Sound sound, RenderSystem rs);
  }
}
