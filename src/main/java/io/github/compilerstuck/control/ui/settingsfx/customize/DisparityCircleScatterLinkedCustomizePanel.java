package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.DisparityCircleScatterLinkedSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
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
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(lineThickness, false);
    CustomizePanelSupport.configureSlider(radiusScale, false);
    CustomizePanelSupport.bindValueLabel(
        lineThickness, lineThicknessValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(
        radiusScale, radiusScaleValue, v -> String.format("%.2f", v));
    draft.bind(lineThickness.valueProperty(), radiusScale.valueProperty());

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

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    DisparityCircleScatterLinkedSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings,
            DisparityCircleScatterLinkedSettings.class,
            DisparityCircleScatterLinkedSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          lineThickness.setValue(s.lineThickness());
          radiusScale.setValue(s.radiusScale());
        });
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
    draft.setListener(listener);
  }
}
