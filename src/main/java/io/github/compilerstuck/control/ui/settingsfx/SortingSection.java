package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import io.github.compilerstuck.control.catalog.AlgorithmDescriptor;
import io.github.compilerstuck.control.config.ShuffleType;
import io.github.compilerstuck.control.ui.settingsfx.vm.AlgorithmViewModel;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Algorithm selection, run-all order dialog, and shuffle. */
public final class SortingSection {

  public static final String ROOT_ID = "section-sorting";
  public static final String CONFIGURE_ID = "settings-configure-order";

  private SortingSection() {}

  public static Node build(AlgorithmViewModel vm) {
    Map<String, String> nameToId = new HashMap<>();
    ComboBox<String> algorithm = new ComboBox<>();
    String selectedName = null;
    for (AlgorithmDescriptor descriptor : vm.getDescriptors()) {
      algorithm.getItems().add(descriptor.displayName());
      nameToId.put(descriptor.displayName(), descriptor.id());
      if (descriptor.id().equals(vm.getSelectedId())) {
        selectedName = descriptor.displayName();
      }
    }
    if (selectedName != null) {
      algorithm.getSelectionModel().select(selectedName);
    }
    algorithm.setMaxWidth(Double.MAX_VALUE);
    algorithm
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, old, name) -> {
              if (name == null) {
                return;
              }
              String id = nameToId.get(name);
              if (id != null && !id.equals(vm.getSelectedId())) {
                vm.selectAlgorithm(id);
              }
            });
    algorithm.setDisable(vm.isRunAll());

    VBox algorithmField = SettingsControls.labeledField(SettingsStrings.ALGORITHM, algorithm);

    ToggleSwitch runAll = new ToggleSwitch();
    runAll.setSelected(vm.isRunAll());
    runAll
        .selectedProperty()
        .addListener(
            (obs, old, selected) -> {
              if (selected != vm.isRunAll()) {
                vm.setRunAll(selected);
              }
            });

    Button configure = new Button(SettingsStrings.CONFIGURE_ORDER);
    configure.setId(CONFIGURE_ID);
    configure.getStyleClass().add(Styles.BUTTON_OUTLINED);
    configure.setDisable(!vm.isRunAll());
    configure.setOnAction(
        e -> {
          if (configure.getScene() != null && configure.getScene().getWindow() != null) {
            RunAllOrderDialog.show(configure.getScene().getWindow(), vm);
          }
        });

    Region runAllSpacer = new Region();
    HBox.setHgrow(runAllSpacer, Priority.ALWAYS);
    HBox runAllControls = new HBox(SettingsLayout.GAP_SM, runAll, runAllSpacer, configure);
    runAllControls.setAlignment(Pos.CENTER_LEFT);
    VBox runAllField =
        new VBox(
            SettingsLayout.GAP_XS,
            SettingsControls.fieldLabel(SettingsStrings.RUN_ALL),
            runAllControls);

    ComboBox<ShuffleType> shuffle = new ComboBox<>();
    shuffle.getItems().setAll(vm.getShuffleTypes());
    shuffle.getSelectionModel().select(vm.getShuffleType());
    shuffle.setMaxWidth(Double.MAX_VALUE);
    shuffle
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, old, type) -> {
              if (type != null && type != vm.getShuffleType()) {
                vm.setShuffleType(type);
              }
            });
    VBox shuffleField = SettingsControls.labeledField(SettingsStrings.SHUFFLE, shuffle);

    vm.addPropertyChangeListener(
        evt -> {
          if (AlgorithmViewModel.PROP_SELECTED_ID.equals(evt.getPropertyName())) {
            String id = String.valueOf(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  for (AlgorithmDescriptor d : vm.getDescriptors()) {
                    if (d.id().equals(id)
                        && !d.displayName()
                            .equals(algorithm.getSelectionModel().getSelectedItem())) {
                      algorithm.getSelectionModel().select(d.displayName());
                      break;
                    }
                  }
                });
          } else if (AlgorithmViewModel.PROP_RUN_ALL.equals(evt.getPropertyName())) {
            boolean value = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (runAll.isSelected() != value) {
                    runAll.setSelected(value);
                  }
                  algorithm.setDisable(value || !vm.isInputsEnabled());
                  configure.setDisable(!value || !vm.isInputsEnabled());
                });
          } else if (AlgorithmViewModel.PROP_SHUFFLE_TYPE.equals(evt.getPropertyName())) {
            ShuffleType type = (ShuffleType) evt.getNewValue();
            VmBindings.runFx(
                () -> {
                  if (shuffle.getSelectionModel().getSelectedItem() != type) {
                    shuffle.getSelectionModel().select(type);
                  }
                });
          } else if (AlgorithmViewModel.PROP_INPUTS_ENABLED.equals(evt.getPropertyName())) {
            boolean enabled = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  algorithm.setDisable(vm.isRunAll() || !enabled);
                  configure.setDisable(!vm.isRunAll() || !enabled);
                });
          }
        });

    VmBindings.bindInputsEnabled(
        runAll,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        AlgorithmViewModel.PROP_INPUTS_ENABLED);
    VmBindings.bindInputsEnabled(
        shuffle,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        AlgorithmViewModel.PROP_INPUTS_ENABLED);

    VBox root = new VBox(SettingsLayout.GAP_SM, algorithmField, runAllField, shuffleField);
    root.setId(ROOT_ID);
    return root;
  }
}
