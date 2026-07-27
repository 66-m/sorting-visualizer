package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.PhyllotaxisSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link PhyllotaxisSettings}. */
public final class PhyllotaxisCustomizePanel implements VisualizationCustomizePanel {

  private final Slider angleStepDeg =
      new Slider(
          PhyllotaxisSettings.ANGLE_STEP_DEG_MIN,
          PhyllotaxisSettings.ANGLE_STEP_DEG_MAX,
          PhyllotaxisSettings.DEFAULT_ANGLE_STEP_DEG);
  private final Label angleStepDegValue = CustomizePanelSupport.valueLabel();
  private final Slider scaleDivisor =
      new Slider(
          PhyllotaxisSettings.SCALE_DIVISOR_MIN,
          PhyllotaxisSettings.SCALE_DIVISOR_MAX,
          PhyllotaxisSettings.DEFAULT_SCALE_DIVISOR);
  private final Label scaleDivisorValue = CustomizePanelSupport.valueLabel();
  private final Slider pointSize =
      new Slider(
          PhyllotaxisSettings.POINT_SIZE_MIN,
          PhyllotaxisSettings.POINT_SIZE_MAX,
          PhyllotaxisSettings.DEFAULT_POINT_SIZE);
  private final Label pointSizeValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(angleStepDeg, false);
    CustomizePanelSupport.configureSlider(scaleDivisor, false);
    CustomizePanelSupport.configureSlider(pointSize, false);
    CustomizePanelSupport.bindValueLabel(
        angleStepDeg, angleStepDegValue, v -> String.format("%.1f°", v));
    CustomizePanelSupport.bindValueLabel(
        scaleDivisor, scaleDivisorValue, v -> String.format("%.0f", v));
    CustomizePanelSupport.bindValueLabel(pointSize, pointSizeValue, v -> String.format("%.1f", v));
    draft.bind(
        angleStepDeg.valueProperty(), scaleDivisor.valueProperty(), pointSize.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Scene scale",
                scaleDivisor,
                scaleDivisorValue,
                PhyllotaxisSettings.DEFAULT_SCALE_DIVISOR),
            CustomizePanelSupport.sliderRow(
                "Angle step",
                angleStepDeg,
                angleStepDegValue,
                PhyllotaxisSettings.DEFAULT_ANGLE_STEP_DEG),
            CustomizePanelSupport.sliderRow(
                "Point size", pointSize, pointSizeValue, PhyllotaxisSettings.DEFAULT_POINT_SIZE));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    PhyllotaxisSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, PhyllotaxisSettings.class, PhyllotaxisSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          angleStepDeg.setValue(s.angleStepDeg());
          scaleDivisor.setValue(s.scaleDivisor());
          pointSize.setValue(s.pointSize());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new PhyllotaxisSettings(
        angleStepDeg.getValue(), scaleDivisor.getValue(), pointSize.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return PhyllotaxisSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
