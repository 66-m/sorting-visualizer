package io.github.compilerstuck.control.config;

import java.util.Random;

/**
 * Strategy that decides whether a visualisation delay should fire for a given sort step.
 *
 * <p>The default implementation ({@link #DEFAULT}) uses the probabilistic approach already present
 * in {@code SortingAlgorithm}: always delay for small arrays, and delay proportionally otherwise,
 * scaled by {@code delayFactor}. Prefer {@link #random(Random)} in tests for deterministic
 * behaviour.
 */
public interface DelayStrategy {

  /**
   * Returns {@code true} if a delay should be issued for the current step.
   *
   * @param arrayLength the current array size
   * @param delayFactor a scale factor in the range (0, 1]; 1.0 means always delay when the other
   *     condition is satisfied
   */
  boolean shouldDelay(int arrayLength, double delayFactor);

  /** Threshold below which every element gets its own delay frame. */
  int DEFAULT_THRESHOLD = 2000;

  /**
   * The default probabilistic strategy backed by an internal {@link Random}. Prefer {@link
   * #random(Random)} in tests.
   */
  DelayStrategy DEFAULT = random(new Random());

  /** Always delay — used by the FrameGate step engine so each delay() consumes a credit. */
  DelayStrategy ALWAYS = (arrayLength, delayFactor) -> true;

  /** Never delay — useful for tests that want to disable probabilistic delays. */
  static DelayStrategy never() {
    return (arrayLength, delayFactor) -> false;
  }

  /**
   * Probabilistic strategy: fires on every element for small arrays; fires proportionally less
   * often as array size grows past the threshold, and always respects {@code delayFactor}.
   */
  static DelayStrategy random(Random rng) {
    return (arrayLength, delayFactor) -> {
      boolean stepOk =
          arrayLength <= DEFAULT_THRESHOLD
              || rng.nextDouble() < (double) DEFAULT_THRESHOLD / arrayLength;
      boolean factorOk = delayFactor >= 1.0 || rng.nextDouble() < delayFactor;
      return stepOk && factorOk;
    };
  }
}
