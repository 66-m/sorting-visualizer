package io.github.compilerstuck.control.model;

import io.github.compilerstuck.visual.Marker;

/**
 * Copy-on-publish bridge: sort worker mutates {@link ArrayController}; render thread reads {@link
 * #publishedView()}. Call {@link #publish} only while the worker is idle (e.g. after {@link
 * FrameGate#awaitIdle()}).
 */
public final class SnapshotPublisher {
  private int[] valuesBuf = new int[0];
  private Marker[] markersBuf = new Marker[0];
  private int length;
  private long revision;
  private final PublishedView publishedView = new PublishedView();

  /** Read-only {@link ArrayModel} for visuals / sound. Stable across frames until next publish. */
  public ArrayModel publishedView() {
    return publishedView;
  }

  /**
   * Copy working values + markers into reusable buffers, then clear markers on {@code working}.
   * Must not run concurrently with working mutations.
   */
  public void publish(ArrayController working) {
    if (working == null) {
      return;
    }
    int n = working.getLength();
    ensureCapacity(n);
    System.arraycopy(working.getArray(), 0, valuesBuf, 0, n);
    for (int i = 0; i < n; i++) {
      markersBuf[i] = working.getMarker(i);
    }
    length = n;
    revision = working.getVisualRevision();
    working.resetMarkers();
  }

  private void ensureCapacity(int n) {
    if (valuesBuf.length < n) {
      valuesBuf = new int[n];
      markersBuf = new Marker[n];
    }
  }

  /** Package-visible for tests. */
  int bufferCapacity() {
    return valuesBuf.length;
  }

  private final class PublishedView implements ArrayModel {
    @Override
    public int getLength() {
      return length;
    }

    @Override
    public int get(int index) {
      return valuesBuf[index];
    }

    @Override
    public Marker getMarker(int index) {
      Marker m = markersBuf[index];
      return m != null ? m : Marker.NORMAL;
    }

    @Override
    public long getVisualRevision() {
      return revision;
    }

    @Override
    public int[] getArray() {
      return valuesBuf;
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
        if (valuesBuf[i - 1] > valuesBuf[i]) {
          return false;
        }
      }
      return true;
    }
  }

  private static UnsupportedOperationException unsupported() {
    return new UnsupportedOperationException("Published array view is read-only");
  }
}
