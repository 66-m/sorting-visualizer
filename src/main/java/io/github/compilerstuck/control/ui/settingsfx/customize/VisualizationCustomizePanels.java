package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.CircleSettings;
import io.github.compilerstuck.control.config.visual.ColorGradientGraphSettings;
import io.github.compilerstuck.control.config.visual.CubeSettings;
import io.github.compilerstuck.control.config.visual.CubicLinesSettings;
import io.github.compilerstuck.control.config.visual.DisparityChordsSettings;
import io.github.compilerstuck.control.config.visual.DisparityCircleScatterLinkedSettings;
import io.github.compilerstuck.control.config.visual.DisparityCircleScatterSettings;
import io.github.compilerstuck.control.config.visual.DisparityCircleSettings;
import io.github.compilerstuck.control.config.visual.DisparityPlaneSettings;
import io.github.compilerstuck.control.config.visual.DisparitySphereHoopsSettings;
import io.github.compilerstuck.control.config.visual.DisparitySquareScatterSettings;
import io.github.compilerstuck.control.config.visual.HoopsSettings;
import io.github.compilerstuck.control.config.visual.ImageHorizontalSettings;
import io.github.compilerstuck.control.config.visual.ImageVerticalSettings;
import io.github.compilerstuck.control.config.visual.MorphingShellSettings;
import io.github.compilerstuck.control.config.visual.MosaicSquaresSettings;
import io.github.compilerstuck.control.config.visual.NumberPlotSettings;
import io.github.compilerstuck.control.config.visual.PhyllotaxisSettings;
import io.github.compilerstuck.control.config.visual.PlaneSettings;
import io.github.compilerstuck.control.config.visual.PyramidSettings;
import io.github.compilerstuck.control.config.visual.ScatterPlotLinkedSettings;
import io.github.compilerstuck.control.config.visual.ScatterPlotSettings;
import io.github.compilerstuck.control.config.visual.SphereHoopsSettings;
import io.github.compilerstuck.control.config.visual.SphereSettings;
import io.github.compilerstuck.control.config.visual.SphericDisparityLinesSettings;
import io.github.compilerstuck.control.config.visual.SwirlDotsSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/** Registry of customize panels by visualization id. */
public final class VisualizationCustomizePanels {

  private static final Map<String, Supplier<VisualizationCustomizePanel>> BY_ID =
      Map.ofEntries(
          Map.entry(CubeSettings.ID, CubeCustomizePanel::new),
          Map.entry(CircleSettings.ID, CircleCustomizePanel::new),
          Map.entry(ColorGradientGraphSettings.ID, ColorGradientGraphCustomizePanel::new),
          Map.entry(CubicLinesSettings.ID, CubicLinesCustomizePanel::new),
          Map.entry(DisparityChordsSettings.ID, DisparityChordsCustomizePanel::new),
          Map.entry(DisparityCircleSettings.ID, DisparityCircleCustomizePanel::new),
          Map.entry(DisparityCircleScatterSettings.ID, DisparityCircleScatterCustomizePanel::new),
          Map.entry(
              DisparityCircleScatterLinkedSettings.ID,
              DisparityCircleScatterLinkedCustomizePanel::new),
          Map.entry(DisparityPlaneSettings.ID, DisparityPlaneCustomizePanel::new),
          Map.entry(DisparitySphereHoopsSettings.ID, DisparitySphereHoopsCustomizePanel::new),
          Map.entry(DisparitySquareScatterSettings.ID, DisparitySquareScatterCustomizePanel::new),
          Map.entry(HoopsSettings.ID, HoopsCustomizePanel::new),
          Map.entry(ImageVerticalSettings.ID, ImageVerticalCustomizePanel::new),
          Map.entry(ImageHorizontalSettings.ID, ImageHorizontalCustomizePanel::new),
          Map.entry(MorphingShellSettings.ID, MorphingShellCustomizePanel::new),
          Map.entry(MosaicSquaresSettings.ID, MosaicSquaresCustomizePanel::new),
          Map.entry(NumberPlotSettings.ID, NumberPlotCustomizePanel::new),
          Map.entry(PhyllotaxisSettings.ID, PhyllotaxisCustomizePanel::new),
          Map.entry(PlaneSettings.ID, PlaneCustomizePanel::new),
          Map.entry(PyramidSettings.ID, PyramidCustomizePanel::new),
          Map.entry(ScatterPlotSettings.ID, ScatterPlotCustomizePanel::new),
          Map.entry(ScatterPlotLinkedSettings.ID, ScatterPlotLinkedCustomizePanel::new),
          Map.entry(SphereSettings.ID, SphereCustomizePanel::new),
          Map.entry(SphereHoopsSettings.ID, SphereHoopsCustomizePanel::new),
          Map.entry(SphericDisparityLinesSettings.ID, SphericDisparityLinesCustomizePanel::new),
          Map.entry(SwirlDotsSettings.ID, SwirlDotsCustomizePanel::new));

  private VisualizationCustomizePanels() {}

  public static boolean hasPanel(String visualizationId) {
    return forId(visualizationId).isPresent();
  }

  /** Defaults for a visualization id, if it has a customize panel. */
  public static Optional<VisualizationSettings> defaultsFor(String visualizationId) {
    return forId(visualizationId).map(supplier -> supplier.get().defaults());
  }

  public static Optional<Supplier<VisualizationCustomizePanel>> forId(String visualizationId) {
    return Optional.ofNullable(BY_ID.get(visualizationId));
  }
}
