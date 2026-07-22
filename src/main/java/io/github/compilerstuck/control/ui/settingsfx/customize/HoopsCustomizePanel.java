package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.HoopsSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
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
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(radiusScale, false);
    CustomizePanelSupport.bindValueLabel(
        radiusScale, radiusScaleValue, v -> String.format("%.3f", v));
    draft.bind(radiusScale.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Radius", radiusScale, radiusScaleValue, HoopsSettings.DEFAULT_RADIUS_SCALE));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    HoopsSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, HoopsSettings.class, HoopsSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          radiusScale.setValue(s.radiusScale());
        });
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
    draft.setListener(listener);
  }
}
