package io.github.compilerstuck.control.ui.settingsfx.vm;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.config.SettingsDefaults;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Headless speed / step-engine view-model. Disables both controls while running (G5). */
public final class SpeedViewModel {

  public static final String PROP_SPEED_LEVEL = "speedLevel";
  public static final String PROP_USE_STEP_ENGINE = "useStepEngine";
  public static final String PROP_INPUTS_ENABLED = "inputsEnabled";

  private final AppContext app;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  private int speedLevel;
  private boolean useStepEngine;
  private boolean inputsEnabled = true;

  public SpeedViewModel(AppContext app) {
    this.app = app;
    this.speedLevel = app.getSpeedLevel();
    this.useStepEngine = app.isUseStepEngine();
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

  public boolean isUseStepEngine() {
    return useStepEngine;
  }

  public void setUseStepEngine(boolean use) {
    if (!inputsEnabled || useStepEngine == use) {
      return;
    }
    boolean old = useStepEngine;
    useStepEngine = use;
    app.setUseStepEngine(use);
    pcs.firePropertyChange(PROP_USE_STEP_ENGINE, old, use);
  }

  public boolean isInputsEnabled() {
    return inputsEnabled;
  }

  /** Disables both speed level and step-engine (fixes SP-05 at the model layer). */
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
