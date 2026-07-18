package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.theme.Styles;
import io.github.compilerstuck.control.ui.settingsfx.vm.DisplayViewModel;
import java.io.File;
import java.nio.file.Path;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/** Display toggles and CSV export bound to {@link DisplayViewModel}. */
public final class DisplaySection {

  public static final String ROOT_ID = "section-display";
  public static final String EXPORT_ID = "settings-export-csv";

  private DisplaySection() {}

  public static Node build(DisplayViewModel vm) {
    CheckBox measurements = new CheckBox(SettingsStrings.SHOW_MEASUREMENTS);
    measurements.setSelected(vm.isPrintMeasurements());
    measurements
        .selectedProperty()
        .addListener(
            (obs, old, selected) -> {
              if (selected != vm.isPrintMeasurements()) {
                vm.setPrintMeasurements(selected);
              }
            });

    CheckBox comparison = new CheckBox(SettingsStrings.SHOW_COMPARISON_TABLE);
    comparison.setSelected(vm.isShowComparisonTable());
    comparison
        .selectedProperty()
        .addListener(
            (obs, old, selected) -> {
              if (selected != vm.isShowComparisonTable()) {
                vm.setShowComparisonTable(selected);
              }
            });

    Button export = new Button(SettingsStrings.EXPORT_CSV);
    export.setId(EXPORT_ID);
    export.getStyleClass().add(Styles.BUTTON_OUTLINED);
    export.setDisable(!vm.canExport());
    export.setOnAction(
        e -> {
          FileChooser chooser = new FileChooser();
          chooser.setTitle(SettingsStrings.EXPORT_CSV);
          chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
          chooser.setInitialFileName("sorting-results.csv");
          File file = chooser.showSaveDialog(export.getScene().getWindow());
          if (file != null) {
            vm.exportCsv(Path.of(file.getAbsolutePath()));
          }
        });

    vm.addPropertyChangeListener(
        evt -> {
          if (DisplayViewModel.PROP_PRINT_MEASUREMENTS.equals(evt.getPropertyName())) {
            boolean value = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (measurements.isSelected() != value) {
                    measurements.setSelected(value);
                  }
                });
          } else if (DisplayViewModel.PROP_SHOW_COMPARISON_TABLE.equals(evt.getPropertyName())) {
            boolean value = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (comparison.isSelected() != value) {
                    comparison.setSelected(value);
                  }
                });
          } else if (DisplayViewModel.PROP_CAN_EXPORT.equals(evt.getPropertyName())) {
            boolean can = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(() -> export.setDisable(!can));
          }
        });

    VmBindings.bindInputsEnabled(
        measurements,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        DisplayViewModel.PROP_INPUTS_ENABLED);
    VmBindings.bindInputsEnabled(
        comparison,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        DisplayViewModel.PROP_INPUTS_ENABLED);

    VBox toggles = new VBox(SettingsLayout.GAP_SM, measurements, comparison);
    VBox root = new VBox(SettingsLayout.GAP_MD, toggles, export);
    root.setId(ROOT_ID);
    return root;
  }
}
