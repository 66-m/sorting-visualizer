package io.github.compilerstuck.control.ui.settingsfx;

import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.control.ui.settingsfx.vm.DisplayViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.SpeedViewModel;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Speed level slider with per-step tick labels. Labels show steps/frame normally, or target sort
 * duration in seconds when equalize-sort-duration mode is on. The equalize toggle lives here so it
 * sits next to the control whose meaning it changes.
 */
public final class SpeedSection {

  public static final String ROOT_ID = "section-speed";
  public static final String EQUALIZE_ID = "speed-equalize";

  private SpeedSection() {}

  public static Node build(SpeedViewModel vm) {
    return build(vm, null);
  }

  public static Node build(SpeedViewModel vm, DisplayViewModel displayVm) {
    CheckBox equalize = null;
    if (displayVm != null) {
      equalize = new CheckBox(SettingsStrings.EQUALIZE_SORT_DURATION);
      equalize.setId(EQUALIZE_ID);
      equalize.setSelected(displayVm.isEqualizeSortDuration());
      equalize.setTooltip(new Tooltip(SettingsStrings.EQUALIZE_SORT_DURATION_TOOLTIP));
      equalize
          .selectedProperty()
          .addListener(
              (obs, old, selected) -> {
                if (selected != displayVm.isEqualizeSortDuration()) {
                  displayVm.setEqualizeSortDuration(selected);
                }
              });
      VmBindings.bindInputsEnabled(
          equalize,
          displayVm::isInputsEnabled,
          displayVm::addPropertyChangeListener,
          DisplayViewModel.PROP_INPUTS_ENABLED);
    }

    Label value = SettingsControls.valueLabel();
    value.setText(formatValue(vm.getSpeedLevel(), equalizeOn(displayVm)));

    HBox header = SettingsControls.labelValueHeader(headerText(equalizeOn(displayVm)), value);
    Label headerTitle = (Label) header.getChildren().get(0);

    Slider slider =
        new Slider(
            SettingsDefaults.SPEED_LEVEL_MIN, SettingsDefaults.SPEED_LEVEL_MAX, vm.getSpeedLevel());
    slider.setMaxWidth(Double.MAX_VALUE);
    slider.setMajorTickUnit(1);
    slider.setMinorTickCount(0);
    slider.setSnapToTicks(true);
    slider.setShowTickMarks(true);
    slider.setShowTickLabels(true);
    applyTickLabels(slider, equalizeOn(displayVm));

    slider
        .valueProperty()
        .addListener(
            (obs, old, v) -> {
              int level = (int) Math.round(v.doubleValue());
              value.setText(formatValue(level, equalizeOn(displayVm)));
              if (level != vm.getSpeedLevel()) {
                vm.setSpeedLevel(level);
              }
            });

    Label slow = SettingsControls.mutedLabel(slowText(equalizeOn(displayVm)));
    Label fast = SettingsControls.mutedLabel(fastText(equalizeOn(displayVm)));
    Region endpointsSpacer = new Region();
    HBox.setHgrow(endpointsSpacer, Priority.ALWAYS);
    HBox endpoints = new HBox(SettingsLayout.GAP_SM, slow, endpointsSpacer, fast);
    endpoints.setAlignment(Pos.CENTER_LEFT);

    VBox sliderBlock = new VBox(SettingsLayout.GAP_XS, slider, endpoints);

    CheckBox equalizeBox = equalize;
    Runnable refreshLabels =
        () -> {
          boolean on = equalizeOn(displayVm);
          if (equalizeBox != null && equalizeBox.isSelected() != on) {
            equalizeBox.setSelected(on);
          }
          headerTitle.setText(headerText(on));
          value.setText(formatValue(vm.getSpeedLevel(), on));
          slow.setText(slowText(on));
          fast.setText(fastText(on));
          applyTickLabels(slider, on);
        };

    vm.addPropertyChangeListener(
        evt -> {
          if (SpeedViewModel.PROP_SPEED_LEVEL.equals(evt.getPropertyName())) {
            int level = (Integer) evt.getNewValue();
            VmBindings.runFx(
                () -> {
                  if ((int) Math.round(slider.getValue()) != level) {
                    slider.setValue(level);
                  }
                  value.setText(formatValue(level, equalizeOn(displayVm)));
                });
          }
        });

    if (displayVm != null) {
      displayVm.addPropertyChangeListener(
          evt -> {
            if (DisplayViewModel.PROP_EQUALIZE_SORT_DURATION.equals(evt.getPropertyName())) {
              VmBindings.runFx(refreshLabels);
            }
          });
    }

    VmBindings.bindInputsEnabled(
        slider,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        SpeedViewModel.PROP_INPUTS_ENABLED);

    VBox root =
        equalize != null
            ? new VBox(SettingsLayout.GAP_SM, header, sliderBlock, equalize)
            : new VBox(SettingsLayout.GAP_SM, header, sliderBlock);
    root.setId(ROOT_ID);
    return root;
  }

  private static void applyTickLabels(Slider slider, boolean equalize) {
    StringConverter<Double> formatter =
        new StringConverter<>() {
          @Override
          public String toString(Double n) {
            if (n == null) {
              return "";
            }
            return SettingsDefaults.speedTickLabel((int) Math.round(n), equalize);
          }

          @Override
          public Double fromString(String s) {
            return Double.NaN;
          }
        };
    // JDK-8286653: skin ignores formatter changes unless cleared to null first.
    slider.setLabelFormatter(null);
    slider.setLabelFormatter(formatter);
  }

  private static boolean equalizeOn(DisplayViewModel displayVm) {
    return displayVm != null && displayVm.isEqualizeSortDuration();
  }

  private static String headerText(boolean equalize) {
    return equalize ? SettingsStrings.SPEED_DURATION_HEADER : SettingsStrings.SPEED_HEADER;
  }

  private static String slowText(boolean equalize) {
    return equalize ? SettingsStrings.SPEED_EQUALIZE_SLOW : SettingsStrings.SPEED_SLOW;
  }

  private static String fastText(boolean equalize) {
    return equalize ? SettingsStrings.SPEED_EQUALIZE_FAST : SettingsStrings.SPEED_FAST;
  }

  private static String formatValue(int level, boolean equalize) {
    if (equalize) {
      return String.format(
          SettingsStrings.SPEED_DURATION_VALUE_FORMAT,
          Math.round(SettingsDefaults.equalizedDurationSec(level)));
    }
    return String.format(
        SettingsStrings.SPEED_STEPS_VALUE_FORMAT, SettingsDefaults.stepsPerFrame(level));
  }
}
