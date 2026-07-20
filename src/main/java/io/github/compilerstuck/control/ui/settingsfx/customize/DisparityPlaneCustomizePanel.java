package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.DisparityPlaneSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link DisparityPlaneSettings}. */
public final class DisparityPlaneCustomizePanel implements VisualizationCustomizePanel {

  private final Slider rotationSpeedRadPerSec =
      new Slider(
          DisparityPlaneSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
          DisparityPlaneSettings.ROTATION_SPEED_RAD_PER_SEC_MAX,
          DisparityPlaneSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC);
  private final Label rotationSpeedRadPerSecValue = CustomizePanelSupport.valueLabel();
  private final Slider maxExtrusionFraction =
      new Slider(
          DisparityPlaneSettings.MAX_EXTRUSION_FRACTION_MIN,
          DisparityPlaneSettings.MAX_EXTRUSION_FRACTION_MAX,
          DisparityPlaneSettings.DEFAULT_MAX_EXTRUSION_FRACTION);
  private final Label maxExtrusionFractionValue = CustomizePanelSupport.valueLabel();
  private final Slider planeScale =
      new Slider(
          DisparityPlaneSettings.PLANE_SCALE_MIN,
          DisparityPlaneSettings.PLANE_SCALE_MAX,
          DisparityPlaneSettings.DEFAULT_PLANE_SCALE);
  private final Label planeScaleValue = CustomizePanelSupport.valueLabel();
  private final Slider tileGap =
      new Slider(
          DisparityPlaneSettings.TILE_GAP_MIN,
          DisparityPlaneSettings.TILE_GAP_MAX,
          DisparityPlaneSettings.DEFAULT_TILE_GAP);
  private final Label tileGapValue = CustomizePanelSupport.valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(rotationSpeedRadPerSec, false);
    CustomizePanelSupport.configureSlider(maxExtrusionFraction, false);
    CustomizePanelSupport.configureSlider(planeScale, false);
    CustomizePanelSupport.configureSlider(tileGap, false);
    CustomizePanelSupport.bindValueLabel(
        rotationSpeedRadPerSec, rotationSpeedRadPerSecValue, v -> String.format("%.2f rad/s", v));
    CustomizePanelSupport.bindValueLabel(
        maxExtrusionFraction, maxExtrusionFractionValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(
        planeScale, planeScaleValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(tileGap, tileGapValue, v -> String.format("%.2f", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Scene scale",
                planeScale,
                planeScaleValue,
                DisparityPlaneSettings.DEFAULT_PLANE_SCALE),
            CustomizePanelSupport.sliderRow(
                "Rotation speed",
                rotationSpeedRadPerSec,
                rotationSpeedRadPerSecValue,
                DisparityPlaneSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC),
            CustomizePanelSupport.sliderRow(
                "Max extrusion",
                maxExtrusionFraction,
                maxExtrusionFractionValue,
                DisparityPlaneSettings.DEFAULT_MAX_EXTRUSION_FRACTION),
            CustomizePanelSupport.sliderRow(
                "Tile gap", tileGap, tileGapValue, DisparityPlaneSettings.DEFAULT_TILE_GAP));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    DisparityPlaneSettings s =
        settings instanceof DisparityPlaneSettings c ? c : DisparityPlaneSettings.defaults();
    loading = true;
    try {
      rotationSpeedRadPerSec.setValue(s.rotationSpeedRadPerSec());
      maxExtrusionFraction.setValue(s.maxExtrusionFraction());
      planeScale.setValue(s.planeScale());
      tileGap.setValue(s.tileGap());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new DisparityPlaneSettings(
        rotationSpeedRadPerSec.getValue(),
        maxExtrusionFraction.getValue(),
        planeScale.getValue(),
        tileGap.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return DisparityPlaneSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    rotationSpeedRadPerSec.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    maxExtrusionFraction.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    planeScale.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    tileGap.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
