package io.github._66_m.control.ui.settingsfx.vm;

import io.github._66_m.control.AppContext;
import io.github._66_m.control.config.audio.AudioSettings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Headless audio settings dialog view-model (no {@code javafx.*} imports). */
public final class AudioSettingsViewModel {

  public static final String PROP_INPUTS_ENABLED = "inputsEnabled";

  private final AppContext app;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private boolean inputsEnabled = true;

  public AudioSettingsViewModel(AppContext app) {
    this.app = app;
  }

  public AudioSettings getSettings() {
    return app.getAudioSettings();
  }

  public void applySettings(AudioSettings settings) {
    app.setAudioSettings(settings);
  }

  public AudioSettings defaults() {
    return AudioSettings.defaults();
  }

  public void previewTestTone(AudioSettings draft) {
    if (app.getSound() != null) {
      app.getSound().previewTestTone(draft);
    }
  }

  public void previewShuffle(
      AudioSettings draft, int simulatedLength, long durationMs, Runnable onFinished) {
    if (app.getSound() != null) {
      app.getSound().previewShuffle(draft, simulatedLength, durationMs, onFinished);
    } else if (onFinished != null) {
      onFinished.run();
    }
  }

  public void previewPitchSweep(
      AudioSettings draft, int simulatedLength, long durationMs, Runnable onFinished) {
    if (app.getSound() != null) {
      app.getSound().previewPitchSweep(draft, simulatedLength, durationMs, onFinished);
    } else if (onFinished != null) {
      onFinished.run();
    }
  }

  public void stopPreview() {
    if (app.getSound() != null) {
      app.getSound().stopPreview();
    }
  }

  public boolean isInputsEnabled() {
    return inputsEnabled;
  }

  public void setInputsEnabled(boolean enabled) {
    if (inputsEnabled == enabled) {
      return;
    }
    boolean old = inputsEnabled;
    inputsEnabled = enabled;
    pcs.firePropertyChange(PROP_INPUTS_ENABLED, old, enabled);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }
}
