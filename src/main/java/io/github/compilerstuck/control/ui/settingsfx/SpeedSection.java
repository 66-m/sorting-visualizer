package io.github.compilerstuck.control.ui.settingsfx;

import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.control.ui.settingsfx.vm.SpeedViewModel;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Speed level slider with live value (disabled while running, G5). */
public final class SpeedSection {

  public static final String ROOT_ID = "section-speed";

  private SpeedSection() {}

  public static Node build(SpeedViewModel vm) {
    Label value = SettingsControls.valueLabel();
    value.setText(formatLevel(vm.getSpeedLevel()));

    Slider slider =
        new Slider(
            SettingsDefaults.SPEED_LEVEL_MIN, SettingsDefaults.SPEED_LEVEL_MAX, vm.getSpeedLevel());
    slider.setMaxWidth(Double.MAX_VALUE);
    slider.setMajorTickUnit(1);
    slider.setMinorTickCount(0);
    slider.setSnapToTicks(true);
    slider.setShowTickMarks(true);
    slider.setShowTickLabels(false);
    slider
        .valueProperty()
        .addListener(
            (obs, old, v) -> {
              int level = (int) Math.round(v.doubleValue());
              value.setText(formatLevel(level));
              if (level != vm.getSpeedLevel()) {
                vm.setSpeedLevel(level);
              }
            });

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox header =
        new HBox(
            SettingsLayout.GAP_SM,
            SettingsControls.fieldLabel(SettingsStrings.LEVEL),
            spacer,
            value);
    header.setAlignment(Pos.CENTER_LEFT);

    Label slow = SettingsControls.mutedLabel(SettingsStrings.SPEED_SLOW);
    Label fast = SettingsControls.mutedLabel(SettingsStrings.SPEED_FAST);
    HBox sliderRow = new HBox(SettingsLayout.GAP_SM, slow, slider, fast);
    sliderRow.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(slider, Priority.ALWAYS);

    vm.addPropertyChangeListener(
        evt -> {
          if (SpeedViewModel.PROP_SPEED_LEVEL.equals(evt.getPropertyName())) {
            int level = (Integer) evt.getNewValue();
            VmBindings.runFx(
                () -> {
                  if ((int) Math.round(slider.getValue()) != level) {
                    slider.setValue(level);
                  }
                  value.setText(formatLevel(level));
                });
          }
        });

    VmBindings.bindInputsEnabled(
        slider,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        SpeedViewModel.PROP_INPUTS_ENABLED);

    VBox root = new VBox(SettingsLayout.GAP_SM, header, sliderRow);
    root.setId(ROOT_ID);
    return root;
  }

  private static String formatLevel(int level) {
    return String.format(
        SettingsStrings.SPEED_LEVEL_FORMAT, level, SettingsDefaults.SPEED_LEVEL_MAX);
  }
}
