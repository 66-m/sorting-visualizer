package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import io.github.compilerstuck.control.catalog.AlgorithmDescriptor;
import io.github.compilerstuck.control.config.ShuffleType;
import io.github.compilerstuck.control.ui.settingsfx.vm.AlgorithmEntry;
import io.github.compilerstuck.control.ui.settingsfx.vm.AlgorithmViewModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Algorithm selection, run-all inline order list, and shuffle. */
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

    ToggleSwitch runAll = new ToggleSwitch(SettingsStrings.RUN_ALL);
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

    VBox orderList = new VBox(SettingsLayout.GAP_XS);
    orderList.getStyleClass().add("settings-run-all-list");
    orderList.setVisible(false);
    orderList.setManaged(false);
    rebuildOrderList(orderList, vm);

    configure.setOnAction(
        e -> {
          boolean show = !orderList.isVisible();
          orderList.setVisible(show);
          orderList.setManaged(show);
          configure.setText(show ? SettingsStrings.HIDE_ORDER : SettingsStrings.CONFIGURE_ORDER);
        });

    Label shuffleLabel = new Label(SettingsStrings.SHUFFLE);
    shuffleLabel.getStyleClass().add("settings-muted");
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
                  if (!value) {
                    orderList.setVisible(false);
                    orderList.setManaged(false);
                    configure.setText(SettingsStrings.CONFIGURE_ORDER);
                  }
                });
          } else if (AlgorithmViewModel.PROP_SHUFFLE_TYPE.equals(evt.getPropertyName())) {
            ShuffleType type = (ShuffleType) evt.getNewValue();
            VmBindings.runFx(
                () -> {
                  if (shuffle.getSelectionModel().getSelectedItem() != type) {
                    shuffle.getSelectionModel().select(type);
                  }
                });
          } else if (AlgorithmViewModel.PROP_ENTRIES.equals(evt.getPropertyName())) {
            VmBindings.runFx(() -> rebuildOrderList(orderList, vm));
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

    VBox algorithmGroup = new VBox(SettingsLayout.GAP_SM, algorithm, runAll);
    VBox orderGroup = new VBox(SettingsLayout.GAP_SM, configure, orderList);
    VBox shuffleGroup = new VBox(SettingsLayout.GAP_SM, shuffleLabel, shuffle);

    VBox root = new VBox(SettingsLayout.GAP_LG, algorithmGroup, orderGroup, shuffleGroup);
    root.setId(ROOT_ID);
    return root;
  }

  private static void rebuildOrderList(VBox orderList, AlgorithmViewModel vm) {
    orderList.getChildren().clear();
    List<AlgorithmEntry> entries = vm.getEntries();
    for (int i = 0; i < entries.size(); i++) {
      final int index = i;
      AlgorithmEntry entry = entries.get(i);

      CheckBox selected = new CheckBox();
      selected.setSelected(entry.isSelected());
      selected.setDisable(!vm.isInputsEnabled());
      selected
          .selectedProperty()
          .addListener(
              (obs, old, value) -> {
                if (value != entry.isSelected()) {
                  vm.setEntrySelected(index, value);
                }
              });

      Label name = new Label(entry.getName());
      HBox.setHgrow(name, Priority.ALWAYS);

      Button up = new Button(SettingsStrings.MOVE_UP);
      up.getStyleClass().addAll(Styles.BUTTON_OUTLINED, "settings-icon-button");
      up.setDisable(index == 0 || !vm.isInputsEnabled());
      up.setOnAction(e -> vm.moveEntry(index, index - 1));

      Button down = new Button(SettingsStrings.MOVE_DOWN);
      down.getStyleClass().addAll(Styles.BUTTON_OUTLINED, "settings-icon-button");
      down.setDisable(index == entries.size() - 1 || !vm.isInputsEnabled());
      down.setOnAction(e -> vm.moveEntry(index, index + 1));

      HBox row = new HBox(SettingsLayout.GAP_SM, selected, name, up, down);
      row.setAlignment(Pos.CENTER_LEFT);
      row.getStyleClass().add("settings-run-all-row");
      orderList.getChildren().add(row);
    }
  }
}
