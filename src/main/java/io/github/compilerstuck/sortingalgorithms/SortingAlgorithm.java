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
    arrayController.addRealTime(System.nanoTime() - startTime);
    timing = false;
    startTime = 0;
  }

  /**
   * Records CPU work since the last step, then waits for a FrameGate credit. Visual wait time is
   * not included in {@link ArrayModel#getRealTime()}.
   */
  public void delay(int[] markers) {
    if (isCancelled()) {
      return;
    }
    if (delay) {
      if (!timing) {
        beginTiming();
      } else {
        arrayController.addRealTime(System.nanoTime() - startTime);
      }

      for (int i : markers) {
        arrayController.setMarker(i, Marker.SET);
      }

      proc.delay();
      startTime = System.nanoTime();
    }
  }

  public void delay() {
    delay(new int[0]);
  }
}
