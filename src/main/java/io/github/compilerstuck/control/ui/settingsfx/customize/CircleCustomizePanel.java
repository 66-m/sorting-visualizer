package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.CircleSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link CircleSettings}. */
public final class CircleCustomizePanel implements VisualizationCustomizePanel {

  private final Slider radiusScale =
      new Slider(
          CircleSettings.RADIUS_SCALE_MIN,
          CircleSettings.RADIUS_SCALE_MAX,
          CircleSettings.DEFAULT_RADIUS_SCALE);
  private final Label radiusScaleValue = CustomizePanelSupport.valueLabel();
  private final Slider startAngleDeg =
      new Slider(
          CircleSettings.START_ANGLE_DEG_MIN,
          CircleSettings.START_ANGLE_DEG_MAX,
          CircleSettings.DEFAULT_START_ANGLE_DEG);
  private final Label startAngleDegValue = CustomizePanelSupport.valueLabel();
  private final Slider lineThickness =
      new Slider(
          CircleSettings.LINE_THICKNESS_MIN,
          CircleSettings.LINE_THICKNESS_MAX,
          CircleSettings.DEFAULT_LINE_THICKNESS);
  private final Label lineThicknessValue = CustomizePanelSupport.valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(radiusScale, false);
    CustomizePanelSupport.configureSlider(startAngleDeg, false);
    CustomizePanelSupport.configureSlider(lineThickness, false);
    CustomizePanelSupport.bindValueLabel(
        radiusScale, radiusScaleValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(
        startAngleDeg, startAngleDegValue, v -> String.format("%.0f°", v));
    CustomizePanelSupport.bindValueLabel(
        lineThickness, lineThicknessValue, v -> String.format("%.2f", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Radius", radiusScale, radiusScaleValue, CircleSettings.DEFAULT_RADIUS_SCALE),
            CustomizePanelSupport.sliderRow(
                "Start angle",
                startAngleDeg,
                startAngleDegValue,
                CircleSettings.DEFAULT_START_ANGLE_DEG),
            CustomizePanelSupport.sliderRow(
                "Line thickness",
                lineThickness,
                lineThicknessValue,
                CircleSettings.DEFAULT_LINE_THICKNESS));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    CircleSettings s = settings instanceof CircleSettings c ? c : CircleSettings.defaults();
    loading = true;
    try {
      radiusScale.setValue(s.radiusScale());
      startAngleDeg.setValue(s.startAngleDeg());
      lineThickness.setValue(s.lineThickness());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new CircleSettings(
        radiusScale.getValue(), startAngleDeg.getValue(), lineThickness.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return CircleSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    radiusScale.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    startAngleDeg.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    lineThickness.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
