package io.github.compilerstuck.Control;

/**
 * Minimal abstraction of the Processing runtime used by sorting algorithms to
 * perform delays (and potentially other small operations).  Keeping the
 * interface small makes it easy to provide a headless stub for testing.
 */
public interface ProcessingContext {
    /**
     * Pause execution for the given number of milliseconds.  The semantics should
     * match {@code processing.core.PApplet.delay(int)}.
     */
    void delay(int ms);
}
