package io.github.compilerstuck.control.ui.settingsfx.vm;

import io.github.compilerstuck.control.AppContext;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Headless sound section view-model (G9). No {@code javafx.*} imports. */
public final class SoundViewModel {

  public static final String PROP_SOUND_ENABLED = "soundEnabled";
  public static final String PROP_INPUTS_ENABLED = "inputsEnabled";

  private final AppContext app;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  private boolean soundEnabled;
  private boolean inputsEnabled = true;

  public SoundViewModel(AppContext app) {
    this.app = app;
    this.soundEnabled = app.getSound() == null || !app.getSound().isMuted();
  }

  public boolean isSoundEnabled() {
    return soundEnabled;
  }

  public void setSoundEnabled(boolean enabled) {
    if (!inputsEnabled || soundEnabled == enabled) {
      return;
    }
    boolean old = soundEnabled;
    soundEnabled = enabled;
    app.setMuted(!enabled);
    pcs.firePropertyChange(PROP_SOUND_ENABLED, old, enabled);
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
