package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.controls.ToggleSwitch;
import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.control.ui.settingsfx.vm.SpeedViewModel;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Speed level slider + step-engine toggle (both disable while running — G5). */
public final class SpeedSection {

  public static final String ROOT_ID = "section-speed";

  private SpeedSection() {}

  public static Node build(SpeedViewModel vm) {
    Slider slider =
        new Slider(
            SettingsDefaults.SPEED_LEVEL_MIN, SettingsDefaults.SPEED_LEVEL_MAX, vm.getSpeedLevel());
    slider.setMajorTickUnit(1);
    slider.setMinorTickCount(0);
    slider.setSnapToTicks(true);
    slider.setShowTickMarks(true);
    slider.setShowTickLabels(false);
    slider
        .valueProperty()
        .addListener(
            (obs, old, value) -> {
              int level = (int) Math.round(value.doubleValue());
              if (level != vm.getSpeedLevel()) {
                vm.setSpeedLevel(level);
              }
            });

    Label slow = new Label(SettingsStrings.SPEED_SLOW);
    slow.getStyleClass().add("settings-muted");
    Label fast = new Label(SettingsStrings.SPEED_FAST);
    fast.getStyleClass().add("settings-muted");
    HBox sliderRow = new HBox(SettingsLayout.GAP_SM, slow, slider, fast);
    sliderRow.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(slider, Priority.ALWAYS);

    ToggleSwitch step = new ToggleSwitch(SettingsStrings.STEP_ENGINE);
    step.setSelected(vm.isUseStepEngine());
    step.selectedProperty()
        .addListener(
            (obs, old, selected) -> {
              if (selected != vm.isUseStepEngine()) {
                vm.setUseStepEngine(selected);
              }
            });

    vm.addPropertyChangeListener(
        evt -> {
          if (SpeedViewModel.PROP_SPEED_LEVEL.equals(evt.getPropertyName())) {
            int level = (Integer) evt.getNewValue();
            VmBindings.runFx(
                () -> {
                  if ((int) Math.round(slider.getValue()) != level) {
                    slider.setValue(level);
                  }
                });
          } else if (SpeedViewModel.PROP_USE_STEP_ENGINE.equals(evt.getPropertyName())) {
            boolean use = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (step.isSelected() != use) {
                    step.setSelected(use);
                  }
                });
          }
        });

    VmBindings.bindInputsEnabled(
        slider,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        SpeedViewModel.PROP_INPUTS_ENABLED);
    VmBindings.bindInputsEnabled(
        step,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        SpeedViewModel.PROP_INPUTS_ENABLED);

    VBox root = new VBox(SettingsLayout.GAP_SM, sliderRow, step);
    root.setId(ROOT_ID);
    return root;
  }
}
