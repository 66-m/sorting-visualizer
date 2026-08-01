package io.github.compilerstuck.control.model;

import io.github.compilerstuck.control.config.ShuffleStrategy;
import io.github.compilerstuck.control.config.ShuffleType;
import io.github.compilerstuck.control.render.DelayContext;
import io.github.compilerstuck.control.shuffle.AlmostSortedShuffleStrategy;
import io.github.compilerstuck.control.shuffle.RandomShuffleStrategy;
import io.github.compilerstuck.control.shuffle.RecordedShuffle;
import io.github.compilerstuck.control.shuffle.ReverseShuffleStrategy;
import io.github.compilerstuck.control.shuffle.SortedShuffleStrategy;
import io.github.compilerstuck.control.shuffle.SwapRecordingModel;
import io.github.compilerstuck.visual.Marker;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ArrayController implements ArrayModel {
  private int[] array;
  private Marker[] markers;
  private int length;

  private long comparisons;
  private long swaps;
  private long writes;
  private long writesAux;
  private double sortedPercentage;
  private int segments;

  /** When true, {@link #update()} must recompute sorted% / segments. */
  private volatile boolean metricsDirty = true;

  /** Bumped on content mutations and {@link Marker#SET}; see {@link #getVisualRevision()}. */
  private final AtomicLong visualRevision = new AtomicLong();

  private double realTime;

  private ShuffleType shuffleType = ShuffleType.RANDOM;
  private ShuffleStrategy shuffleStrategy;
  private OperationReporter operationReporter = OperationReporter.NOOP;
  private CancellationToken cancellationToken = CancellationToken.alwaysActive();
  private DelayContext delayContext =
      () -> {
        /* no-op */
      };

  public ArrayController(int size) {
    setShuffleType(ShuffleType.RANDOM);
    resize(size);
  }

  public void resize(int size) {
    array = new int[size];
    markers = new Marker[size];
    length = size;
    comparisons = 0;
    swaps = 0;
    writes = 0;
    realTime = 0;
    writesAux = 0;
    sortedPercentage = 1;
    segments = 1;
    metricsDirty = true;
    bumpVisualRevision();

    // Initial Values
    for (int i = 0; i < size; i++) {
      array[i] = i;
      markers[i] = Marker.NORMAL;
    }
  }

  private void markMetricsDirty() {
    metricsDirty = true;
  }

  private void bumpVisualRevision() {
    visualRevision.incrementAndGet();
  }

  @Override
  public long getVisualRevision() {
    return visualRevision.get();
  }

  @Override
  public long getComparisons() {
    return comparisons;
  }

  @Override
  public void addComparisons(int n) {
    comparisons += n;
  }

  @Override
  public long getSwaps() {
    return swaps;
  }

  @Override
  public long getWrites() {
    return writes;
  }

  @Override
  public double getRealTime() {
    return realTime;
  }

  @Override
  public void addRealTime(double realTime) {
    this.realTime += realTime;
  }

  @Override
  public int[] getArray() {
    return array;
  }

  @Override
  public long getWritesAux() {
    return writesAux;
  }

  @Override
  public void addWritesAux(int n) {
    this.writesAux += n;
  }

  public void resetMeasurements() {
    comparisons = 0;
    swaps = 0;
    writes = 0;
    realTime = 0;
    writesAux = 0;
    sortedPercentage = 1;
    segments = 1;
    metricsDirty = true;
  }

  /**
   * Copies {@code snapshot} into the working array without incrementing write/swap counters. Used
   * to restore state after an equalize dry-run.
   */
  public void restoreContents(int[] snapshot) {
    if (snapshot == null || snapshot.length != length) {
      throw new IllegalArgumentException("snapshot length must match array length");
    }
    System.arraycopy(snapshot, 0, array, 0, length);
    resetMarkers();
    markMetricsDirty();
    bumpVisualRevision();
  }

  public void resetArray() {
    for (int i = 0; i < length; i++) {
      array[i] = i;
      markers[i] = Marker.NORMAL;
    }
    markMetricsDirty();
    bumpVisualRevision();
  }

  @Override
  public Marker getMarker(int index) {
    return markers[index];
  }

  @Override
  public void setMarker(int i, Marker m) {
    if (markers[i] != m) {
      bumpVisualRevision();
    }
    markers[i] = m;
  }

  public void resetMarkers() {
    boolean changed = false;
    for (int i = 0; i < length; i++) {
      if (markers[i] != Marker.NORMAL) {
        markers[i] = Marker.NORMAL;
        changed = true;
      }
    }
    if (changed) {
      bumpVisualRevision();
    }
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public int get(int i) {
    return array[i];
  }

  @Override
  public void set(int i, int value) {
    array[i] = value;
    writes += 1;
    markMetricsDirty();
    bumpVisualRevision();
  }

  @Override
  public void swap(int i, int j) {
    int swapOneValue = array[i];
    array[i] = array[j];
    array[j] = swapOneValue;
    writes += 2;
    swaps += 1;
    markMetricsDirty();
    bumpVisualRevision();
  }

  @Override
  public boolean isSorted() {
    for (int i = 1; i < length; i++) {
      if (array[i - 1] > array[i]) return false;
    }
    return true;
  }

  @Override
  public double getSortedPercentage() {
    return sortedPercentage;
  }

  @Override
  public int getSegments() {
    return segments;
  }

  public void update() {
    if (!metricsDirty) {
      return;
    }

    double sortedCount = 0;
    int segmentStart = 0;
    int sgmnts = 0;
    for (int i = 1; i < length; i++) {
      if (array[i] < array[i - 1]) {
        sortedCount += i - 1 - segmentStart;
        segmentStart = i;
        sgmnts++;
      } else if (i == length - 1 && array[length - 1] > array[length - 2]) {
        sortedCount += i - segmentStart + 1;
        segmentStart = i;
        sgmnts++;
      }
    }

    segments = sgmnts;
    sortedPercentage = sortedCount / length;
    metricsDirty = false;
  }

  /** Package-visible for tests. */
  boolean isMetricsDirty() {
    return metricsDirty;
  }

  void shuffle() {
    if (cancellationToken.isCancelled()) return;
    shuffleStrategy.shuffle(this, delayContext, operationReporter, cancellationToken);
    markMetricsDirty();
    bumpVisualRevision();
  }

  /**
   * Runs the configured shuffle with no pacing, recording swap pairs so {@link
   * #replayRecordedShuffle(RecordedShuffle)} can animate the same permutation.
   */
  public RecordedShuffle captureMuteShuffle() {
    int[] pre = Arrays.copyOf(array, length);
    if (cancellationToken.isCancelled()) {
      return new RecordedShuffle(pre, pre, new int[0]);
    }
    List<Integer> pairs = SwapRecordingModel.newPairList();
    SwapRecordingModel recorder = new SwapRecordingModel(this, pairs);
    DelayContext noop =
        () -> {
          /* mute */
        };
    shuffleStrategy.shuffle(recorder, noop, OperationReporter.NOOP, cancellationToken);
    markMetricsDirty();
    bumpVisualRevision();
    int[] post = Arrays.copyOf(array, length);
    return new RecordedShuffle(pre, post, recorder.swapPairsArray());
  }

  /**
   * Restores the pre-shuffle state, replays recorded swaps (or a marker sweep) with the current
   * {@link DelayContext}, then forces the post-shuffle permutation.
   */
  public void replayRecordedShuffle(RecordedShuffle recorded) {
    if (recorded == null) {
      throw new IllegalArgumentException("recorded");
    }
    if (recorded.length() != length) {
      throw new IllegalArgumentException("recording length must match array length");
    }
    restoreContents(recorded.pre());
    int swaps = recorded.swapCount();
    if (swaps == 0) {
      int n = Math.max(1, length);
      for (int i = 0; i < length && !cancellationToken.isCancelled(); i++) {
        setMarker(i, Marker.SET);
        int denom = Math.max(1, n - 1);
        operationReporter.report("Shuffling.. " + (int) ((double) i / denom * 100) + "%");
        RandomShuffleStrategy.maybeDelay(delayContext, i, n);
      }
    } else {
      for (int s = 0; s < swaps && !cancellationToken.isCancelled(); s++) {
        int i = recorded.swapI(s);
        int j = recorded.swapJ(s);
        swap(i, j);
        setMarker(i, Marker.SET);
        setMarker(j, Marker.SET);
        int denom = Math.max(1, swaps - 1);
        operationReporter.report("Shuffling.. " + (int) ((double) s / denom * 100) + "%");
        RandomShuffleStrategy.maybeDelay(delayContext, s, swaps);
      }
    }
    restoreContents(recorded.post());
  }

  public void setDelayContext(DelayContext delayContext) {
    this.delayContext =
        delayContext != null
            ? delayContext
            : () -> {
              /* no-op */
            };
  }

  public ShuffleType getShuffleType() {
    return shuffleType;
  }

  public void setShuffleType(ShuffleType shuffleType) {
    this.shuffleType = shuffleType;
    this.shuffleStrategy =
        switch (shuffleType) {
          case RANDOM -> new RandomShuffleStrategy();
          case REVERSE -> new ReverseShuffleStrategy();
          case ALMOST_SORTED -> new AlmostSortedShuffleStrategy();
          case SORTED -> new SortedShuffleStrategy();
        };
  }

  public void setOperationReporter(OperationReporter operationReporter) {
    this.operationReporter = operationReporter != null ? operationReporter : OperationReporter.NOOP;
  }

  public void setCancellationToken(CancellationToken cancellationToken) {
    this.cancellationToken =
        cancellationToken != null ? cancellationToken : CancellationToken.alwaysActive();
  }
}
