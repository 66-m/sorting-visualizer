package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.NumberPlotSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
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
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(fontSize, false);
    CustomizePanelSupport.bindValueLabel(fontSize, fontSizeValue, v -> String.format("%.0f", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "DISPLAY",
            CustomizePanelSupport.sliderRow(
                "Font size", fontSize, fontSizeValue, NumberPlotSettings.DEFAULT_FONT_SIZE));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    NumberPlotSettings s =
        settings instanceof NumberPlotSettings c ? c : NumberPlotSettings.defaults();
    loading = true;
    try {
      fontSize.setValue(s.fontSize());
    } finally {
      loading = false;
    }
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
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    fontSize.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
