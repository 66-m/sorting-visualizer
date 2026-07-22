package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.MorphingShellSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link MorphingShellSettings}. */
public final class MorphingShellCustomizePanel implements VisualizationCustomizePanel {

  private final Slider rotationSpeedRadPerSec =
      new Slider(
          MorphingShellSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
          MorphingShellSettings.ROTATION_SPEED_RAD_PER_SEC_MAX,
          MorphingShellSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC);
  private final Label rotationSpeedRadPerSecValue = CustomizePanelSupport.valueLabel();
  private final Slider sphereSize =
      new Slider(
          MorphingShellSettings.SPHERE_SIZE_MIN,
          MorphingShellSettings.SPHERE_SIZE_MAX,
          MorphingShellSettings.DEFAULT_SPHERE_SIZE);
  private final Label sphereSizeValue = CustomizePanelSupport.valueLabel();
  private final Slider shellRadiusScale =
      new Slider(
          MorphingShellSettings.SHELL_RADIUS_SCALE_MIN,
          MorphingShellSettings.SHELL_RADIUS_SCALE_MAX,
          MorphingShellSettings.DEFAULT_SHELL_RADIUS_SCALE);
  private final Label shellRadiusScaleValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(rotationSpeedRadPerSec, false);
    CustomizePanelSupport.configureSlider(sphereSize, false);
    CustomizePanelSupport.configureSlider(shellRadiusScale, false);
    CustomizePanelSupport.bindValueLabel(
        rotationSpeedRadPerSec, rotationSpeedRadPerSecValue, v -> String.format("%.2f rad/s", v));
    CustomizePanelSupport.bindValueLabel(
        sphereSize, sphereSizeValue, v -> String.format("%.1f", v));
    CustomizePanelSupport.bindValueLabel(
        shellRadiusScale, shellRadiusScaleValue, v -> String.format("%.2f", v));
    draft.bind(
        rotationSpeedRadPerSec.valueProperty(),
        sphereSize.valueProperty(),
        shellRadiusScale.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Scene scale",
                shellRadiusScale,
                shellRadiusScaleValue,
                MorphingShellSettings.DEFAULT_SHELL_RADIUS_SCALE),
            CustomizePanelSupport.sliderRow(
                "Rotation speed",
                rotationSpeedRadPerSec,
                rotationSpeedRadPerSecValue,
                MorphingShellSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC),
            CustomizePanelSupport.sliderRow(
                "Sphere size",
                sphereSize,
                sphereSizeValue,
                MorphingShellSettings.DEFAULT_SPHERE_SIZE));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    MorphingShellSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, MorphingShellSettings.class, MorphingShellSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          rotationSpeedRadPerSec.setValue(s.rotationSpeedRadPerSec());
          sphereSize.setValue(s.sphereSize());
          shellRadiusScale.setValue(s.shellRadiusScale());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new MorphingShellSettings(
        rotationSpeedRadPerSec.getValue(), sphereSize.getValue(), shellRadiusScale.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return MorphingShellSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
