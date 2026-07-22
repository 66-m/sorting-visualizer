package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.ImageVerticalSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link ImageVerticalSettings}. */
public final class ImageVerticalCustomizePanel implements VisualizationCustomizePanel {

  private final ComboBox<ImageVerticalSettings.FitMode> fitMode = new ComboBox<>();
  private final Slider highlightStrength =
      new Slider(
          ImageVerticalSettings.HIGHLIGHT_STRENGTH_MIN,
          ImageVerticalSettings.HIGHLIGHT_STRENGTH_MAX,
          ImageVerticalSettings.DEFAULT_HIGHLIGHT_STRENGTH);
  private final Label highlightStrengthValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    fitMode.getItems().setAll(ImageVerticalSettings.FitMode.values());
    fitMode.getSelectionModel().select(ImageVerticalSettings.DEFAULT_FIT_MODE);
    CustomizePanelSupport.configureSlider(highlightStrength, false);
    CustomizePanelSupport.bindValueLabel(
        highlightStrength, highlightStrengthValue, v -> String.format("%.2f", v));
    draft.bind(
        fitMode.getSelectionModel().selectedItemProperty(), highlightStrength.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "IMAGE",
            CustomizePanelSupport.comboRow(
                "Fit mode", fitMode, ImageVerticalSettings.DEFAULT_FIT_MODE),
            CustomizePanelSupport.sliderRow(
                "Highlight strength",
                highlightStrength,
                highlightStrengthValue,
                ImageVerticalSettings.DEFAULT_HIGHLIGHT_STRENGTH));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    ImageVerticalSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, ImageVerticalSettings.class, ImageVerticalSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          fitMode.getSelectionModel().select(s.fitMode());
          highlightStrength.setValue(s.highlightStrength());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new ImageVerticalSettings(
        fitMode.getSelectionModel().getSelectedItem(), highlightStrength.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return ImageVerticalSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
