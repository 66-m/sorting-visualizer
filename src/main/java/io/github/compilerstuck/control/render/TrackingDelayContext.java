package io.github.compilerstuck.control.render;

import io.github.compilerstuck.control.model.EqualizePacing;
import java.util.Objects;

/**
 * Delegates to a production {@link DelayContext} while recording consumed equalize steps.
 *
 * <p>For {@link #delayFrame()}, applies equalize policy: batch beats into plain {@link #delay()}
 * calls when the target is below the one-frame-per-beat floor, or insert extra published frames
 * when stretching longer than that floor.
 */
public final class TrackingDelayContext implements DelayContext {
  private final DelayContext delegate;
  private final EqualizePacing pacing;

  public TrackingDelayContext(DelayContext delegate, EqualizePacing pacing) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    this.pacing = Objects.requireNonNull(pacing, "pacing");
  }

  @Override
  public void delay() {
    pacing.recordStep();
    delegate.delay();
  }

  @Override
  public void delayFrame() {
    pacing.recordFrameBeat();
    if (pacing.batchBeats()) {
      // Hit short targets: allow multiple columns per published frame (no drain).
      delegate.delay();
      return;
    }
    delegate.delayFrame();
    int extra = pacing.takeExtraFrameWaits();
    for (int i = 0; i < extra; i++) {
      // Empty published frames between beats (still drain+await).
      delegate.delayFrame();
    }
  }
}
