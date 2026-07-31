package io.github.compilerstuck.control.render;

import io.github.compilerstuck.control.model.FrameGate;

/** {@link DelayContext} backed by {@link FrameGate}; no graphics dependency. */
public final class FrameGateDelayContext implements DelayContext {
  private final FrameGate frameGate;

  public FrameGateDelayContext(FrameGate frameGate) {
    this.frameGate = frameGate;
  }

  @Override
  public void delay() {
    try {
      frameGate.awaitStep();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void delayFrame() {
    // Drop leftover credits from this frame's grant so awaitIdle can publish now, then wait for
    // the next grant (one visible frame per call regardless of steps-per-frame).
    frameGate.drain();
    delay();
  }
}
