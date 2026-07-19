package io.github.compilerstuck.control.render;

/**
 * Minimal pacing port used by sorting algorithms and shuffle strategies. Intentionally separate
 * from drawing so algorithms never depend on a graphics API.
 *
 * <p>Implementations await a {@link io.github.compilerstuck.control.model.FrameGate} credit (one
 * visualized step).
 */
public interface DelayContext {
  /** Block until the next frame-gate step is granted. */
  void delay();
}
