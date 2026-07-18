package io.github.compilerstuck.control.ui.settingsfx;

import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.control.ui.settingsfx.vm.ArraySizeViewModel;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Array size slider + validated text field. */
public final class ArraySizeSection {

  public static final String ROOT_ID = "section-array-size";

  private ArraySizeSection() {}

  public static Node build(ArraySizeViewModel vm) {
    Slider slider =
        new Slider(SettingsDefaults.ARRAY_SIZE_MIN, SettingsDefaults.ARRAY_SIZE_MAX, vm.getSize());
    slider.setBlockIncrement(1);
    slider.setMajorTickUnit(5000);
    slider.setShowTickMarks(false);
    slider
        .valueProperty()
        .addListener(
            (obs, old, value) -> {
              int size = (int) Math.round(value.doubleValue());
              if (size != vm.getSize()) {
                vm.setSizeFromSlider(size);
              }
            });

    TextField text = new TextField(vm.getText());
    text.setPrefColumnCount(6);
    text.textProperty()
        .addListener(
            (obs, old, value) -> {
              if (!value.equals(vm.getText())) {
                vm.setText(value);
              }
            });

    Button apply = new Button(SettingsStrings.APPLY);
    apply.setOnAction(e -> vm.applyText());
    apply.setDisable(!vm.isTextValid());

    Label error = new Label(vm.getValidationMessage());
    error.getStyleClass().add("settings-inline-error");
    error.setVisible(!vm.isTextValid());
    error.setManaged(!vm.isTextValid());

    vm.addPropertyChangeListener(
        evt -> {
          String prop = evt.getPropertyName();
          if (ArraySizeViewModel.PROP_SIZE.equals(prop)) {
            int size = (Integer) evt.getNewValue();
            VmBindings.runFx(
                () -> {
                  if ((int) Math.round(slider.getValue()) != size) {
                    slider.setValue(size);
                  }
                });
          } else if (ArraySizeViewModel.PROP_TEXT.equals(prop)) {
            String value = String.valueOf(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (!text.getText().equals(value)) {
                    text.setText(value);
                  }
                });
          } else if (ArraySizeViewModel.PROP_TEXT_VALID.equals(prop)) {
            boolean valid = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  apply.setDisable(!valid);
                  error.setVisible(!valid);
                  error.setManaged(!valid);
                });
          } else if (ArraySizeViewModel.PROP_VALIDATION_MESSAGE.equals(prop)) {
            String msg = String.valueOf(evt.getNewValue());
            VmBindings.runFx(() -> error.setText(msg == null ? "" : msg));
          }
        });

    VmBindings.bindInputsEnabled(
        slider,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        ArraySizeViewModel.PROP_INPUTS_ENABLED);
    VmBindings.bindInputsEnabled(
        text,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        ArraySizeViewModel.PROP_INPUTS_ENABLED);
    VmBindings.bindInputsEnabled(
        apply,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        ArraySizeViewModel.PROP_INPUTS_ENABLED);

    HBox row = new HBox(SettingsLayout.GAP_SM, text, apply);
    row.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(text, Priority.ALWAYS);

    VBox root = new VBox(SettingsLayout.GAP_SM, slider, row, error);
    root.setId(ROOT_ID);
    return root;
  }
}
