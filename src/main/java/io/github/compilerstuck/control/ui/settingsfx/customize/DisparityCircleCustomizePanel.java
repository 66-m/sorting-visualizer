package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.DisparityCircleSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
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
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

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
    draft.bind(
        radiusScale.valueProperty(), lineThickness.valueProperty(), startAngleDeg.valueProperty());

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

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    DisparityCircleSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, DisparityCircleSettings.class, DisparityCircleSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          radiusScale.setValue(s.radiusScale());
          lineThickness.setValue(s.lineThickness());
          startAngleDeg.setValue(s.startAngleDeg());
        });
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
    draft.setListener(listener);
  }
}
