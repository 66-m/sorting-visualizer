package io.github._66_m.control.catalog;

import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.render.DelayContext;
import io.github._66_m.sortingalgorithms.SortingAlgorithm;
import java.util.function.BiFunction;

/**
 * Describes an available sorting algorithm and how to instantiate it against a given {@link
 * ArrayModel} and {@link DelayContext} (used for delays).
 */
public record AlgorithmDescriptor(
    String id,
    String displayName,
    BiFunction<ArrayModel, DelayContext, SortingAlgorithm> factory) {}
