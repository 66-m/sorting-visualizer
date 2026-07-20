package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.ImageHorizontalSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
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
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    fitMode.getItems().setAll(ImageHorizontalSettings.FitMode.values());
    fitMode.getSelectionModel().select(ImageHorizontalSettings.DEFAULT_FIT_MODE);
    CustomizePanelSupport.configureSlider(highlightStrength, false);
    CustomizePanelSupport.bindValueLabel(
        highlightStrength, highlightStrengthValue, v -> String.format("%.2f", v));
    bindDraftChanges();

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

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    ImageHorizontalSettings s =
        settings instanceof ImageHorizontalSettings c ? c : ImageHorizontalSettings.defaults();
    loading = true;
    try {
      fitMode.getSelectionModel().select(s.fitMode());
      highlightStrength.setValue(s.highlightStrength());
    } finally {
      loading = false;
    }
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
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    fitMode
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((obs, o, v) -> fireDraftChanged());
    highlightStrength.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
