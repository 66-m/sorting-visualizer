package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.ScatterPlotLinkedSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link ScatterPlotLinkedSettings}. */
public final class ScatterPlotLinkedCustomizePanel implements VisualizationCustomizePanel {

  private final Slider lineThickness =
      new Slider(
          ScatterPlotLinkedSettings.LINE_THICKNESS_MIN,
          ScatterPlotLinkedSettings.LINE_THICKNESS_MAX,
          ScatterPlotLinkedSettings.DEFAULT_LINE_THICKNESS);
  private final Label lineThicknessValue = CustomizePanelSupport.valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(lineThickness, false);
    CustomizePanelSupport.bindValueLabel(
        lineThickness, lineThicknessValue, v -> String.format("%.2f", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Line thickness",
                lineThickness,
                lineThicknessValue,
                ScatterPlotLinkedSettings.DEFAULT_LINE_THICKNESS));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    ScatterPlotLinkedSettings s =
        settings instanceof ScatterPlotLinkedSettings c ? c : ScatterPlotLinkedSettings.defaults();
    loading = true;
    try {
      lineThickness.setValue(s.lineThickness());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new ScatterPlotLinkedSettings(lineThickness.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return ScatterPlotLinkedSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    lineThickness.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
