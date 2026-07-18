package io.github.compilerstuck.control.ui.settingsfx;

import io.github.compilerstuck.control.ui.settingsfx.vm.AppearanceViewModel;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** Gradient preset combo + custom color pickers. */
public final class AppearanceSection {

  public static final String ROOT_ID = "section-appearance";

  private AppearanceSection() {}

  public static Node build(AppearanceViewModel vm) {
    ComboBox<String> presets = new ComboBox<>();
    List<String> names = vm.getPresetNames();
    presets.getItems().setAll(names);
    presets.getSelectionModel().select(vm.getSelectedIndex());
    presets.setMaxWidth(Double.MAX_VALUE);
    presets
        .getSelectionModel()
        .selectedIndexProperty()
        .addListener(
            (obs, old, index) -> {
              if (index != null
                  && index.intValue() >= 0
                  && index.intValue() != vm.getSelectedIndex()) {
                vm.selectPreset(index.intValue());
              }
            });

    ColorPicker color1 = new ColorPicker(toFx(vm.getColor1()));
    ColorPicker color2 = new ColorPicker(toFx(vm.getColor2()));
    color1.setOnAction(e -> vm.setCustomColors(toAwt(color1.getValue()), toAwt(color2.getValue())));
    color2.setOnAction(e -> vm.setCustomColors(toAwt(color1.getValue()), toAwt(color2.getValue())));

    vm.addPropertyChangeListener(
        evt -> {
          if (AppearanceViewModel.PROP_SELECTED_INDEX.equals(evt.getPropertyName())) {
            int index = (Integer) evt.getNewValue();
            VmBindings.runFx(
                () -> {
                  if (presets.getSelectionModel().getSelectedIndex() != index) {
                    presets.getSelectionModel().select(index);
                  }
                });
          } else if (AppearanceViewModel.PROP_COLOR1.equals(evt.getPropertyName())) {
            Color fx = toFx((java.awt.Color) evt.getNewValue());
            VmBindings.runFx(() -> color1.setValue(fx));
          } else if (AppearanceViewModel.PROP_COLOR2.equals(evt.getPropertyName())) {
            Color fx = toFx((java.awt.Color) evt.getNewValue());
            VmBindings.runFx(() -> color2.setValue(fx));
          }
        });

    VmBindings.bindInputsEnabled(
        presets,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        AppearanceViewModel.PROP_INPUTS_ENABLED);
    VmBindings.bindInputsEnabled(
        color1,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        AppearanceViewModel.PROP_INPUTS_ENABLED);
    VmBindings.bindInputsEnabled(
        color2,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        AppearanceViewModel.PROP_INPUTS_ENABLED);

    HBox swatches = new HBox(SettingsLayout.GAP_SM, color1, color2);
    Label hint = new Label(SettingsStrings.SWATCH_HINT);
    hint.getStyleClass().add("settings-muted");

    VBox root = new VBox(SettingsLayout.GAP_SM, presets, swatches, hint);
    root.setId(ROOT_ID);
    return root;
  }

  private static Color toFx(java.awt.Color c) {
    if (c == null) {
      return Color.BLACK;
    }
    return Color.rgb(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 255.0);
  }

  private static java.awt.Color toAwt(Color c) {
    return new java.awt.Color(
        (float) c.getRed(), (float) c.getGreen(), (float) c.getBlue(), (float) c.getOpacity());
  }
}
