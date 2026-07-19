package io.github.compilerstuck.control.catalog;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.DelayContext;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import java.util.function.BiFunction;

/**
 * Describes an available sorting algorithm and how to instantiate it against a given {@link
 * ArrayModel} and {@link DelayContext} (used for delays).
 */
public record AlgorithmDescriptor(
    String id,
    String displayName,
    BiFunction<ArrayModel, DelayContext, SortingAlgorithm> factory) {}
