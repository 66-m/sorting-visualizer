package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.CubicLinesSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link CubicLinesSettings}. */
public final class CubicLinesCustomizePanel implements VisualizationCustomizePanel {

  private final Slider rotationSpeedRadPerSec =
      new Slider(
          CubicLinesSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
          CubicLinesSettings.ROTATION_SPEED_RAD_PER_SEC_MAX,
          CubicLinesSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC);
  private final Label rotationSpeedRadPerSecValue = CustomizePanelSupport.valueLabel();
  private final Slider sceneScaleDivisor =
      new Slider(
          CubicLinesSettings.SCENE_SCALE_DIVISOR_MIN,
          CubicLinesSettings.SCENE_SCALE_DIVISOR_MAX,
          CubicLinesSettings.DEFAULT_SCENE_SCALE_DIVISOR);
  private final Label sceneScaleDivisorValue = CustomizePanelSupport.valueLabel();
  private final Slider markerSize =
      new Slider(
          CubicLinesSettings.MARKER_SIZE_MIN,
          CubicLinesSettings.MARKER_SIZE_MAX,
          CubicLinesSettings.DEFAULT_MARKER_SIZE);
  private final Label markerSizeValue = CustomizePanelSupport.valueLabel();
  private final Slider lineOpacity =
      new Slider(
          CubicLinesSettings.LINE_OPACITY_MIN,
          CubicLinesSettings.LINE_OPACITY_MAX,
          CubicLinesSettings.DEFAULT_LINE_OPACITY);
  private final Label lineOpacityValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(rotationSpeedRadPerSec, false);
    CustomizePanelSupport.configureSlider(sceneScaleDivisor, false);
    CustomizePanelSupport.configureSlider(markerSize, false);
    CustomizePanelSupport.configureSlider(lineOpacity, true);
    CustomizePanelSupport.bindValueLabel(
        rotationSpeedRadPerSec, rotationSpeedRadPerSecValue, v -> String.format("%.2f rad/s", v));
    CustomizePanelSupport.bindValueLabel(
        sceneScaleDivisor, sceneScaleDivisorValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(
        markerSize, markerSizeValue, v -> String.format("%.1f", v));
    CustomizePanelSupport.bindValueLabel(
        lineOpacity, lineOpacityValue, v -> String.format("%d", Math.round(v)));
    draft.bind(
        rotationSpeedRadPerSec.valueProperty(),
        sceneScaleDivisor.valueProperty(),
        markerSize.valueProperty(),
        lineOpacity.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Scene scale",
                sceneScaleDivisor,
                sceneScaleDivisorValue,
                CubicLinesSettings.DEFAULT_SCENE_SCALE_DIVISOR),
            CustomizePanelSupport.sliderRow(
                "Rotation speed",
                rotationSpeedRadPerSec,
                rotationSpeedRadPerSecValue,
                CubicLinesSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC),
            CustomizePanelSupport.sliderRow(
                "Marker size", markerSize, markerSizeValue, CubicLinesSettings.DEFAULT_MARKER_SIZE),
            CustomizePanelSupport.sliderRow(
                "Line opacity",
                lineOpacity,
                lineOpacityValue,
                CubicLinesSettings.DEFAULT_LINE_OPACITY));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    CubicLinesSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, CubicLinesSettings.class, CubicLinesSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          rotationSpeedRadPerSec.setValue(s.rotationSpeedRadPerSec());
          sceneScaleDivisor.setValue(s.sceneScaleDivisor());
          markerSize.setValue(s.markerSize());
          lineOpacity.setValue(s.lineOpacity());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new CubicLinesSettings(
        rotationSpeedRadPerSec.getValue(),
        sceneScaleDivisor.getValue(),
        markerSize.getValue(),
        (int) Math.round(lineOpacity.getValue()));
  }

  @Override
  public VisualizationSettings defaults() {
    return CubicLinesSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
