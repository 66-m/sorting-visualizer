package io.github.compilerstuck.control.catalog;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import io.github.compilerstuck.visual.Visualization;

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
