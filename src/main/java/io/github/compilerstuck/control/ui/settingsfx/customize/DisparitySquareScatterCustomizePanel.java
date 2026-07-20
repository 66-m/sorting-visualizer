package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.DisparitySquareScatterSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link DisparitySquareScatterSettings}. */
public final class DisparitySquareScatterCustomizePanel implements VisualizationCustomizePanel {

  private final Slider pointSize =
      new Slider(
          DisparitySquareScatterSettings.POINT_SIZE_MIN,
          DisparitySquareScatterSettings.POINT_SIZE_MAX,
          DisparitySquareScatterSettings.DEFAULT_POINT_SIZE);
  private final Label pointSizeValue = CustomizePanelSupport.valueLabel();
  private final Slider perimeterScale =
      new Slider(
          DisparitySquareScatterSettings.PERIMETER_SCALE_MIN,
          DisparitySquareScatterSettings.PERIMETER_SCALE_MAX,
          DisparitySquareScatterSettings.DEFAULT_PERIMETER_SCALE);
  private final Label perimeterScaleValue = CustomizePanelSupport.valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(pointSize, false);
    CustomizePanelSupport.configureSlider(perimeterScale, false);
    CustomizePanelSupport.bindValueLabel(pointSize, pointSizeValue, v -> String.format("%.1f", v));
    CustomizePanelSupport.bindValueLabel(
        perimeterScale, perimeterScaleValue, v -> String.format("%.2f", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Scene scale",
                perimeterScale,
                perimeterScaleValue,
                DisparitySquareScatterSettings.DEFAULT_PERIMETER_SCALE),
            CustomizePanelSupport.sliderRow(
                "Point size",
                pointSize,
                pointSizeValue,
                DisparitySquareScatterSettings.DEFAULT_POINT_SIZE));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    DisparitySquareScatterSettings s =
        settings instanceof DisparitySquareScatterSettings c
            ? c
            : DisparitySquareScatterSettings.defaults();
    loading = true;
    try {
      pointSize.setValue(s.pointSize());
      perimeterScale.setValue(s.perimeterScale());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new DisparitySquareScatterSettings(pointSize.getValue(), perimeterScale.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return DisparitySquareScatterSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    pointSize.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    perimeterScale.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
