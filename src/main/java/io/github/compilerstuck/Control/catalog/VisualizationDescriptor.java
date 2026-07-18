package io.github.compilerstuck.Control.catalog;

import io.github.compilerstuck.Control.model.ArrayModel;
import io.github.compilerstuck.Control.render.RenderContext;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import io.github.compilerstuck.Visual.Visualization;

/**
 * Describes an available visualization, its array-size constraints, and how
 * to instantiate it.
 */
public record VisualizationDescriptor(String id, String displayName, VisualConstraints constraints,
                                       VisualizationFactory factory) {

    @FunctionalInterface
    public interface VisualizationFactory {
        Visualization create(ArrayModel array, ColorGradient gradient, Sound sound, RenderContext renderContext);
    }
}
