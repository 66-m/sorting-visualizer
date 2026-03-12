package io.github.compilerstuck.Control.config;

/**
 * Strategy that decides whether a visualisation delay should fire for a given
 * sort step.
 *
 * <p>The default implementation ({@link #DEFAULT}) uses the probabilistic
 * approach already present in {@code SortingAlgorithm}: always delay for small
 * arrays, and delay proportionally otherwise, scaled by {@code delayFactor}.
 */
public interface DelayStrategy {

    /**
     * Returns {@code true} if a delay should be issued for the current step.
     *
     * @param arrayLength  the current array size
     * @param delayFactor  a scale factor in the range (0, 1]; 1.0 means always
     *                     delay when the other condition is satisfied
     */
    boolean shouldDelay(int arrayLength, double delayFactor);

    /** Threshold below which every element gets its own delay frame. */
    int DEFAULT_THRESHOLD = 2000;

    /**
     * The default probabilistic strategy: fires on every element for small
     * arrays; fires proportionally less often as array size grows past the
     * threshold, and always respects {@code delayFactor}.
     */
    DelayStrategy DEFAULT = (arrayLength, delayFactor) -> {
        boolean stepOk = arrayLength <= DEFAULT_THRESHOLD
                || Math.random() < (double) DEFAULT_THRESHOLD / arrayLength;
        boolean factorOk = delayFactor >= 1.0 || Math.random() < delayFactor;
        return stepOk && factorOk;
    };
}
