package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.MosaicSquaresSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
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
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(tileGapPx, false);
    CustomizePanelSupport.bindValueLabel(tileGapPx, tileGapPxValue, v -> String.format("%.1f", v));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Tile gap", tileGapPx, tileGapPxValue, MosaicSquaresSettings.DEFAULT_TILE_GAP_PX));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    MosaicSquaresSettings s =
        settings instanceof MosaicSquaresSettings c ? c : MosaicSquaresSettings.defaults();
    loading = true;
    try {
      tileGapPx.setValue(s.tileGapPx());
    } finally {
      loading = false;
    }
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
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    tileGapPx.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
