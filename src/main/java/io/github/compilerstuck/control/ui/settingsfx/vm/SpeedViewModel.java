package io.github.compilerstuck.control.ui.settingsfx.vm;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.config.SettingsDefaults;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Headless speed view-model. Disables the control while running (G5). */
public final class SpeedViewModel {

  public static final String PROP_SPEED_LEVEL = "speedLevel";
  public static final String PROP_INPUTS_ENABLED = "inputsEnabled";

  private final AppContext app;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  private int speedLevel;
  private boolean inputsEnabled = true;

  public SpeedViewModel(AppContext app) {
    this.app = app;
    this.speedLevel = app.getSpeedLevel();
  }

  public int getSpeedLevel() {
    return speedLevel;
  }

  public void setSpeedLevel(int level) {
    if (!inputsEnabled) {
      return;
    }
    int clamped = SettingsDefaults.clampSpeedLevel(level);
    if (speedLevel == clamped) {
      return;
    }
    int old = speedLevel;
    speedLevel = clamped;
    app.setSpeedLevel(clamped);
    pcs.firePropertyChange(PROP_SPEED_LEVEL, old, clamped);
  }

  public boolean isInputsEnabled() {
    return inputsEnabled;
  }

  /** Disables the speed level control while a sort is running. */
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
