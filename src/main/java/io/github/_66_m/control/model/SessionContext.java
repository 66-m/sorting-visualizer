package io.github._66_m.control.model;

import io.github._66_m.sound.Sound;

/**
 * State shared by the parts of a sorting session: the live array, sound, UI state, the pacing gate
 * and the current algorithm's cancellation token.
 */
final class SessionContext {
  final ArrayController array;
  final Sound sound;
  final SortingStateManager state;

  private volatile FrameGate frameGate;
  private volatile CancellationToken token = CancellationToken.alwaysActive();

  SessionContext(ArrayController array, Sound sound, SortingStateManager state) {
    this.array = array;
    this.sound = sound;
    this.state = state;
  }

  FrameGate frameGate() {
    return frameGate;
  }

  void setFrameGate(FrameGate frameGate) {
    this.frameGate = frameGate;
  }

  CancellationToken token() {
    return token;
  }

  void setToken(CancellationToken token) {
    this.token = token;
  }

  /** Drops unused FrameGate step credits (no-op without a gate). */
  void drainGate() {
    FrameGate gate = frameGate;
    if (gate != null) {
      gate.drain();
    }
  }

  /** True once the user cancelled the session or skipped the current algorithm. */
  boolean shouldStop() {
    return !state.shouldContinueExecution() || token.isCancelled();
  }
}
