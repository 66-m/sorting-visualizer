package io.github.compilerstuck.sound;

import io.github.compilerstuck.control.model.ArrayModel;

public abstract class Sound {
  ArrayModel arrayModel;
  protected boolean isMuted;

  public Sound(ArrayModel arrayModel) {
    this.arrayModel = arrayModel;
  }

  public abstract void playSound(int value);

  public abstract void mute(boolean mute);

  /**
   * Hard-cut sounding notes without changing mute state. Used between algorithms and on session end
   * so SoftSynth release tails do not smear into the next phase.
   */
  public void cutNotes() {
    // no-op
  }

  /** Runs {@code action} with sound muted, restoring the previous mute channel state afterward. */
  public void withMuted(Runnable action) {
    mute(true);
    try {
      action.run();
    } finally {
      mute(false);
    }
  }

  /**
   * Releases native audio resources. Default is a no-op; MIDI implementations should close the
   * synthesizer.
   */
  public void dispose() {
    // no-op
  }

  public void setIsMuted(boolean muted) {
    isMuted = muted;
    mute(isMuted);
  }

  public boolean isMuted() {
    return isMuted;
  }
}
