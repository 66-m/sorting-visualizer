package io.github.compilerstuck.control.shuffle;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.visual.Marker;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Delegates to a live model while recording every {@link #swap(int, int)} as flat {@code i,j}. */
public final class SwapRecordingModel implements ArrayModel {
  private final ArrayModel delegate;
  private final List<Integer> swapPairs;

  public SwapRecordingModel(ArrayModel delegate, List<Integer> swapPairs) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    this.swapPairs = Objects.requireNonNull(swapPairs, "swapPairs");
  }

  public int[] swapPairsArray() {
    int n = swapPairs.size();
    int[] out = new int[n];
    for (int i = 0; i < n; i++) {
      out[i] = swapPairs.get(i);
    }
    return out;
  }

  public static List<Integer> newPairList() {
    return new ArrayList<>();
  }

  @Override
  public int getLength() {
    return delegate.getLength();
  }

  @Override
  public int get(int index) {
    return delegate.get(index);
  }

  @Override
  public void set(int index, int value) {
    delegate.set(index, value);
  }

  @Override
  public void swap(int i, int j) {
    swapPairs.add(i);
    swapPairs.add(j);
    delegate.swap(i, j);
  }

  @Override
  public Marker getMarker(int index) {
    return delegate.getMarker(index);
  }

  @Override
  public void setMarker(int index, Marker m) {
    delegate.setMarker(index, m);
  }

  @Override
  public void addComparisons(int n) {
    delegate.addComparisons(n);
  }

  @Override
  public void addWritesAux(int n) {
    delegate.addWritesAux(n);
  }

  @Override
  public void addRealTime(double timeNs) {
    delegate.addRealTime(timeNs);
  }

  @Override
  public long getComparisons() {
    return delegate.getComparisons();
  }

  @Override
  public long getSwaps() {
    return delegate.getSwaps();
  }

  @Override
  public long getWrites() {
    return delegate.getWrites();
  }

  @Override
  public long getWritesAux() {
    return delegate.getWritesAux();
  }

  @Override
  public double getRealTime() {
    return delegate.getRealTime();
  }

  @Override
  public double getSortedPercentage() {
    return delegate.getSortedPercentage();
  }

  @Override
  public int getSegments() {
    return delegate.getSegments();
  }

  @Override
  public boolean isSorted() {
    return delegate.isSorted();
  }

  @Override
  public int[] getArray() {
    return delegate.getArray();
  }

  @Override
  public long getVisualRevision() {
    return delegate.getVisualRevision();
  }
}
