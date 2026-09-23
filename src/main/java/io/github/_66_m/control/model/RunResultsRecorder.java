package io.github._66_m.control.model;

import io.github._66_m.sortingalgorithms.SortingAlgorithm;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Collects a {@link RunResult} per finished algorithm. Written by the sorting thread, read by the
 * render and UI threads.
 */
final class RunResultsRecorder {
  private final List<RunResult> results = new CopyOnWriteArrayList<>();

  /** Snapshots {@code array}'s metrics for {@code algorithm}, which just finished. */
  void record(SortingAlgorithm algorithm, ArrayController array, int elapsedSeconds) {
    results.add(
        new RunResult(
            algorithm.getName(),
            algorithm.getAlternativeSize(),
            array.getComparisons(),
            array.getRealTime(),
            array.getSwaps(),
            array.getWrites(),
            array.getWritesAux(),
            elapsedSeconds));
  }

  void clear() {
    results.clear();
  }

  boolean isEmpty() {
    return results.isEmpty();
  }

  /** Immutable snapshot in run order. */
  List<RunResult> results() {
    return List.copyOf(results);
  }
}
