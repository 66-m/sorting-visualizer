package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.ScatterPlotSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
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
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(pointSize, false);
    CustomizePanelSupport.bindValueLabel(pointSize, pointSizeValue, v -> String.format("%.1f", v));
    draft.bind(pointSize.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Point size", pointSize, pointSizeValue, ScatterPlotSettings.DEFAULT_POINT_SIZE));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    ScatterPlotSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, ScatterPlotSettings.class, ScatterPlotSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          pointSize.setValue(s.pointSize());
        });
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
    draft.setListener(listener);
  }
}
