package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.SphereHoopsSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link SphereHoopsSettings}. */
public final class SphereHoopsCustomizePanel implements VisualizationCustomizePanel {

  private final Slider globeScale =
      new Slider(
          SphereHoopsSettings.GLOBE_SCALE_MIN,
          SphereHoopsSettings.GLOBE_SCALE_MAX,
          SphereHoopsSettings.DEFAULT_GLOBE_SCALE);
  private final Label globeScaleValue = CustomizePanelSupport.valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(globeScale, false);
    CustomizePanelSupport.bindValueLabel(
        globeScale, globeScaleValue, v -> String.format("%.2f", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Scene scale",
                globeScale,
                globeScaleValue,
                SphereHoopsSettings.DEFAULT_GLOBE_SCALE));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    SphereHoopsSettings s =
        settings instanceof SphereHoopsSettings c ? c : SphereHoopsSettings.defaults();
    loading = true;
    try {
      globeScale.setValue(s.globeScale());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new SphereHoopsSettings(globeScale.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return SphereHoopsSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    globeScale.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
