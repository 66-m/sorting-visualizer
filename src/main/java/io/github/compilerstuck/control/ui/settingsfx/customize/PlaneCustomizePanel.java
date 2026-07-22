package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.PlaneSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link PlaneSettings}. */
public final class PlaneCustomizePanel implements VisualizationCustomizePanel {

  private final Slider rotationSpeedRadPerSec =
      new Slider(
          PlaneSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
          PlaneSettings.ROTATION_SPEED_RAD_PER_SEC_MAX,
          PlaneSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC);
  private final Label rotationSpeedRadPerSecValue = CustomizePanelSupport.valueLabel();
  private final Slider planeScale =
      new Slider(
          PlaneSettings.PLANE_SCALE_MIN,
          PlaneSettings.PLANE_SCALE_MAX,
          PlaneSettings.DEFAULT_PLANE_SCALE);
  private final Label planeScaleValue = CustomizePanelSupport.valueLabel();
  private final Slider tileGap =
      new Slider(
          PlaneSettings.TILE_GAP_MIN, PlaneSettings.TILE_GAP_MAX, PlaneSettings.DEFAULT_TILE_GAP);
  private final Label tileGapValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(rotationSpeedRadPerSec, false);
    CustomizePanelSupport.configureSlider(planeScale, false);
    CustomizePanelSupport.configureSlider(tileGap, false);
    CustomizePanelSupport.bindValueLabel(
        rotationSpeedRadPerSec, rotationSpeedRadPerSecValue, v -> String.format("%.2f rad/s", v));
    CustomizePanelSupport.bindValueLabel(
        planeScale, planeScaleValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(tileGap, tileGapValue, v -> String.format("%.2f", v));
    draft.bind(
        rotationSpeedRadPerSec.valueProperty(),
        planeScale.valueProperty(),
        tileGap.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Scene scale", planeScale, planeScaleValue, PlaneSettings.DEFAULT_PLANE_SCALE),
            CustomizePanelSupport.sliderRow(
                "Rotation speed",
                rotationSpeedRadPerSec,
                rotationSpeedRadPerSecValue,
                PlaneSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC),
            CustomizePanelSupport.sliderRow(
                "Tile gap", tileGap, tileGapValue, PlaneSettings.DEFAULT_TILE_GAP));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    PlaneSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, PlaneSettings.class, PlaneSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          rotationSpeedRadPerSec.setValue(s.rotationSpeedRadPerSec());
          planeScale.setValue(s.planeScale());
          tileGap.setValue(s.tileGap());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new PlaneSettings(
        rotationSpeedRadPerSec.getValue(), planeScale.getValue(), tileGap.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return PlaneSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
