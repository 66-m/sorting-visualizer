package io.github.compilerstuck.Control;

import io.github.compilerstuck.Visual.Marker;

/**
 * Represents the model for a sortable array; the interface exposes only the
 * the operations required by sorting algorithms and instrumentation. By coding
 * against this interface we can later replace the implementation, add
 * listeners, or provide a mock for tests. The interface also exposes various
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
    void addSleepTime(double sleepTime);
    void addRealTime(double timeNs);

    // Measurement accessors ------------------------------------------------
    long getComparisons();
    long getSwaps();
    long getWrites();
    long getWritesAux();
    long getArrayAccesses();
    double getDelay();
    double getRealTime();
    double getSortedPercentage();
    int getSegments();

    /**
     * Quick check used by tests to verify correctness; semantics are the same as
     * {@code ArrayController.isSorted()}.  Implementations may scan the array.
     */
    boolean isSorted();

    /**
     * Return direct access to the underlying int[]; provided for algorithms that
     * require a bulk view.  Consumers should not modify the returned array
     * except via the ArrayModel API.
     */
    int[] getArray();
}
