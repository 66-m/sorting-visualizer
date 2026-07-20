package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.ImageVerticalSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
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
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    fitMode.getItems().setAll(ImageVerticalSettings.FitMode.values());
    fitMode.getSelectionModel().select(ImageVerticalSettings.DEFAULT_FIT_MODE);
    CustomizePanelSupport.configureSlider(highlightStrength, false);
    CustomizePanelSupport.bindValueLabel(
        highlightStrength, highlightStrengthValue, v -> String.format("%.2f", v));
    bindDraftChanges();

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

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    ImageVerticalSettings s =
        settings instanceof ImageVerticalSettings c ? c : ImageVerticalSettings.defaults();
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
    return new ImageVerticalSettings(
        fitMode.getSelectionModel().getSelectedItem(), highlightStrength.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return ImageVerticalSettings.defaults();
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
