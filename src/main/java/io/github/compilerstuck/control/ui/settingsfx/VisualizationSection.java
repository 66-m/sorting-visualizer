package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.theme.Styles;
import io.github.compilerstuck.control.catalog.VisualizationDescriptor;
import io.github.compilerstuck.control.ui.settingsfx.customize.VisualizationCustomizePanels;
import io.github.compilerstuck.control.ui.settingsfx.vm.VisualizationViewModel;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/** Visualization combo + Customize + optional image path validation (G3). */
public final class VisualizationSection {

  public static final String ROOT_ID = "section-visualization";
  public static final String CUSTOMIZE_BUTTON_ID = "visualization-customize";
  public static final String RESET_ALL_BUTTON_ID = "visualization-reset-all";
  public static final String RESET_ALL_CONFIRM_ID = "visualization-reset-all-confirm";

  private VisualizationSection() {}

  public static Node build(VisualizationViewModel vm) {
    Map<String, String> nameToId = new HashMap<>();
    ComboBox<String> combo = new ComboBox<>();
    String selectedName = null;
    for (VisualizationDescriptor descriptor : vm.getDescriptors()) {
      combo.getItems().add(descriptor.displayName());
      nameToId.put(descriptor.displayName(), descriptor.id());
      if (descriptor.id().equals(vm.getSelectedId())) {
        selectedName = descriptor.displayName();
      }
    }
    if (selectedName != null) {
      combo.getSelectionModel().select(selectedName);
    }
    combo.setMaxWidth(Double.MAX_VALUE);
    combo
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, old, name) -> {
              if (name == null) {
                return;
              }
              String id = nameToId.get(name);
              if (id != null && !id.equals(vm.getSelectedId())) {
                vm.selectVisualization(id);
              }
            });

    Button resetAll = new Button(SettingsStrings.RESET_ALL_VISUALS);
    resetAll.setId(RESET_ALL_BUTTON_ID);
    resetAll.getStyleClass().add("settings-secondary-action");
    resetAll.setTooltip(new Tooltip(SettingsStrings.RESET_ALL_VISUALS_TOOLTIP));
    resetAll.setOnAction(
        e -> {
          if (confirmResetAll(resetAll.getScene().getWindow())) {
            vm.resetAllCustomizations();
          }
        });
    StackPane resetAllHost = SettingsControls.wrapForDisabledTooltip(resetAll);
    Tooltip resetAllTip = SettingsControls.disabledTooltip();

    Button customize = new Button(SettingsStrings.CUSTOMIZE);
    customize.setId(CUSTOMIZE_BUTTON_ID);
    customize.getStyleClass().add(Styles.BUTTON_OUTLINED);
    customize.setOnAction(
        e -> VisualizationCustomizeDialog.show(customize.getScene().getWindow(), vm));
    StackPane customizeHost = SettingsControls.wrapForDisabledTooltip(customize);
    Tooltip customizeTip = SettingsControls.disabledTooltip();

    syncActionButtons(
        customize, customizeHost, customizeTip, resetAll, resetAllHost, resetAllTip, vm);

    HBox actions = new HBox(SettingsLayout.GAP_SM, customizeHost, resetAllHost);
    actions.setAlignment(Pos.CENTER_LEFT);
    HBox comboRow = SettingsControls.controlWithAction(combo, actions);

    TextField path = new TextField(vm.getImagePath());
    path.setPromptText(SettingsStrings.IMAGE_PATH_PROMPT);
    path.setMaxWidth(Double.MAX_VALUE);

    Button browse = new Button(SettingsStrings.BROWSE);
    browse.getStyleClass().add(Styles.BUTTON_OUTLINED);
    browse.setOnAction(
        e -> {
          File file = SafeFileDialogs.chooseImageFile();
          if (file != null) {
            path.setText(file.getAbsolutePath());
            vm.setImagePath(Path.of(file.getAbsolutePath()));
          }
        });

    path.setOnAction(e -> vm.setImagePath(Path.of(path.getText().trim())));
    path.focusedProperty()
        .addListener(
            (obs, wasFocused, isFocused) -> {
              if (wasFocused && !isFocused) {
                vm.setImagePath(Path.of(path.getText().trim()));
              }
            });

    Label error = new Label(vm.getImageError());
    error.getStyleClass().add("settings-inline-error");
    boolean hasError = vm.getImageError() != null && !vm.getImageError().isBlank();
    error.setVisible(hasError);
    error.setManaged(hasError);

    HBox imageRow = SettingsControls.controlWithAction(path, browse);
    Label imageLabel = SettingsControls.fieldLabel(SettingsStrings.IMAGE);
    imageLabel.setLabelFor(path);
    VBox imageBlock = new VBox(SettingsLayout.GAP_XS, imageLabel, imageRow, error);
    imageBlock.setVisible(vm.needsImage());
    imageBlock.setManaged(vm.needsImage());

    vm.addPropertyChangeListener(
        evt -> {
          if (VisualizationViewModel.PROP_SELECTED_ID.equals(evt.getPropertyName())) {
            String id = String.valueOf(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  for (VisualizationDescriptor d : vm.getDescriptors()) {
                    if (d.id().equals(id)
                        && !d.displayName().equals(combo.getSelectionModel().getSelectedItem())) {
                      combo.getSelectionModel().select(d.displayName());
                      break;
                    }
                  }
                  syncActionButtons(
                      customize,
                      customizeHost,
                      customizeTip,
                      resetAll,
                      resetAllHost,
                      resetAllTip,
                      vm);
                });
          } else if (VisualizationViewModel.PROP_CONFIGURABLE.equals(evt.getPropertyName())) {
            VmBindings.runFx(
                () ->
                    syncActionButtons(
                        customize,
                        customizeHost,
                        customizeTip,
                        resetAll,
                        resetAllHost,
                        resetAllTip,
                        vm));
          } else if (VisualizationViewModel.PROP_NEEDS_IMAGE.equals(evt.getPropertyName())) {
            boolean needs = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  imageBlock.setVisible(needs);
                  imageBlock.setManaged(needs);
                });
          } else if (VisualizationViewModel.PROP_IMAGE_PATH.equals(evt.getPropertyName())) {
            String value = String.valueOf(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (!path.getText().equals(value)) {
                    path.setText(value);
                  }
                });
          } else if (VisualizationViewModel.PROP_IMAGE_ERROR.equals(evt.getPropertyName())) {
            String msg = evt.getNewValue() == null ? "" : String.valueOf(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  error.setText(msg);
                  boolean show = !msg.isBlank();
                  error.setVisible(show);
                  error.setManaged(show);
                });
          } else if (VisualizationViewModel.PROP_INPUTS_ENABLED.equals(evt.getPropertyName())) {
            VmBindings.runFx(
                () ->
                    syncActionButtons(
                        customize,
                        customizeHost,
                        customizeTip,
                        resetAll,
                        resetAllHost,
                        resetAllTip,
                        vm));
          }
        });

    VmBindings.bindInputsEnabled(
        combo,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        VisualizationViewModel.PROP_INPUTS_ENABLED);
    VmBindings.bindInputsEnabled(
        path,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        VisualizationViewModel.PROP_INPUTS_ENABLED);
    VmBindings.bindInputsEnabled(
        browse,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        VisualizationViewModel.PROP_INPUTS_ENABLED);

    VBox root = new VBox(SettingsLayout.GAP_SM, comboRow, imageBlock);
    root.setId(ROOT_ID);
    return root;
  }

  private static boolean canCustomize(VisualizationViewModel vm) {
    return vm.isConfigurable() && VisualizationCustomizePanels.hasPanel(vm.getSelectedId());
  }

  private static void syncActionButtons(
      Button customize,
      StackPane customizeHost,
      Tooltip customizeTip,
      Button resetAll,
      StackPane resetAllHost,
      Tooltip resetAllTip,
      VisualizationViewModel vm) {
    boolean inputsEnabled = vm.isInputsEnabled();
    boolean available = canCustomize(vm);

    customize.setDisable(!available || !inputsEnabled);
    String customizeReason = null;
    if (!inputsEnabled) {
      customizeReason = SettingsStrings.CUSTOMIZE_BUSY_TOOLTIP;
    } else if (!available) {
      customizeReason = SettingsStrings.CUSTOMIZE_UNAVAILABLE_TOOLTIP;
    }
    SettingsControls.setDisabledTooltip(customizeHost, customizeTip, customizeReason);

    resetAll.setDisable(!inputsEnabled);
    SettingsControls.setDisabledTooltip(
        resetAllHost,
        resetAllTip,
        inputsEnabled ? null : SettingsStrings.RESET_ALL_VISUALS_BUSY_TOOLTIP);
  }

  private static boolean confirmResetAll(Window owner) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    if (owner != null) {
      alert.initOwner(owner);
    }
    alert.setTitle(SettingsStrings.RESET_ALL_VISUALS_TITLE);
    alert.setHeaderText(null);
    alert.setContentText(SettingsStrings.RESET_ALL_VISUALS_MESSAGE);
    alert.getDialogPane().setId(RESET_ALL_CONFIRM_ID);

    ButtonType reset =
        new ButtonType(SettingsStrings.RESET_ALL_VISUALS_CONFIRM, ButtonBar.ButtonData.OK_DONE);
    ButtonType cancel = new ButtonType(SettingsStrings.CANCEL, ButtonBar.ButtonData.CANCEL_CLOSE);
    alert.getButtonTypes().setAll(cancel, reset);

    var css = SettingsStylesheets.cssUrl();
    if (css != null) {
      alert.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    return alert.showAndWait().filter(reset::equals).isPresent();
  }
}
