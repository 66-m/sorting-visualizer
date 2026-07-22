package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.DisparityCircleScatterSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link DisparityCircleScatterSettings}. */
public final class DisparityCircleScatterCustomizePanel implements VisualizationCustomizePanel {

  private final Slider pointSize =
      new Slider(
          DisparityCircleScatterSettings.POINT_SIZE_MIN,
          DisparityCircleScatterSettings.POINT_SIZE_MAX,
          DisparityCircleScatterSettings.DEFAULT_POINT_SIZE);
  private final Label pointSizeValue = CustomizePanelSupport.valueLabel();
  private final Slider radiusScale =
      new Slider(
          DisparityCircleScatterSettings.RADIUS_SCALE_MIN,
          DisparityCircleScatterSettings.RADIUS_SCALE_MAX,
          DisparityCircleScatterSettings.DEFAULT_RADIUS_SCALE);
  private final Label radiusScaleValue = CustomizePanelSupport.valueLabel();
  private final Slider startAngleDeg =
      new Slider(
          DisparityCircleScatterSettings.START_ANGLE_DEG_MIN,
          DisparityCircleScatterSettings.START_ANGLE_DEG_MAX,
          DisparityCircleScatterSettings.DEFAULT_START_ANGLE_DEG);
  private final Label startAngleDegValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(pointSize, false);
    CustomizePanelSupport.configureSlider(radiusScale, false);
    CustomizePanelSupport.configureSlider(startAngleDeg, false);
    CustomizePanelSupport.bindValueLabel(pointSize, pointSizeValue, v -> String.format("%.1f", v));
    CustomizePanelSupport.bindValueLabel(
        radiusScale, radiusScaleValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(
        startAngleDeg, startAngleDegValue, v -> String.format("%.0f°", v));
    draft.bind(
        pointSize.valueProperty(), radiusScale.valueProperty(), startAngleDeg.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Radius",
                radiusScale,
                radiusScaleValue,
                DisparityCircleScatterSettings.DEFAULT_RADIUS_SCALE),
            CustomizePanelSupport.sliderRow(
                "Point size",
                pointSize,
                pointSizeValue,
                DisparityCircleScatterSettings.DEFAULT_POINT_SIZE),
            CustomizePanelSupport.sliderRow(
                "Start angle",
                startAngleDeg,
                startAngleDegValue,
                DisparityCircleScatterSettings.DEFAULT_START_ANGLE_DEG));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    DisparityCircleScatterSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings,
            DisparityCircleScatterSettings.class,
            DisparityCircleScatterSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          pointSize.setValue(s.pointSize());
          radiusScale.setValue(s.radiusScale());
          startAngleDeg.setValue(s.startAngleDeg());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new DisparityCircleScatterSettings(
        pointSize.getValue(), radiusScale.getValue(), startAngleDeg.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return DisparityCircleScatterSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
