package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.CubeSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsStrings;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/**
 * Draft editor for {@link CubeSettings}.
 *
 * <p>Layout: sectioned form on a 4-column grid (label | control | value | reset) so every row
 * shares the same alignment and density.
 */
public final class CubeCustomizePanel implements VisualizationCustomizePanel {

  private final Slider rotationSpeed =
      new Slider(
          CubeSettings.ROTATION_SPEED_MIN,
          CubeSettings.ROTATION_SPEED_MAX,
          CubeSettings.DEFAULT_ROTATION_SPEED);
  private final Slider fillOpacity =
      new Slider(
          CubeSettings.FILL_OPACITY_MIN,
          CubeSettings.FILL_OPACITY_MAX,
          CubeSettings.DEFAULT_FILL_OPACITY);
  private final Slider sceneScale =
      new Slider(
          CubeSettings.SCENE_SCALE_DIVISOR_MIN,
          CubeSettings.SCENE_SCALE_DIVISOR_MAX,
          CubeSettings.DEFAULT_SCENE_SCALE_DIVISOR);
  private final CheckBox wireframe = new CheckBox();
  private final Label rotationValue = CustomizePanelSupport.valueLabel();
  private final Label fillOpacityValue = CustomizePanelSupport.valueLabel();
  private final Label sceneScaleValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(rotationSpeed, false);
    CustomizePanelSupport.configureSlider(fillOpacity, true);
    CustomizePanelSupport.configureSlider(sceneScale, false);

    CustomizePanelSupport.bindValueLabel(
        rotationSpeed, rotationValue, v -> String.format("%.2f rad/s", v));
    CustomizePanelSupport.bindValueLabel(
        fillOpacity, fillOpacityValue, v -> String.format("%d", Math.round(v)));
    CustomizePanelSupport.bindValueLabel(
        sceneScale, sceneScaleValue, v -> String.format("%.2f", v));
    draft.bind(
        rotationSpeed.valueProperty(),
        fillOpacity.valueProperty(),
        sceneScale.valueProperty(),
        wireframe.selectedProperty());

    VBox motion =
        CustomizePanelSupport.section(
            SettingsStrings.CUBE_SECTION_MOTION,
            CustomizePanelSupport.sliderRow(
                SettingsStrings.CUBE_ROTATION_SPEED,
                rotationSpeed,
                rotationValue,
                CubeSettings.DEFAULT_ROTATION_SPEED));

    VBox appearance =
        CustomizePanelSupport.section(
            SettingsStrings.CUBE_SECTION_APPEARANCE,
            CustomizePanelSupport.sliderRow(
                SettingsStrings.CUBE_FILL_OPACITY,
                fillOpacity,
                fillOpacityValue,
                CubeSettings.DEFAULT_FILL_OPACITY),
            CustomizePanelSupport.checkboxRow(
                SettingsStrings.CUBE_WIREFRAME, wireframe, CubeSettings.DEFAULT_WIREFRAME));

    VBox frame =
        CustomizePanelSupport.section(
            SettingsStrings.CUBE_SECTION_FRAME,
            CustomizePanelSupport.sliderRow(
                SettingsStrings.CUBE_SCENE_SCALE,
                sceneScale,
                sceneScaleValue,
                CubeSettings.DEFAULT_SCENE_SCALE_DIVISOR));

    return CustomizePanelSupport.panelRoot(frame, motion, appearance);
  }

  @Override
  public void load(VisualizationSettings settings) {
    CubeSettings cube =
        CustomizePanelSupport.castOrDefaults(settings, CubeSettings.class, CubeSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          rotationSpeed.setValue(cube.rotationSpeedRadPerSec());
          fillOpacity.setValue(cube.fillOpacity());
          sceneScale.setValue(cube.sceneScaleDivisor());
          wireframe.setSelected(cube.wireframeEnabled());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new CubeSettings(
        rotationSpeed.getValue(),
        (int) Math.round(fillOpacity.getValue()),
        wireframe.isSelected(),
        sceneScale.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return CubeSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
