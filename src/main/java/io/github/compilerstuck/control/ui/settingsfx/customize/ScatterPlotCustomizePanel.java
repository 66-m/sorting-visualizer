package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.ScatterPlotSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link ScatterPlotSettings}. */
public final class ScatterPlotCustomizePanel implements VisualizationCustomizePanel {

  private final Slider pointSize =
      new Slider(
          ScatterPlotSettings.POINT_SIZE_MIN,
          ScatterPlotSettings.POINT_SIZE_MAX,
          ScatterPlotSettings.DEFAULT_POINT_SIZE);
  private final Label pointSizeValue = CustomizePanelSupport.valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(pointSize, false);
    CustomizePanelSupport.bindValueLabel(pointSize, pointSizeValue, v -> String.format("%.1f", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Point size", pointSize, pointSizeValue, ScatterPlotSettings.DEFAULT_POINT_SIZE));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    ScatterPlotSettings s =
        settings instanceof ScatterPlotSettings c ? c : ScatterPlotSettings.defaults();
    loading = true;
    try {
      pointSize.setValue(s.pointSize());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new ScatterPlotSettings(pointSize.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return ScatterPlotSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    pointSize.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
