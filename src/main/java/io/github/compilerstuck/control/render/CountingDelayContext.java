package io.github.compilerstuck.control.render;

import java.util.function.BooleanSupplier;

/**
 * Counts {@link #delay()} / {@link #delayFrame()} calls without blocking. Used for equalize-mode
 * dry-runs. Optionally times out after a wall-clock deadline or an external cancel signal.
 */
public final class CountingDelayContext implements DelayContext {
  private final long deadlineNanos;
  private final BooleanSupplier externalCancel;
  private final Runnable onAbort;
  private long stepCount;
  private long frameBeatCount;
  private boolean timedOut;
  private boolean aborted;

  /** Unlimited counting (no timeout). */
  public CountingDelayContext() {
    this(Long.MAX_VALUE, null, null);
  }

  /**
   * @param deadlineNanos {@link System#nanoTime()} after which further delays mark timed-out
   * @param externalCancel optional; when true, aborts counting (e.g. session cancel)
   * @param onAbort optional callback invoked once when aborting or timing out
   */
  public CountingDelayContext(
      long deadlineNanos, BooleanSupplier externalCancel, Runnable onAbort) {
    this.deadlineNanos = deadlineNanos;
    this.externalCancel = externalCancel;
    this.onAbort = onAbort;
  }

  @Override
  public void delay() {
    if (shouldStop()) {
      return;
    }
    stepCount++;
  }

  @Override
  public void delayFrame() {
    if (shouldStop()) {
      return;
    }
    stepCount++;
    frameBeatCount++;
  }

  private boolean shouldStop() {
    if (timedOut || aborted) {
      return true;
    }
    if (externalCancel != null && externalCancel.getAsBoolean()) {
      aborted = true;
      fireAbort();
      return true;
    }
    if (System.nanoTime() >= deadlineNanos) {
      timedOut = true;
      fireAbort();
      return true;
    }
    return false;
  }

  private void fireAbort() {
    if (onAbort != null) {
      onAbort.run();
    }
  }

  public long stepCount() {
    return stepCount;
  }

  public long frameBeatCount() {
    return frameBeatCount;
  }

  public boolean timedOut() {
    return timedOut;
  }

  public boolean aborted() {
    return aborted;
  }
}
