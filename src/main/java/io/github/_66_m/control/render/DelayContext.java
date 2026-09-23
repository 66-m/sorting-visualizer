package io.github._66_m.control.render;

/**
 * Minimal pacing port used by sorting algorithms and shuffle strategies. Intentionally separate
 * from drawing so algorithms never depend on a graphics API.
 *
 * <p>Implementations await a {@link io.github._66_m.control.model.FrameGate} credit (one visualized
 * step).
 */
public interface DelayContext {
  /** Block until the next frame-gate step is granted. */
  void delay();

  /**
   * Wait for the next <em>published</em> frame: discard unused step credits so the render thread
   * can snapshot, then await one new credit. Use for whole-array visual beats (e.g. one Gravity
   * Sort column) that must not skip frames when steps-per-frame &gt; 1.
   *
   * <p>Default matches {@link #delay()} for simple/no-op contexts.
   */
  default void delayFrame() {
    delay();
  }
}
