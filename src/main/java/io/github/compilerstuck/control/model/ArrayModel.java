package io.github.compilerstuck.control.model;

import io.github.compilerstuck.visual.Marker;

/**
 * Represents the model for a sortable array; the interface exposes only the operations required by
 * sorting algorithms and instrumentation. By coding against this interface we can later replace the
 * implementation, add listeners, or provide a mock for tests. The interface also exposes various
 * measurement accessors used by the UI and test harness.
 */
public interface ArrayModel {
  int getLength();

  int get(int index);

  void set(int index, int value);

  void swap(int i, int j);

  Marker getMarker(int index);

  void setMarker(int index, Marker m);

  /* statistics */
  void addComparisons(int n);

  void addWritesAux(int n);

  /** Adds a slice of behind-the-scenes sort work time in nanoseconds (excludes visual pacing). */
  void addRealTime(double timeNs);

  // Measurement accessors ------------------------------------------------
  long getComparisons();

  long getSwaps();

  long getWrites();

  long getWritesAux();

  /**
   * Accumulated behind-the-scenes sort time in nanoseconds up to this point (algorithm work only;
   * FrameGate waits are excluded).
   */
  double getRealTime();

  double getSortedPercentage();

  int getSegments();

  /**
   * Quick check used by tests to verify correctness; semantics are the same as {@code
   * ArrayController.isSorted()}. Implementations may scan the array.
   */
  boolean isSorted();

  /**
   * Live mutable backing store for algorithms that need a bulk view and for snapshot publishers
   * that copy values. Algorithms may read/write this array directly for performance; visuals and
   * sound must use the published snapshot view, not this method.
   */
  int[] getArray();

  /**
   * Monotonic counter bumped when array contents change ({@code set}/{@code swap}/resize/reset) or
   * when a marker value changes. Visuals can use this to skip idle recomputation. Setting a marker
   * to the same value it already has does not bump the counter.
   */
  long getVisualRevision();
}
