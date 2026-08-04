package io.github.compilerstuck.sound;

import io.github.compilerstuck.control.config.audio.AudioSettings;
import io.github.compilerstuck.control.model.ArrayModel;

public abstract class Sound {
  ArrayModel arrayModel;
  protected boolean isMuted;
  protected AudioSettings settings = AudioSettings.defaults();

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

  /**
   * Applies customizable audio settings (instrument, levels, pitch mapping, advanced GM2
   * controllers) to the underlying engine. Default is a no-op; only stores the settings so {@link
   * #getSettings()} stays consistent.
   */
  public void applySettings(AudioSettings settings) {
    this.settings = settings != null ? settings : AudioSettings.defaults();
  }

  public AudioSettings getSettings() {
    return settings;
  }

  /**
   * Plays a short single test tone using (possibly unapplied) draft settings, bypassing mute.
   * Default is a no-op.
   */
  public void previewTestTone(AudioSettings draft) {
    // no-op
  }

  /**
   * Plays a {@code durationMs} burst of notes simulating a shuffle of {@code simulatedLength}
   * elements, using draft settings, bypassing mute. Default is a no-op that immediately reports
   * completion.
   */
  public void previewShuffle(
      AudioSettings draft, int simulatedLength, long durationMs, Runnable onFinished) {
    if (onFinished != null) {
      onFinished.run();
    }
  }

  /**
   * Plays a {@code durationMs} burst sweeping from lowest to highest mapped pitch across {@code
   * simulatedLength} elements, using draft settings, bypassing mute. Default is a no-op that
   * immediately reports completion.
   */
  public void previewPitchSweep(
      AudioSettings draft, int simulatedLength, long durationMs, Runnable onFinished) {
    if (onFinished != null) {
      onFinished.run();
    }
  }

  /** Stops any in-progress preview and restores the last applied (committed) settings. */
  public void stopPreview() {
    // no-op
  }
}
