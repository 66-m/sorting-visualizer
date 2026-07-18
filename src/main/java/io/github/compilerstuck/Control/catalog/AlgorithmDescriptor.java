package io.github.compilerstuck.Control.catalog;

import io.github.compilerstuck.Control.model.ArrayModel;
import io.github.compilerstuck.Control.render.ProcessingContext;
import io.github.compilerstuck.SortingAlgorithms.SortingAlgorithm;

import java.util.function.BiFunction;

/**
 * Describes an available sorting algorithm and how to instantiate it against
 * a given {@link ArrayModel} and {@link ProcessingContext} (used for delays).
 */
public record AlgorithmDescriptor(String id, String displayName,
                                   BiFunction<ArrayModel, ProcessingContext, SortingAlgorithm> factory) {
}
