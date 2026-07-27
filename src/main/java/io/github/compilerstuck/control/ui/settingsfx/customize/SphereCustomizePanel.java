package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.SphereSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link SphereSettings}. */
public final class SphereCustomizePanel implements VisualizationCustomizePanel {

  private final Slider rotationSpeedRadPerSec =
      new Slider(
          SphereSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
          SphereSettings.ROTATION_SPEED_RAD_PER_SEC_MAX,
          SphereSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC);
  private final Label rotationSpeedRadPerSecValue = CustomizePanelSupport.valueLabel();
  private final Slider globeScale =
      new Slider(
          SphereSettings.GLOBE_SCALE_MIN,
          SphereSettings.GLOBE_SCALE_MAX,
          SphereSettings.DEFAULT_GLOBE_SCALE);
  private final Label globeScaleValue = CustomizePanelSupport.valueLabel();
  private final Slider pointSize =
      new Slider(
          SphereSettings.POINT_SIZE_MIN,
          SphereSettings.POINT_SIZE_MAX,
          SphereSettings.DEFAULT_POINT_SIZE);
  private final Label pointSizeValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(rotationSpeedRadPerSec, false);
    CustomizePanelSupport.configureSlider(globeScale, false);
    CustomizePanelSupport.configureSlider(pointSize, false);
    CustomizePanelSupport.bindValueLabel(
        rotationSpeedRadPerSec, rotationSpeedRadPerSecValue, v -> String.format("%.2f rad/s", v));
    CustomizePanelSupport.bindValueLabel(
        globeScale, globeScaleValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(pointSize, pointSizeValue, v -> String.format("%.1f", v));
    draft.bind(
        rotationSpeedRadPerSec.valueProperty(),
        globeScale.valueProperty(),
        pointSize.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Scene scale", globeScale, globeScaleValue, SphereSettings.DEFAULT_GLOBE_SCALE),
            CustomizePanelSupport.sliderRow(
                "Rotation speed",
                rotationSpeedRadPerSec,
                rotationSpeedRadPerSecValue,
                SphereSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC),
            CustomizePanelSupport.sliderRow(
                "Point size", pointSize, pointSizeValue, SphereSettings.DEFAULT_POINT_SIZE));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    SphereSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, SphereSettings.class, SphereSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          rotationSpeedRadPerSec.setValue(s.rotationSpeedRadPerSec());
          globeScale.setValue(s.globeScale());
          pointSize.setValue(s.pointSize());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new SphereSettings(
        rotationSpeedRadPerSec.getValue(), globeScale.getValue(), pointSize.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return SphereSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
