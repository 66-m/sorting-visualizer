package io.github.compilerstuck.control.model;

import io.github.compilerstuck.visual.Marker;

/**
 * Frozen copy of array values + markers for the render thread. Mutators throw; metrics are not
 * meaningful (HUD reads the live {@link ArrayController}).
 */
public final class ArraySnapshot implements ArrayModel {
  private final int length;
  private final int[] values;
  private final Marker[] markers;
  private final long revision;

  public ArraySnapshot(int length, int[] values, Marker[] markers, long revision) {
    this.length = length;
    this.values = values;
    this.markers = markers;
    this.revision = revision;
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public int get(int index) {
    return values[index];
  }

  @Override
  public Marker getMarker(int index) {
    return markers[index];
  }

  @Override
  public long getVisualRevision() {
    return revision;
  }

  /**
   * Returns the snapshot values buffer. Do not mutate — owned by {@link SnapshotPublisher} and
   * reused across publishes.
   */
  @Override
  public int[] getArray() {
    return values;
  }

  @Override
  public void set(int index, int value) {
    throw unsupported();
  }

  @Override
  public void swap(int i, int j) {
    throw unsupported();
  }

  @Override
  public void setMarker(int index, Marker m) {
    throw unsupported();
  }

  @Override
  public void addComparisons(int n) {
    throw unsupported();
  }

  @Override
  public void addWritesAux(int n) {
    throw unsupported();
  }

  @Override
  public void addSleepTime(double sleepTime) {
    throw unsupported();
  }

  @Override
  public void addRealTime(double timeNs) {
    throw unsupported();
  }

  @Override
  public long getComparisons() {
    return 0;
  }

  @Override
  public long getSwaps() {
    return 0;
  }

  @Override
  public long getWrites() {
    return 0;
  }

  @Override
  public long getWritesAux() {
    return 0;
  }

  @Override
  public double getDelay() {
    return 0;
  }

  @Override
  public double getRealTime() {
    return 0;
  }

  @Override
  public double getSortedPercentage() {
    return 0;
  }

  @Override
  public int getSegments() {
    return 0;
  }

  @Override
  public boolean isSorted() {
    for (int i = 1; i < length; i++) {
      if (values[i - 1] > values[i]) {
        return false;
      }
    }
    return true;
  }

  private static UnsupportedOperationException unsupported() {
    return new UnsupportedOperationException("ArraySnapshot is read-only");
  }
}
