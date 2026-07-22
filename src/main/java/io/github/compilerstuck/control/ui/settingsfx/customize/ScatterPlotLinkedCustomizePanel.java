package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.ScatterPlotLinkedSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
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
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(lineThickness, false);
    CustomizePanelSupport.bindValueLabel(
        lineThickness, lineThicknessValue, v -> String.format("%.2f", v));
    draft.bind(lineThickness.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Line thickness",
                lineThickness,
                lineThicknessValue,
                ScatterPlotLinkedSettings.DEFAULT_LINE_THICKNESS));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    ScatterPlotLinkedSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, ScatterPlotLinkedSettings.class, ScatterPlotLinkedSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          lineThickness.setValue(s.lineThickness());
        });
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
    draft.setListener(listener);
  }
}
