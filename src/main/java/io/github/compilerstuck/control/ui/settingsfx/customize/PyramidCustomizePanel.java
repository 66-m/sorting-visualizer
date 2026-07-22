package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.PyramidSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link PyramidSettings}. */
public final class PyramidCustomizePanel implements VisualizationCustomizePanel {

  private final Slider rotationSpeedRadPerSec =
      new Slider(
          PyramidSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
          PyramidSettings.ROTATION_SPEED_RAD_PER_SEC_MAX,
          PyramidSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC);
  private final Label rotationSpeedRadPerSecValue = CustomizePanelSupport.valueLabel();
  private final Slider stackScale =
      new Slider(
          PyramidSettings.STACK_SCALE_MIN,
          PyramidSettings.STACK_SCALE_MAX,
          PyramidSettings.DEFAULT_STACK_SCALE);
  private final Label stackScaleValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(rotationSpeedRadPerSec, false);
    CustomizePanelSupport.configureSlider(stackScale, false);
    CustomizePanelSupport.bindValueLabel(
        rotationSpeedRadPerSec, rotationSpeedRadPerSecValue, v -> String.format("%.2f rad/s", v));
    CustomizePanelSupport.bindValueLabel(
        stackScale, stackScaleValue, v -> String.format("%.2f", v));
    draft.bind(rotationSpeedRadPerSec.valueProperty(), stackScale.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Scene scale", stackScale, stackScaleValue, PyramidSettings.DEFAULT_STACK_SCALE),
            CustomizePanelSupport.sliderRow(
                "Rotation speed",
                rotationSpeedRadPerSec,
                rotationSpeedRadPerSecValue,
                PyramidSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    PyramidSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, PyramidSettings.class, PyramidSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          rotationSpeedRadPerSec.setValue(s.rotationSpeedRadPerSec());
          stackScale.setValue(s.stackScale());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new PyramidSettings(rotationSpeedRadPerSec.getValue(), stackScale.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return PyramidSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
