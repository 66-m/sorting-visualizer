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
import java.util.Optional;
import java.util.function.Supplier;

/** Registry of customize panels by visualization id. */
public final class VisualizationCustomizePanels {

  private VisualizationCustomizePanels() {}

  public static boolean hasPanel(String visualizationId) {
    return forId(visualizationId).isPresent();
  }

  /** Defaults for a visualization id, if it has a customize panel. */
  public static Optional<VisualizationSettings> defaultsFor(String visualizationId) {
    return forId(visualizationId).map(supplier -> supplier.get().defaults());
  }

  public static Optional<Supplier<VisualizationCustomizePanel>> forId(String visualizationId) {
    if (CubeSettings.ID.equals(visualizationId)) {
      return Optional.of(CubeCustomizePanel::new);
    }
    if (CircleSettings.ID.equals(visualizationId)) {
      return Optional.of(CircleCustomizePanel::new);
    }
    if (ColorGradientGraphSettings.ID.equals(visualizationId)) {
      return Optional.of(ColorGradientGraphCustomizePanel::new);
    }
    if (CubicLinesSettings.ID.equals(visualizationId)) {
      return Optional.of(CubicLinesCustomizePanel::new);
    }
    if (DisparityChordsSettings.ID.equals(visualizationId)) {
      return Optional.of(DisparityChordsCustomizePanel::new);
    }
    if (DisparityCircleSettings.ID.equals(visualizationId)) {
      return Optional.of(DisparityCircleCustomizePanel::new);
    }
    if (DisparityCircleScatterSettings.ID.equals(visualizationId)) {
      return Optional.of(DisparityCircleScatterCustomizePanel::new);
    }
    if (DisparityCircleScatterLinkedSettings.ID.equals(visualizationId)) {
      return Optional.of(DisparityCircleScatterLinkedCustomizePanel::new);
    }
    if (DisparityPlaneSettings.ID.equals(visualizationId)) {
      return Optional.of(DisparityPlaneCustomizePanel::new);
    }
    if (DisparitySphereHoopsSettings.ID.equals(visualizationId)) {
      return Optional.of(DisparitySphereHoopsCustomizePanel::new);
    }
    if (DisparitySquareScatterSettings.ID.equals(visualizationId)) {
      return Optional.of(DisparitySquareScatterCustomizePanel::new);
    }
    if (HoopsSettings.ID.equals(visualizationId)) {
      return Optional.of(HoopsCustomizePanel::new);
    }
    if (ImageVerticalSettings.ID.equals(visualizationId)) {
      return Optional.of(ImageVerticalCustomizePanel::new);
    }
    if (ImageHorizontalSettings.ID.equals(visualizationId)) {
      return Optional.of(ImageHorizontalCustomizePanel::new);
    }
    if (MorphingShellSettings.ID.equals(visualizationId)) {
      return Optional.of(MorphingShellCustomizePanel::new);
    }
    if (MosaicSquaresSettings.ID.equals(visualizationId)) {
      return Optional.of(MosaicSquaresCustomizePanel::new);
    }
    if (NumberPlotSettings.ID.equals(visualizationId)) {
      return Optional.of(NumberPlotCustomizePanel::new);
    }
    if (PhyllotaxisSettings.ID.equals(visualizationId)) {
      return Optional.of(PhyllotaxisCustomizePanel::new);
    }
    if (PlaneSettings.ID.equals(visualizationId)) {
      return Optional.of(PlaneCustomizePanel::new);
    }
    if (PyramidSettings.ID.equals(visualizationId)) {
      return Optional.of(PyramidCustomizePanel::new);
    }
    if (ScatterPlotSettings.ID.equals(visualizationId)) {
      return Optional.of(ScatterPlotCustomizePanel::new);
    }
    if (ScatterPlotLinkedSettings.ID.equals(visualizationId)) {
      return Optional.of(ScatterPlotLinkedCustomizePanel::new);
    }
    if (SphereSettings.ID.equals(visualizationId)) {
      return Optional.of(SphereCustomizePanel::new);
    }
    if (SphereHoopsSettings.ID.equals(visualizationId)) {
      return Optional.of(SphereHoopsCustomizePanel::new);
    }
    if (SphericDisparityLinesSettings.ID.equals(visualizationId)) {
      return Optional.of(SphericDisparityLinesCustomizePanel::new);
    }
    if (SwirlDotsSettings.ID.equals(visualizationId)) {
      return Optional.of(SwirlDotsCustomizePanel::new);
    }
    return Optional.empty();
  }
}
