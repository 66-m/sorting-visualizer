package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.ColorGradientGraphSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

/** Draft editor for {@link ColorGradientGraphSettings}. */
public final class ColorGradientGraphCustomizePanel implements VisualizationCustomizePanel {

  private final CheckBox showIndexDividers = new CheckBox();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {

    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "DISPLAY",
            CustomizePanelSupport.checkboxRow(
                "Index dividers",
                showIndexDividers,
                ColorGradientGraphSettings.DEFAULT_SHOW_INDEX_DIVIDERS));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    ColorGradientGraphSettings s =
        settings instanceof ColorGradientGraphSettings c
            ? c
            : ColorGradientGraphSettings.defaults();
    loading = true;
    try {
      showIndexDividers.setSelected(s.showIndexDividers());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new ColorGradientGraphSettings(showIndexDividers.isSelected());
  }

  @Override
  public VisualizationSettings defaults() {
    return ColorGradientGraphSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    showIndexDividers.selectedProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
