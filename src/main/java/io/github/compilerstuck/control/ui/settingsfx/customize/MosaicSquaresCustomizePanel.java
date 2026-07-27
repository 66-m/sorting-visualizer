package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.MosaicSquaresSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link MosaicSquaresSettings}. */
public final class MosaicSquaresCustomizePanel implements VisualizationCustomizePanel {

  private final Slider tileGapPx =
      new Slider(
          MosaicSquaresSettings.TILE_GAP_PX_MIN,
          MosaicSquaresSettings.TILE_GAP_PX_MAX,
          MosaicSquaresSettings.DEFAULT_TILE_GAP_PX);
  private final Label tileGapPxValue = CustomizePanelSupport.valueLabel();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(tileGapPx, false);
    CustomizePanelSupport.bindValueLabel(tileGapPx, tileGapPxValue, v -> String.format("%.1f", v));
    draft.bind(tileGapPx.valueProperty());

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Tile gap", tileGapPx, tileGapPxValue, MosaicSquaresSettings.DEFAULT_TILE_GAP_PX));

    return CustomizePanelSupport.panelRoot(section);
  }

  @Override
  public void load(VisualizationSettings settings) {
    MosaicSquaresSettings s =
        CustomizePanelSupport.castOrDefaults(
            settings, MosaicSquaresSettings.class, MosaicSquaresSettings::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          tileGapPx.setValue(s.tileGapPx());
        });
  }

  @Override
  public VisualizationSettings toSettings() {
    return new MosaicSquaresSettings(tileGapPx.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return MosaicSquaresSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
