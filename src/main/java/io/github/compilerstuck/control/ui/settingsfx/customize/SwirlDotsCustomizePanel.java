package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.SwirlDotsSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link SwirlDotsSettings}. */
public final class SwirlDotsCustomizePanel implements VisualizationCustomizePanel {

  private final Slider spiralTurns =
      new Slider(
          SwirlDotsSettings.SPIRAL_TURNS_MIN,
          SwirlDotsSettings.SPIRAL_TURNS_MAX,
          SwirlDotsSettings.DEFAULT_SPIRAL_TURNS);
  private final Label spiralTurnsValue = CustomizePanelSupport.valueLabel();
  private final Slider radiusScale =
      new Slider(
          SwirlDotsSettings.RADIUS_SCALE_MIN,
          SwirlDotsSettings.RADIUS_SCALE_MAX,
          SwirlDotsSettings.DEFAULT_RADIUS_SCALE);
  private final Label radiusScaleValue = CustomizePanelSupport.valueLabel();
  private final Slider pointSize =
      new Slider(
          SwirlDotsSettings.POINT_SIZE_MIN,
          SwirlDotsSettings.POINT_SIZE_MAX,
          SwirlDotsSettings.DEFAULT_POINT_SIZE);
  private final Label pointSizeValue = CustomizePanelSupport.valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(spiralTurns, false);
    CustomizePanelSupport.configureSlider(radiusScale, false);
    CustomizePanelSupport.configureSlider(pointSize, false);
    CustomizePanelSupport.bindValueLabel(
        spiralTurns, spiralTurnsValue, v -> String.format("%.1f", v));
    CustomizePanelSupport.bindValueLabel(
        radiusScale, radiusScaleValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(pointSize, pointSizeValue, v -> String.format("%.1f", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Radius", radiusScale, radiusScaleValue, SwirlDotsSettings.DEFAULT_RADIUS_SCALE),
            CustomizePanelSupport.sliderRow(
                "Spiral turns",
                spiralTurns,
                spiralTurnsValue,
                SwirlDotsSettings.DEFAULT_SPIRAL_TURNS),
            CustomizePanelSupport.sliderRow(
                "Point size", pointSize, pointSizeValue, SwirlDotsSettings.DEFAULT_POINT_SIZE));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    SwirlDotsSettings s =
        settings instanceof SwirlDotsSettings c ? c : SwirlDotsSettings.defaults();
    loading = true;
    try {
      spiralTurns.setValue(s.spiralTurns());
      radiusScale.setValue(s.radiusScale());
      pointSize.setValue(s.pointSize());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new SwirlDotsSettings(
        spiralTurns.getValue(), radiusScale.getValue(), pointSize.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return SwirlDotsSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    spiralTurns.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    radiusScale.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    pointSize.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
