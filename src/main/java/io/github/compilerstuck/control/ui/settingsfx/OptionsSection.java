package io.github.compilerstuck.control.ui.settingsfx;

import io.github.compilerstuck.control.ui.settingsfx.vm.DebugViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.DisplayViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.SoundViewModel;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

/**
 * Combined Options section: display toggles, sound, and debug.
 *
 * <p>Keeps both Settings columns at three sections so heights stay balanced.
 */
public final class OptionsSection {

  public static final String ROOT_ID = "section-options";

  private OptionsSection() {}

  public static Node build(
      DisplayViewModel displayVm, SoundViewModel soundVm, DebugViewModel debugVm) {
    CheckBox measurements = new CheckBox(SettingsStrings.SHOW_MEASUREMENTS);
    measurements.setSelected(displayVm.isPrintMeasurements());
    measurements
        .selectedProperty()
        .addListener(
            (obs, old, selected) -> {
              if (selected != displayVm.isPrintMeasurements()) {
                displayVm.setPrintMeasurements(selected);
              }
            });

    CheckBox comparison = new CheckBox(SettingsStrings.SHOW_COMPARISON_TABLE);
    comparison.setSelected(displayVm.isShowComparisonTable());
    comparison
        .selectedProperty()
        .addListener(
            (obs, old, selected) -> {
              if (selected != displayVm.isShowComparisonTable()) {
                displayVm.setShowComparisonTable(selected);
              }
            });

    CheckBox startDelay = new CheckBox(SettingsStrings.FIVE_SECOND_START_DELAY);
    startDelay.setSelected(displayVm.isFiveSecondStartDelay());
    startDelay
        .selectedProperty()
        .addListener(
            (obs, old, selected) -> {
              if (selected != displayVm.isFiveSecondStartDelay()) {
                displayVm.setFiveSecondStartDelay(selected);
              }
            });

    VBox display = new VBox(SettingsLayout.GAP_SM, measurements, comparison, startDelay);
    display.setId(DisplaySection.ROOT_ID);

    Node sound = SoundSection.build(soundVm);
    Node debug = DebugSection.build(debugVm);

    displayVm.addPropertyChangeListener(
        evt -> {
          if (DisplayViewModel.PROP_PRINT_MEASUREMENTS.equals(evt.getPropertyName())) {
            boolean value = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (measurements.isSelected() != value) {
                    measurements.setSelected(value);
                  }
                });
          } else if (DisplayViewModel.PROP_SHOW_COMPARISON_TABLE.equals(evt.getPropertyName())) {
            boolean value = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (comparison.isSelected() != value) {
                    comparison.setSelected(value);
                  }
                });
          } else if (DisplayViewModel.PROP_FIVE_SECOND_START_DELAY.equals(evt.getPropertyName())) {
            boolean value = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (startDelay.isSelected() != value) {
                    startDelay.setSelected(value);
                  }
                });
          }
        });

    VmBindings.bindInputsEnabled(
        measurements,
        displayVm::isInputsEnabled,
        displayVm::addPropertyChangeListener,
        DisplayViewModel.PROP_INPUTS_ENABLED);
    VmBindings.bindInputsEnabled(
        comparison,
        displayVm::isInputsEnabled,
        displayVm::addPropertyChangeListener,
        DisplayViewModel.PROP_INPUTS_ENABLED);
    VmBindings.bindInputsEnabled(
        startDelay,
        displayVm::isInputsEnabled,
        displayVm::addPropertyChangeListener,
        DisplayViewModel.PROP_INPUTS_ENABLED);

    VBox root = new VBox(SettingsLayout.GAP_SM, display, sound, debug);
    root.setId(ROOT_ID);
    return root;
  }
}
