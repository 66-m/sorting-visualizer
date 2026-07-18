package io.github.compilerstuck.Control.model;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe cancellation signal for a sorting session (algorithms and shuffles).
 */
public final class CancellationToken {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * Returns a fresh token that is active until {@link #cancel()} is called.
     */
    public static CancellationToken alwaysActive() {
        return new CancellationToken();
    }
}
