package io.github.compilerstuck.control.render;

import io.github.compilerstuck.control.model.FrameGate;

/** {@link DelayContext} backed by {@link FrameGate} — no graphics dependency. */
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
}
