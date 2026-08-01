package io.github.compilerstuck.control.ui.settingsfx.vm;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.config.CanvasBackground;
import io.github.compilerstuck.control.config.GradientPresets;
import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;

/**
 * Headless appearance (gradient + canvas background) view-model. Initializes from the live {@link
 * AppContext#getColorGradient()} rather than a hard-coded combo index.
 */
public final class AppearanceViewModel {

  public static final String PROP_SELECTED_INDEX = "selectedIndex";
  public static final String PROP_COLOR1 = "color1";
  public static final String PROP_COLOR2 = "color2";
  public static final String PROP_CANVAS_BACKGROUND = "canvasBackground";
  public static final String PROP_INPUTS_ENABLED = "inputsEnabled";

  private final AppContext app;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private final List<ColorGradient> presets;

  private int selectedIndex;
  private Color color1;
  private Color color2;
  private CanvasBackground canvasBackground;
  private boolean inputsEnabled = true;

  public AppearanceViewModel(AppContext app) {
    this.app = app;
    this.presets = GradientPresets.createDefaultList();
    ColorGradient current = app.getColorGradient();
    if (current != null) {
      selectedIndex = GradientPresets.indexOfName(presets, current.getName());
      color1 = current.getColor1();
      color2 = current.getColor2();
    } else {
      selectedIndex = 0;
      color1 = presets.get(0).getColor1();
      color2 = presets.get(0).getColor2();
    }
    canvasBackground =
        app.getCanvasBackground() != null
            ? app.getCanvasBackground()
            : SettingsDefaults.DEFAULT_CANVAS_BACKGROUND;
  }

  public List<ColorGradient> getPresets() {
    return Collections.unmodifiableList(presets);
  }

  public List<String> getPresetNames() {
    return presets.stream().map(ColorGradient::getName).toList();
  }

  public List<CanvasBackground> getCanvasBackgroundOptions() {
    return List.of(CanvasBackground.values());
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public Color getColor1() {
    return color1;
  }

  public Color getColor2() {
    return color2;
  }

  public CanvasBackground getCanvasBackground() {
    return canvasBackground;
  }

  public void selectPreset(int index) {
    if (!inputsEnabled || index < 0 || index >= presets.size() || index == selectedIndex) {
      return;
    }
    int old = selectedIndex;
    selectedIndex = index;
    ColorGradient selected = presets.get(index);
    selected.updateGradient(app.getSize());
    app.setColorGradient(selected);
    Color old1 = color1;
    Color old2 = color2;
    color1 = selected.getColor1();
    color2 = selected.getColor2();
    pcs.firePropertyChange(PROP_SELECTED_INDEX, old, selectedIndex);
    pcs.firePropertyChange(PROP_COLOR1, old1, color1);
    pcs.firePropertyChange(PROP_COLOR2, old2, color2);
  }

  /** Updates the Custom Gradient entry and selects it. */
  public void setCustomColors(Color c1, Color c2) {
    if (!inputsEnabled || c1 == null || c2 == null) {
      return;
    }
    ColorGradient custom = presets.get(presets.size() - 1);
    custom.setColor1(c1);
    custom.setColor2(c2);
    custom.updateGradient(app.getSize());
    Color old1 = color1;
    Color old2 = color2;
    int oldIdx = selectedIndex;
    color1 = c1;
    color2 = c2;
    selectedIndex = presets.size() - 1;
    app.setColorGradient(custom);
    pcs.firePropertyChange(PROP_SELECTED_INDEX, oldIdx, selectedIndex);
    pcs.firePropertyChange(PROP_COLOR1, old1, color1);
    pcs.firePropertyChange(PROP_COLOR2, old2, color2);
  }

  public void setCanvasBackground(CanvasBackground background) {
    if (!inputsEnabled || background == null || background == canvasBackground) {
      return;
    }
    CanvasBackground old = canvasBackground;
    canvasBackground = background;
    app.setCanvasBackground(background);
    pcs.firePropertyChange(PROP_CANVAS_BACKGROUND, old, canvasBackground);
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
