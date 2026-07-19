package io.github.compilerstuck.control.ui.settingsfx;

import io.github.compilerstuck.control.ui.settingsfx.vm.DebugViewModel;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

/** Debug toggles bound to {@link DebugViewModel}. */
public final class DebugSection {

  public static final String ROOT_ID = "section-debug";

  private DebugSection() {}

  public static Node build(DebugViewModel vm) {
    CheckBox perfStats = new CheckBox(SettingsStrings.SHOW_PERF_STATS);
    perfStats.setSelected(vm.isPerfStats());
    perfStats
        .selectedProperty()
        .addListener(
            (obs, old, selected) -> {
              if (selected != vm.isPerfStats()) {
                vm.setPerfStats(selected);
              }
            });

    vm.addPropertyChangeListener(
        evt -> {
          if (DebugViewModel.PROP_PERF_STATS.equals(evt.getPropertyName())) {
            boolean value = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (perfStats.isSelected() != value) {
                    perfStats.setSelected(value);
                  }
                });
          }
        });

    VBox root = new VBox(SettingsLayout.GAP_SM, perfStats);
    root.setId(ROOT_ID);
    return root;
  }
}
