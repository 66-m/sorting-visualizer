package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.MorphingShellSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
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
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

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
    bindDraftChanges();

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

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    MorphingShellSettings s =
        settings instanceof MorphingShellSettings c ? c : MorphingShellSettings.defaults();
    loading = true;
    try {
      rotationSpeedRadPerSec.setValue(s.rotationSpeedRadPerSec());
      sphereSize.setValue(s.sphereSize());
      shellRadiusScale.setValue(s.shellRadiusScale());
    } finally {
      loading = false;
    }
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
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    rotationSpeedRadPerSec.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    sphereSize.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    shellRadiusScale.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
