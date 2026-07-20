package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.DisparityCircleScatterLinkedSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link DisparityCircleScatterLinkedSettings}. */
public final class DisparityCircleScatterLinkedCustomizePanel
    implements VisualizationCustomizePanel {

  private final Slider lineThickness =
      new Slider(
          DisparityCircleScatterLinkedSettings.LINE_THICKNESS_MIN,
          DisparityCircleScatterLinkedSettings.LINE_THICKNESS_MAX,
          DisparityCircleScatterLinkedSettings.DEFAULT_LINE_THICKNESS);
  private final Label lineThicknessValue = CustomizePanelSupport.valueLabel();
  private final Slider radiusScale =
      new Slider(
          DisparityCircleScatterLinkedSettings.RADIUS_SCALE_MIN,
          DisparityCircleScatterLinkedSettings.RADIUS_SCALE_MAX,
          DisparityCircleScatterLinkedSettings.DEFAULT_RADIUS_SCALE);
  private final Label radiusScaleValue = CustomizePanelSupport.valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(lineThickness, false);
    CustomizePanelSupport.configureSlider(radiusScale, false);
    CustomizePanelSupport.bindValueLabel(
        lineThickness, lineThicknessValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(
        radiusScale, radiusScaleValue, v -> String.format("%.2f", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Radius",
                radiusScale,
                radiusScaleValue,
                DisparityCircleScatterLinkedSettings.DEFAULT_RADIUS_SCALE),
            CustomizePanelSupport.sliderRow(
                "Line thickness",
                lineThickness,
                lineThicknessValue,
                DisparityCircleScatterLinkedSettings.DEFAULT_LINE_THICKNESS));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    DisparityCircleScatterLinkedSettings s =
        settings instanceof DisparityCircleScatterLinkedSettings c
            ? c
            : DisparityCircleScatterLinkedSettings.defaults();
    loading = true;
    try {
      lineThickness.setValue(s.lineThickness());
      radiusScale.setValue(s.radiusScale());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new DisparityCircleScatterLinkedSettings(
        lineThickness.getValue(), radiusScale.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return DisparityCircleScatterLinkedSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    lineThickness.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    radiusScale.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
