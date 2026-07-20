package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.SphericDisparityLinesSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link SphericDisparityLinesSettings}. */
public final class SphericDisparityLinesCustomizePanel implements VisualizationCustomizePanel {

  private final Slider rotationSpeedRadPerSec =
      new Slider(
          SphericDisparityLinesSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
          SphericDisparityLinesSettings.ROTATION_SPEED_RAD_PER_SEC_MAX,
          SphericDisparityLinesSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC);
  private final Label rotationSpeedRadPerSecValue = CustomizePanelSupport.valueLabel();
  private final Slider globeScale =
      new Slider(
          SphericDisparityLinesSettings.GLOBE_SCALE_MIN,
          SphericDisparityLinesSettings.GLOBE_SCALE_MAX,
          SphericDisparityLinesSettings.DEFAULT_GLOBE_SCALE);
  private final Label globeScaleValue = CustomizePanelSupport.valueLabel();
  private final Slider lineOpacity =
      new Slider(
          SphericDisparityLinesSettings.LINE_OPACITY_MIN,
          SphericDisparityLinesSettings.LINE_OPACITY_MAX,
          SphericDisparityLinesSettings.DEFAULT_LINE_OPACITY);
  private final Label lineOpacityValue = CustomizePanelSupport.valueLabel();
  private final Slider markerSize =
      new Slider(
          SphericDisparityLinesSettings.MARKER_SIZE_MIN,
          SphericDisparityLinesSettings.MARKER_SIZE_MAX,
          SphericDisparityLinesSettings.DEFAULT_MARKER_SIZE);
  private final Label markerSizeValue = CustomizePanelSupport.valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(rotationSpeedRadPerSec, false);
    CustomizePanelSupport.configureSlider(globeScale, false);
    CustomizePanelSupport.configureSlider(lineOpacity, true);
    CustomizePanelSupport.configureSlider(markerSize, false);
    CustomizePanelSupport.bindValueLabel(
        rotationSpeedRadPerSec, rotationSpeedRadPerSecValue, v -> String.format("%.2f rad/s", v));
    CustomizePanelSupport.bindValueLabel(
        globeScale, globeScaleValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(
        lineOpacity, lineOpacityValue, v -> String.format("%d", Math.round(v)));
    CustomizePanelSupport.bindValueLabel(
        markerSize, markerSizeValue, v -> String.format("%.1f", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Scene scale",
                globeScale,
                globeScaleValue,
                SphericDisparityLinesSettings.DEFAULT_GLOBE_SCALE),
            CustomizePanelSupport.sliderRow(
                "Rotation speed",
                rotationSpeedRadPerSec,
                rotationSpeedRadPerSecValue,
                SphericDisparityLinesSettings.DEFAULT_ROTATION_SPEED_RAD_PER_SEC),
            CustomizePanelSupport.sliderRow(
                "Marker size",
                markerSize,
                markerSizeValue,
                SphericDisparityLinesSettings.DEFAULT_MARKER_SIZE),
            CustomizePanelSupport.sliderRow(
                "Line opacity",
                lineOpacity,
                lineOpacityValue,
                SphericDisparityLinesSettings.DEFAULT_LINE_OPACITY));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    SphericDisparityLinesSettings s =
        settings instanceof SphericDisparityLinesSettings c
            ? c
            : SphericDisparityLinesSettings.defaults();
    loading = true;
    try {
      rotationSpeedRadPerSec.setValue(s.rotationSpeedRadPerSec());
      globeScale.setValue(s.globeScale());
      lineOpacity.setValue(s.lineOpacity());
      markerSize.setValue(s.markerSize());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new SphericDisparityLinesSettings(
        rotationSpeedRadPerSec.getValue(),
        globeScale.getValue(),
        (int) Math.round(lineOpacity.getValue()),
        markerSize.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return SphericDisparityLinesSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    rotationSpeedRadPerSec.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    globeScale.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    lineOpacity.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    markerSize.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
