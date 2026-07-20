package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.theme.Styles;
import io.github.compilerstuck.control.catalog.VisualizationDescriptor;
import io.github.compilerstuck.control.ui.settingsfx.customize.VisualizationCustomizePanels;
import io.github.compilerstuck.control.ui.settingsfx.vm.VisualizationViewModel;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/** Visualization combo + Customize + optional image path validation (G3). */
public final class VisualizationSection {

  public static final String ROOT_ID = "section-visualization";
  public static final String CUSTOMIZE_BUTTON_ID = "visualization-customize";

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

    Button customize = new Button(SettingsStrings.CUSTOMIZE);
    customize.setId(CUSTOMIZE_BUTTON_ID);
    customize.getStyleClass().add(Styles.BUTTON_OUTLINED);
    customize.setDisable(!canCustomize(vm));
    customize.setOnAction(
        e -> VisualizationCustomizeDialog.show(customize.getScene().getWindow(), vm));

    HBox comboRow = SettingsControls.controlWithAction(combo, customize);

    TextField path = new TextField(vm.getImagePath());
    path.setPromptText(SettingsStrings.IMAGE_PATH_PROMPT);
    path.setMaxWidth(Double.MAX_VALUE);

    Button browse = new Button(SettingsStrings.BROWSE);
    browse.getStyleClass().add(Styles.BUTTON_OUTLINED);
    browse.setOnAction(
        e -> {
          FileChooser chooser = new FileChooser();
          chooser.setTitle(SettingsStrings.BROWSE);
          chooser
              .getExtensionFilters()
              .add(
                  new FileChooser.ExtensionFilter(
                      "Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
          File file = chooser.showOpenDialog(browse.getScene().getWindow());
          if (file != null) {
            path.setText(file.getAbsolutePath());
            vm.setImagePath(Path.of(file.getAbsolutePath()));
          }
        });

    path.setOnAction(e -> vm.setImagePath(Path.of(path.getText().trim())));

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
                  customize.setDisable(!canCustomize(vm) || !vm.isInputsEnabled());
                });
          } else if (VisualizationViewModel.PROP_CONFIGURABLE.equals(evt.getPropertyName())) {
            VmBindings.runFx(
                () -> customize.setDisable(!canCustomize(vm) || !vm.isInputsEnabled()));
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
                () -> customize.setDisable(!canCustomize(vm) || !vm.isInputsEnabled()));
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
}
