package io.github.compilerstuck.Control.model;

/**
 * Credits-based gate so a sorting thread can wait for draw-thread step budgets
 * instead of sleeping ({@code FrameGate} step-engine mode).
 */
public final class FrameGate {
    private final Object lock = new Object();
    private int credits;
    private boolean cancelled;

    /**
     * Called from the draw thread to allow up to {@code n} algorithm delay steps.
     */
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

    /**
     * Blocks until a credit is available or the gate is cancelled.
     */
    public void awaitStep() throws InterruptedException {
        synchronized (lock) {
            while (credits <= 0 && !cancelled) {
                lock.wait();
            }
            if (cancelled) {
                return;
            }
            credits--;
        }
    }

    /**
     * Unblocks waiters; further awaits return immediately until {@link #reset()}.
     */
    public void cancel() {
        synchronized (lock) {
            cancelled = true;
            lock.notifyAll();
        }
    }

    /**
     * Clears credits and cancellation for a new session.
     */
    public void reset() {
        synchronized (lock) {
            credits = 0;
            cancelled = false;
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
