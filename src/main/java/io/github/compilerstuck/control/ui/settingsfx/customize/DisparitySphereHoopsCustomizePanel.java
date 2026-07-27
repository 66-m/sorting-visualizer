package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.DisparitySphereHoopsSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link DisparitySphereHoopsSettings}. */
public final class DisparitySphereHoopsCustomizePanel implements VisualizationCustomizePanel {

  private final Slider globeScale =
      new Slider(
          DisparitySphereHoopsSettings.GLOBE_SCALE_MIN,
          DisparitySphereHoopsSettings.GLOBE_SCALE_MAX,
          DisparitySphereHoopsSettings.DEFAULT_GLOBE_SCALE);
  private final Label globeScaleValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(globeScale, false);
    CustomizePanelSupport.bindValueLabel(
        globeScale, globeScaleValue, v -> String.format("%.2f", v));
    draft.bind(globeScale.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Scene scale",
                globeScale,
                globeScaleValue,
                DisparitySphereHoopsSettings.DEFAULT_GLOBE_SCALE));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    DisparitySphereHoopsSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, DisparitySphereHoopsSettings.class, DisparitySphereHoopsSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          globeScale.setValue(s.globeScale());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new DisparitySphereHoopsSettings(globeScale.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return DisparitySphereHoopsSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
