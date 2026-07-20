package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.HoopsSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link HoopsSettings}. */
public final class HoopsCustomizePanel implements VisualizationCustomizePanel {

  private final Slider radiusScale =
      new Slider(
          HoopsSettings.RADIUS_SCALE_MIN,
          HoopsSettings.RADIUS_SCALE_MAX,
          HoopsSettings.DEFAULT_RADIUS_SCALE);
  private final Label radiusScaleValue = CustomizePanelSupport.valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(radiusScale, false);
    CustomizePanelSupport.bindValueLabel(
        radiusScale, radiusScaleValue, v -> String.format("%.3f", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Radius", radiusScale, radiusScaleValue, HoopsSettings.DEFAULT_RADIUS_SCALE));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    HoopsSettings s = settings instanceof HoopsSettings c ? c : HoopsSettings.defaults();
    loading = true;
    try {
      radiusScale.setValue(s.radiusScale());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new HoopsSettings(radiusScale.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return HoopsSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    radiusScale.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
