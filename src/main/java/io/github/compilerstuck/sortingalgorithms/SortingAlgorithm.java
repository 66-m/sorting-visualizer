package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.model.CancellationToken;
import io.github.compilerstuck.control.model.OperationReporter;
import io.github.compilerstuck.control.render.DelayContext;
import io.github.compilerstuck.visual.Marker;

public abstract class SortingAlgorithm {
  protected DelayContext proc;
  protected String name;
  protected boolean delay;
  protected int alternativeSize;
  protected boolean selected = true;

  /** NanoTime at the start of the current behind-the-scenes work slice; 0 if not timing. */
  private long startTime;

  private boolean timing;
  protected OperationReporter operationReporter = OperationReporter.NOOP;
  protected CancellationToken cancellationToken = CancellationToken.alwaysActive();

  /**
   * Under equalize mode, only every {@code delayStride}-th {@link #delay}/{@link #delayFrame} waits
   * on the FrameGate so huge step counts can still hit the duration target.
   */
  private int delayStride = 1;

  private int delayPhase;

  /** Indices highlighted on the previous visualized pace; cleared before the next highlight. */
  private int[] lastMarkers = EMPTY_MARKERS;

  private static final int[] EMPTY_MARKERS = new int[0];

  ArrayModel arrayController;

  public SortingAlgorithm(ArrayModel arrayController) {
    this(
        arrayController,
        () -> {
          /* no-op delay */
        });
  }

  public SortingAlgorithm(ArrayModel arrayController, DelayContext proc) {
    this.proc = proc;
    this.arrayController = arrayController;
    delay = true;
  }

  public abstract void sort();

  public void setDelay(boolean delay) {
    this.delay = delay;
  }

  public String getName() {
    return name;
  }

  public void setAlternativeSize(int alternativeSize) {
    this.alternativeSize = alternativeSize;
  }

  public int getAlternativeSize() {
    return alternativeSize;
  }

  public void setDelayContext(DelayContext proc) {
    this.proc =
        proc != null
            ? proc
            : () -> {
              /* no-op */
            };
  }

  public void setOperationReporter(OperationReporter operationReporter) {
    this.operationReporter = operationReporter != null ? operationReporter : OperationReporter.NOOP;
  }

  protected void report(String operation) {
    operationReporter.report(operation);
  }

  public void setCancellationToken(CancellationToken cancellationToken) {
    this.cancellationToken =
        cancellationToken != null ? cancellationToken : CancellationToken.alwaysActive();
  }

  protected boolean isCancelled() {
    return cancellationToken.isCancelled();
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }

  /** Starts behind-the-scenes timing for a sort run (excludes visual FrameGate waits). */
  public void beginTiming() {
    startTime = System.nanoTime();
    timing = true;
  }

  /** Flushes the current work slice into {@link ArrayModel#getRealTime()} and stops timing. */
  public void endTiming() {
    if (!timing) {
      return;
    }
    arrayController.addRealTime((double) (System.nanoTime() - startTime));
    timing = false;
    startTime = 0;
  }

  /**
   * Records CPU work since the last step, then waits for a FrameGate credit. Visual wait time is
   * not included in {@link ArrayModel#getRealTime()}.
   */
  public void delay(int[] markers) {
    pace(markers, false);
  }

  /**
   * Like {@link #delay(int[])}, but forces one published frame (see {@link
   * DelayContext#delayFrame()}).
   */
  public void delayFrame(int[] markers) {
    pace(markers, true);
  }

  public void delay() {
    delay(new int[0]);
  }

  /** Visualize only every {@code stride}-th paced step (minimum 1). Resets the phase counter. */
  public void setDelayStride(int stride) {
    delayStride = Math.max(1, stride);
    delayPhase = 0;
  }

  public int getDelayStride() {
    return delayStride;
  }

  private void pace(int[] markers, boolean wholeFrame) {
    if (isCancelled()) {
      return;
    }
    if (!delay) {
      return;
    }

    delayPhase++;
    if (delayPhase % delayStride != 0) {
      return;
    }

    if (!timing) {
      beginTiming();
    } else {
      arrayController.addRealTime((double) (System.nanoTime() - startTime));
    }

    // Replace highlights instead of accumulating — fast equalize batches would otherwise paint
    // most of the array white before the next publish.
    clearLastMarkers();
    for (int i : markers) {
      arrayController.setMarker(i, Marker.SET);
    }
    lastMarkers = markers.length == 0 ? EMPTY_MARKERS : markers;

    if (wholeFrame) {
      proc.delayFrame();
    } else {
      proc.delay();
    }
    startTime = System.nanoTime();
  }

  private void clearLastMarkers() {
    for (int i : lastMarkers) {
      if (i >= 0 && i < arrayController.getLength()) {
        arrayController.setMarker(i, Marker.NORMAL);
      }
    }
  }
}
