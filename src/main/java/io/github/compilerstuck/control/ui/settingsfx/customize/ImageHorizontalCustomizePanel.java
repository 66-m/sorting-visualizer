package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.ImageHorizontalSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link ImageHorizontalSettings}. */
public final class ImageHorizontalCustomizePanel implements VisualizationCustomizePanel {

  private final ComboBox<ImageHorizontalSettings.FitMode> fitMode = new ComboBox<>();
  private final Slider highlightStrength =
      new Slider(
          ImageHorizontalSettings.HIGHLIGHT_STRENGTH_MIN,
          ImageHorizontalSettings.HIGHLIGHT_STRENGTH_MAX,
          ImageHorizontalSettings.DEFAULT_HIGHLIGHT_STRENGTH);
  private final Label highlightStrengthValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    fitMode.getItems().setAll(ImageHorizontalSettings.FitMode.values());
    fitMode.getSelectionModel().select(ImageHorizontalSettings.DEFAULT_FIT_MODE);
    CustomizePanelSupport.configureSlider(highlightStrength, false);
    CustomizePanelSupport.bindValueLabel(
        highlightStrength, highlightStrengthValue, v -> String.format("%.2f", v));
    draft.bind(
        fitMode.getSelectionModel().selectedItemProperty(), highlightStrength.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "IMAGE",
            CustomizePanelSupport.comboRow(
                "Fit mode", fitMode, ImageHorizontalSettings.DEFAULT_FIT_MODE),
            CustomizePanelSupport.sliderRow(
                "Highlight strength",
                highlightStrength,
                highlightStrengthValue,
                ImageHorizontalSettings.DEFAULT_HIGHLIGHT_STRENGTH));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    ImageHorizontalSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, ImageHorizontalSettings.class, ImageHorizontalSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          fitMode.getSelectionModel().select(s.fitMode());
          highlightStrength.setValue(s.highlightStrength());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new ImageHorizontalSettings(
        fitMode.getSelectionModel().getSelectedItem(), highlightStrength.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return ImageHorizontalSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
