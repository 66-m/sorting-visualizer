package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.DisparityChordsSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/** Draft editor for {@link DisparityChordsSettings}. */
public final class DisparityChordsCustomizePanel implements VisualizationCustomizePanel {

  private final Slider radiusScale =
      new Slider(
          DisparityChordsSettings.RADIUS_SCALE_MIN,
          DisparityChordsSettings.RADIUS_SCALE_MAX,
          DisparityChordsSettings.DEFAULT_RADIUS_SCALE);
  private final Label radiusScaleValue = CustomizePanelSupport.valueLabel();
  private final Slider lineThickness =
      new Slider(
          DisparityChordsSettings.LINE_THICKNESS_MIN,
          DisparityChordsSettings.LINE_THICKNESS_MAX,
          DisparityChordsSettings.DEFAULT_LINE_THICKNESS);
  private final Label lineThicknessValue = CustomizePanelSupport.valueLabel();
  private final Slider coincidentMarkerSize =
      new Slider(
          DisparityChordsSettings.COINCIDENT_MARKER_SIZE_MIN,
          DisparityChordsSettings.COINCIDENT_MARKER_SIZE_MAX,
          DisparityChordsSettings.DEFAULT_COINCIDENT_MARKER_SIZE);
  private final Label coincidentMarkerSizeValue = CustomizePanelSupport.valueLabel();
  private final Slider chordOpacity =
      new Slider(
          DisparityChordsSettings.CHORD_OPACITY_MIN,
          DisparityChordsSettings.CHORD_OPACITY_MAX,
          DisparityChordsSettings.DEFAULT_CHORD_OPACITY);
  private final Label chordOpacityValue = CustomizePanelSupport.valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    CustomizePanelSupport.configureSlider(radiusScale, false);
    CustomizePanelSupport.configureSlider(lineThickness, false);
    CustomizePanelSupport.configureSlider(coincidentMarkerSize, false);
    CustomizePanelSupport.configureSlider(chordOpacity, true);
    CustomizePanelSupport.bindValueLabel(
        radiusScale, radiusScaleValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(
        lineThickness, lineThicknessValue, v -> String.format("%.2f", v));
    CustomizePanelSupport.bindValueLabel(
        coincidentMarkerSize, coincidentMarkerSizeValue, v -> String.format("%.1f", v));
    CustomizePanelSupport.bindValueLabel(
        chordOpacity, chordOpacityValue, v -> String.format("%d", Math.round(v)));
    bindDraftChanges();

    VBox section =
        CustomizePanelSupport.section(
            "LAYOUT",
            CustomizePanelSupport.sliderRow(
                "Radius",
                radiusScale,
                radiusScaleValue,
                DisparityChordsSettings.DEFAULT_RADIUS_SCALE),
            CustomizePanelSupport.sliderRow(
                "Line thickness",
                lineThickness,
                lineThicknessValue,
                DisparityChordsSettings.DEFAULT_LINE_THICKNESS),
            CustomizePanelSupport.sliderRow(
                "Marker size",
                coincidentMarkerSize,
                coincidentMarkerSizeValue,
                DisparityChordsSettings.DEFAULT_COINCIDENT_MARKER_SIZE),
            CustomizePanelSupport.sliderRow(
                "Chord opacity",
                chordOpacity,
                chordOpacityValue,
                DisparityChordsSettings.DEFAULT_CHORD_OPACITY));

    VBox root = new VBox(SettingsLayout.GAP_MD, section);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    DisparityChordsSettings s =
        settings instanceof DisparityChordsSettings c ? c : DisparityChordsSettings.defaults();
    loading = true;
    try {
      radiusScale.setValue(s.radiusScale());
      lineThickness.setValue(s.lineThickness());
      coincidentMarkerSize.setValue(s.coincidentMarkerSize());
      chordOpacity.setValue(s.chordOpacity());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new DisparityChordsSettings(
        radiusScale.getValue(),
        lineThickness.getValue(),
        coincidentMarkerSize.getValue(),
        (int) Math.round(chordOpacity.getValue()));
  }

  @Override
  public VisualizationSettings defaults() {
    return DisparityChordsSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    radiusScale.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    lineThickness.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    coincidentMarkerSize.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
    chordOpacity.valueProperty().addListener((obs, o, v) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }
}
