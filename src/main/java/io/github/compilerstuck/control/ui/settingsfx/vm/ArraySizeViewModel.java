package io.github.compilerstuck.control.ui.settingsfx.vm;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.catalog.VisualConstraints;
import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.control.ui.settingsfx.SettingsStrings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.function.Supplier;

/** Headless array-size view-model with digit validation and constraint fitting. */
public final class ArraySizeViewModel {

  public static final String PROP_SIZE = "size";
  public static final String PROP_TEXT = "text";
  public static final String PROP_TEXT_VALID = "textValid";
  public static final String PROP_VALIDATION_MESSAGE = "validationMessage";
  public static final String PROP_CAN_RUN = "canRun";
  public static final String PROP_INPUTS_ENABLED = "inputsEnabled";
  public static final String PROP_FPS_WARNING = "fpsWarning";
  public static final String PROP_HIGH_SIZE_WARNING = "highSizeWarning";

  private final AppContext app;
  private final Supplier<VisualConstraints> constraintsSupplier;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  private int size;
  private String text;
  private boolean textValid = true;
  private String validationMessage = "";
  private boolean inputsEnabled = true;
  private boolean fpsWarning;
  private boolean highSizeWarning;

  public ArraySizeViewModel(AppContext app, Supplier<VisualConstraints> constraintsSupplier) {
    this.app = app;
    this.constraintsSupplier = constraintsSupplier != null ? constraintsSupplier : () -> null;
    this.size = app.getSize();
    this.text = String.valueOf(size);
  }

  public int getSize() {
    return size;
  }

  public String getText() {
    return text;
  }

  public boolean isTextValid() {
    return textValid;
  }

  public String getValidationMessage() {
    return validationMessage;
  }

  public boolean canRun() {
    return size > SettingsDefaults.ARRAY_SIZE_MIN;
  }

  /** Live preview while dragging; resizes without writing preferences. */
  public void setSizeFromSlider(int requested) {
    if (!inputsEnabled) {
      return;
    }
    applyFitted(fit(requested), false);
  }

  /** Writes the current size to preferences once the slider is released. */
  public void commitSizeFromSlider() {
    if (!inputsEnabled) {
      return;
    }
    app.flushPreferences();
  }

  /** Updates the text buffer and validity without applying to AppContext. */
  public void setText(String raw) {
    if (!inputsEnabled) {
      return;
    }
    String oldText = text;
    boolean oldValid = textValid;
    String oldMsg = validationMessage;
    text = raw == null ? "" : raw;
    if (text.matches("[0-9]+") && text.length() <= 6) {
      textValid = true;
      validationMessage = "";
    } else {
      textValid = false;
      validationMessage = SettingsStrings.ARRAY_SIZE_WHOLE_NUMBER;
    }
    pcs.firePropertyChange(PROP_TEXT, oldText, text);
    pcs.firePropertyChange(PROP_TEXT_VALID, oldValid, textValid);
    pcs.firePropertyChange(PROP_VALIDATION_MESSAGE, oldMsg, validationMessage);
  }

  /** Applies the current text if valid. Returns whether apply succeeded. */
  public boolean applyText() {
    if (!inputsEnabled || !textValid) {
      return false;
    }
    int value = Integer.parseInt(text);
    applyFitted(fit(value), true);
    return true;
  }

  /** Sync displayed size without re-applying (e.g. after visualization constraint fit). */
  public void syncDisplayedSize(int newSize) {
    int oldSize = size;
    boolean oldCanRun = canRun();
    size = newSize;
    String oldText = text;
    text = String.valueOf(newSize);
    textValid = true;
    validationMessage = "";
    pcs.firePropertyChange(PROP_SIZE, oldSize, size);
    pcs.firePropertyChange(PROP_TEXT, oldText, text);
    pcs.firePropertyChange(PROP_TEXT_VALID, false, true);
    pcs.firePropertyChange(PROP_CAN_RUN, oldCanRun, canRun());
  }

  public boolean isInputsEnabled() {
    return inputsEnabled;
  }

  public boolean isFpsWarning() {
    return fpsWarning;
  }

  public boolean isHighSizeWarning() {
    return highSizeWarning;
  }

  public void setInputsEnabled(boolean enabled) {
    if (inputsEnabled == enabled) {
      return;
    }
    boolean old = inputsEnabled;
    inputsEnabled = enabled;
    pcs.firePropertyChange(PROP_INPUTS_ENABLED, old, enabled);
    if (!enabled) {
      setFpsWarning(false);
    }
  }

  /**
   * Samples current canvas FPS while the user is choosing size (idle). Shows a warning when FPS is
   * known and below {@link SettingsDefaults#ARRAY_SIZE_FPS_WARNING_THRESHOLD}. Ignored while a sort
   * is running (inputs disabled).
   */
  public void refreshFpsWarning() {
    if (!inputsEnabled) {
      setFpsWarning(false);
      return;
    }
    int fps = app.getFramesPerSecond();
    setFpsWarning(fps > 0 && fps < SettingsDefaults.ARRAY_SIZE_FPS_WARNING_THRESHOLD);
  }

  private void setFpsWarning(boolean warn) {
    if (fpsWarning == warn) {
      return;
    }
    boolean old = fpsWarning;
    fpsWarning = warn;
    pcs.firePropertyChange(PROP_FPS_WARNING, old, warn);
  }

  private void setHighSizeWarning(boolean warn) {
    if (highSizeWarning == warn) {
      return;
    }
    boolean old = highSizeWarning;
    highSizeWarning = warn;
    pcs.firePropertyChange(PROP_HIGH_SIZE_WARNING, old, warn);
  }

  private void applyFitted(int fitted, boolean persist) {
    if (app.isRunning()) {
      return;
    }
    boolean oldCanRun = canRun();
    int oldSize = size;
    String oldText = text;
    size = fitted;
    text = String.valueOf(fitted);
    textValid = true;
    validationMessage = "";
    app.updateArraySize(fitted);
    if (persist) {
      app.flushPreferences();
    }
    updateHighSizeWarning(oldSize, fitted);
    pcs.firePropertyChange(PROP_SIZE, oldSize, size);
    pcs.firePropertyChange(PROP_TEXT, oldText, text);
    pcs.firePropertyChange(PROP_CAN_RUN, oldCanRun, canRun());
  }

  /**
   * Shows a lag warning only when crossing from {@code <=} {@link
   * SettingsDefaults#ARRAY_SIZE_HIGH_WARNING_THRESHOLD} to above it. Clears when size drops back to
   * the threshold or below.
   */
  private void updateHighSizeWarning(int oldSize, int newSize) {
    int threshold = SettingsDefaults.ARRAY_SIZE_HIGH_WARNING_THRESHOLD;
    if (oldSize <= threshold && newSize > threshold) {
      setHighSizeWarning(true);
    } else if (newSize <= threshold) {
      setHighSizeWarning(false);
    }
  }

  private int fit(int requestedSize) {
    VisualConstraints constraints = constraintsSupplier.get();
    if (constraints == null) {
      return SettingsDefaults.clampArraySize(requestedSize);
    }
    return constraints.fitSize(
        requestedSize, SettingsDefaults.ARRAY_SIZE_MIN, SettingsDefaults.ARRAY_SIZE_MAX);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }
}
