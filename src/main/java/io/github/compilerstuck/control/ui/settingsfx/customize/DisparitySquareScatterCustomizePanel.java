package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.DisparitySquareScatterSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
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
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(pointSize, false);
    CustomizePanelSupport.configureSlider(perimeterScale, false);
    CustomizePanelSupport.bindValueLabel(pointSize, pointSizeValue, v -> String.format("%.1f", v));
    CustomizePanelSupport.bindValueLabel(
        perimeterScale, perimeterScaleValue, v -> String.format("%.2f", v));
    draft.bind(pointSize.valueProperty(), perimeterScale.valueProperty());

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

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    DisparitySquareScatterSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings,
            DisparitySquareScatterSettings.class,
            DisparitySquareScatterSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          pointSize.setValue(s.pointSize());
          perimeterScale.setValue(s.perimeterScale());
        });
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
    draft.setListener(listener);
  }
}
