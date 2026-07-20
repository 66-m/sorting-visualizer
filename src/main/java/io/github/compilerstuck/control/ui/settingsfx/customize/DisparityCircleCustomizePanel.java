package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.DisparityCircleSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link DisparityCircleSettings}. */
public final class DisparityCircleCustomizePanel implements VisualizationCustomizePanel {

  private final Slider radiusScale =
      new Slider(
          DisparityCircleSettings.RADIUS_SCALE_MIN,
          DisparityCircleSettings.RADIUS_SCALE_MAX,
          DisparityCircleSettings.DEFAULT_RADIUS_SCALE);
  private final Label radiusScaleValue = CustomizePanelSupport.valueLabel();
  private final Slider lineThickness =
      new Slider(
          DisparityCircleSettings.LINE_THICKNESS_MIN,
          DisparityCircleSettings.LINE_THICKNESS_MAX,
          DisparityCircleSettings.DEFAULT_LINE_THICKNESS);
  private final Label lineThicknessValue = CustomizePanelSupport.valueLabel();
  private final Slider startAngleDeg =
      new Slider(
          DisparityCircleSettings.START_ANGLE_DEG_MIN,
          DisparityCircleSettings.START_ANGLE_DEG_MAX,
          DisparityCircleSettings.DEFAULT_START_ANGLE_DEG);
  private final Label startAngleDegValue = CustomizePanelSupport.valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(radiusScale, false);
    CustomizePanelSupport.configureSlider(lineThickness, false);
    CustomizePanelSupport.configureSlider(startAngleDeg, false);
    CustomizePanelSupport.bindValueLabel(
        radiusScale, radiusScaleValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(
        lineThickness, lineThicknessValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(
        startAngleDeg, startAngleDegValue, v -> String.format("%.0f°", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Radius",
                radiusScale,
                radiusScaleValue,
                DisparityCircleSettings.DEFAULT_RADIUS_SCALE),
            CustomizePanelSupport.sliderRow(
                "Start angle",
                startAngleDeg,
                startAngleDegValue,
                DisparityCircleSettings.DEFAULT_START_ANGLE_DEG),
            CustomizePanelSupport.sliderRow(
                "Line thickness",
                lineThickness,
                lineThicknessValue,
                DisparityCircleSettings.DEFAULT_LINE_THICKNESS));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    DisparityCircleSettings s =
        settings instanceof DisparityCircleSettings c ? c : DisparityCircleSettings.defaults();
    loading = true;
    try {
      radiusScale.setValue(s.radiusScale());
      lineThickness.setValue(s.lineThickness());
      startAngleDeg.setValue(s.startAngleDeg());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new DisparityCircleSettings(
        radiusScale.getValue(), lineThickness.getValue(), startAngleDeg.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return DisparityCircleSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    radiusScale.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    lineThickness.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    startAngleDeg.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
