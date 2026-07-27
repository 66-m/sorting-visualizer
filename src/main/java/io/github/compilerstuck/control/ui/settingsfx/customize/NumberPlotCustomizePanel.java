package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.NumberPlotSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link NumberPlotSettings}. */
public final class NumberPlotCustomizePanel implements VisualizationCustomizePanel {

  private final Slider fontSize =
      new Slider(
          NumberPlotSettings.FONT_SIZE_MIN,
          NumberPlotSettings.FONT_SIZE_MAX,
          NumberPlotSettings.DEFAULT_FONT_SIZE);
  private final Label fontSizeValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(fontSize, false);
    CustomizePanelSupport.bindValueLabel(fontSize, fontSizeValue, v -> String.format("%.0f", v));
    draft.bind(fontSize.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "DISPLAY",
            CustomizePanelSupport.sliderRow(
                "Font size", fontSize, fontSizeValue, NumberPlotSettings.DEFAULT_FONT_SIZE));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    NumberPlotSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, NumberPlotSettings.class, NumberPlotSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          fontSize.setValue(s.fontSize());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new NumberPlotSettings(fontSize.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return NumberPlotSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
