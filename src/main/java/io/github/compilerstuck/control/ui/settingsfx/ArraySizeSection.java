package io.github.compilerstuck.control.ui.settingsfx;

import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.control.ui.settingsfx.vm.ArraySizeViewModel;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Array size slider + live value + validated text field. */
public final class ArraySizeSection {

  public static final String ROOT_ID = "section-array-size";
  public static final String FPS_WARNING_ID = "array-size-fps-warning";
  public static final String HIGH_SIZE_WARNING_ID = "array-size-high-warning";

  private static final Duration FPS_POLL_INTERVAL = Duration.millis(500);

  private ArraySizeSection() {}

  public static Node build(ArraySizeViewModel vm) {
    Label value = SettingsControls.valueLabel();
    value.setText(formatSize(vm.getSize()));

    Slider slider =
        new Slider(SettingsDefaults.ARRAY_SIZE_MIN, SettingsDefaults.ARRAY_SIZE_MAX, vm.getSize());
    slider.setMaxWidth(Double.MAX_VALUE);
    slider.setBlockIncrement(1);
    slider.setMajorTickUnit(10_000);
    slider.setShowTickMarks(false);
    slider
        .valueProperty()
        .addListener(
            (obs, old, v) -> {
              int size = (int) Math.round(v.doubleValue());
              value.setText(formatSize(size));
              if (size != vm.getSize()) {
                vm.setSizeFromSlider(size);
              }
            });

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox header =
        new HBox(
            SettingsLayout.GAP_SM, SettingsControls.fieldLabel(SettingsStrings.SIZE), spacer, value);
    header.setAlignment(Pos.CENTER_LEFT);

    TextField text = new TextField(vm.getText());
    text.setPrefColumnCount(7);
    text.textProperty()
        .addListener(
            (obs, old, v) -> {
              if (!v.equals(vm.getText())) {
                vm.setText(v);
              }
            });

    Button apply = new Button(SettingsStrings.APPLY);
    apply.setOnAction(e -> vm.applyText());
    apply.setDisable(!vm.isTextValid());

    Label error = new Label(vm.getValidationMessage());
    error.getStyleClass().add("settings-inline-error");
    error.setVisible(!vm.isTextValid());
    error.setManaged(!vm.isTextValid());

    Label highSizeWarning = new Label(SettingsStrings.ARRAY_SIZE_HIGH_WARNING);
    highSizeWarning.setId(HIGH_SIZE_WARNING_ID);
    highSizeWarning.getStyleClass().add("settings-inline-warning");
    highSizeWarning.setWrapText(true);
    highSizeWarning.setVisible(vm.isHighSizeWarning());
    highSizeWarning.setManaged(vm.isHighSizeWarning());

    Label fpsWarning = new Label(SettingsStrings.ARRAY_SIZE_FPS_WARNING);
    fpsWarning.setId(FPS_WARNING_ID);
    fpsWarning.getStyleClass().add("settings-inline-warning");
    fpsWarning.setWrapText(true);
    fpsWarning.setVisible(vm.isFpsWarning());
    fpsWarning.setManaged(vm.isFpsWarning());

    Timeline fpsPoll =
        new Timeline(new KeyFrame(FPS_POLL_INTERVAL, e -> vm.refreshFpsWarning()));
    fpsPoll.setCycleCount(Animation.INDEFINITE);

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
                  value.setText(formatSize(size));
                });
          } else if (ArraySizeViewModel.PROP_TEXT.equals(prop)) {
            String next = String.valueOf(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (!text.getText().equals(next)) {
                    text.setText(next);
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
          } else if (ArraySizeViewModel.PROP_HIGH_SIZE_WARNING.equals(prop)) {
            boolean warn = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  highSizeWarning.setVisible(warn);
                  highSizeWarning.setManaged(warn);
                });
          } else if (ArraySizeViewModel.PROP_FPS_WARNING.equals(prop)) {
            boolean warn = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  fpsWarning.setVisible(warn);
                  fpsWarning.setManaged(warn);
                });
          } else if (ArraySizeViewModel.PROP_INPUTS_ENABLED.equals(prop)) {
            boolean enabled = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (enabled) {
                    fpsPoll.play();
                  } else {
                    fpsPoll.stop();
                  }
                });
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

    HBox precision = new HBox(SettingsLayout.GAP_SM, text, apply);
    precision.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(text, Priority.ALWAYS);

    VBox root =
        new VBox(
            SettingsLayout.GAP_SM, header, slider, precision, error, highSizeWarning, fpsWarning);
    root.setId(ROOT_ID);
    root.sceneProperty()
        .addListener(
            (obs, oldScene, newScene) -> {
              if (newScene != null && vm.isInputsEnabled()) {
                fpsPoll.play();
              } else {
                fpsPoll.stop();
              }
            });
    return root;
  }

  private static String formatSize(int size) {
    return String.format("%,d", size);
  }
}
