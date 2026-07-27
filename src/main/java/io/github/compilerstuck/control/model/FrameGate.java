package io.github.compilerstuck.control.model;

/** Credits-based gate so a sorting thread waits for draw-thread step budgets. */
public final class FrameGate {
  private final Object lock = new Object();
  private int credits;
  private boolean cancelled;

  /** Called from the draw thread to allow up to {@code n} algorithm delay steps. */
  public void grant(int n) {
    if (n <= 0) {
      return;
    }
    synchronized (lock) {
      if (cancelled) {
        return;
      }
      credits += n;
      lock.notifyAll();
    }
  }

  /** Blocks until a credit is available or the gate is cancelled. */
  public void awaitStep() throws InterruptedException {
    synchronized (lock) {
      while (credits <= 0 && !cancelled) {
        lock.wait();
      }
      if (cancelled) {
        return;
      }
      credits--;
      if (credits == 0) {
        lock.notifyAll();
      }
    }
  }

  /**
   * Blocks until no credits remain (sort worker blocked in {@link #awaitStep}) or the gate is
   * cancelled. Used by the render thread before publishing an array snapshot.
   */
  public void awaitIdle() throws InterruptedException {
    synchronized (lock) {
      while (credits > 0 && !cancelled) {
        lock.wait();
      }
    }
  }

  /**
   * Drops leftover credits and wakes {@link #awaitIdle} waiters. Used when the sort worker finishes
   * (or is torn down) so the render thread is not stuck waiting for credits nobody will consume.
   */
  public void drain() {
    synchronized (lock) {
      credits = 0;
      lock.notifyAll();
    }
  }

  /** Unblocks waiters; further awaits return immediately until {@link #reset()}. */
  public void cancel() {
    synchronized (lock) {
      cancelled = true;
      lock.notifyAll();
    }
  }

  /** Clears credits and cancellation for a new session. */
  public void reset() {
    synchronized (lock) {
      credits = 0;
      cancelled = false;
      lock.notifyAll();
    }
  }

  /** Package-visible for tests. */
  int availableCredits() {
    synchronized (lock) {
      return credits;
    }
  }

  /** Package-visible for tests. */
  boolean isCancelled() {
    synchronized (lock) {
      return cancelled;
    }
  }
}
