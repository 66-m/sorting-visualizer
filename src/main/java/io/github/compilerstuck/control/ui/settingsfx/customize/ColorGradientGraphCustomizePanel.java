package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.ColorGradientGraphSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

/** Draft editor for {@link ColorGradientGraphSettings}. */
public final class ColorGradientGraphCustomizePanel implements VisualizationCustomizePanel {

  private final CheckBox showIndexDividers = new CheckBox();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {

    draft.bind(showIndexDividers.selectedProperty());

    VBox section =
        CustomizePanelSupport.section(
            "DISPLAY",
            CustomizePanelSupport.checkboxRow(
                "Index dividers",
                showIndexDividers,
                ColorGradientGraphSettings.DEFAULT_SHOW_INDEX_DIVIDERS));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    ColorGradientGraphSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, ColorGradientGraphSettings.class, ColorGradientGraphSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          showIndexDividers.setSelected(s.showIndexDividers());
        });
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
    draft.setListener(listener);
  }
}
