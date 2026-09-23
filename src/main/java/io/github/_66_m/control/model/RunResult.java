package io.github._66_m.control.model;

/**
 * Metrics of one algorithm that finished in a sorting session.
 *
 * @param algorithmName display name of the algorithm
 * @param elements array size the algorithm reported ({@code alternativeSize})
 * @param comparisons counted comparisons
 * @param realTimeNanos behind-the-scenes algorithm time in nanoseconds (excludes visual pacing)
 * @param swaps counted swaps
 * @param writesMain writes to the main array
 * @param writesAux writes to auxiliary arrays
 * @param elapsedSeconds seconds since the session started when the algorithm finished
 */
public record RunResult(
    String algorithmName,
    int elements,
    long comparisons,
    double realTimeNanos,
    long swaps,
    long writesMain,
    long writesAux,
    int elapsedSeconds) {}
