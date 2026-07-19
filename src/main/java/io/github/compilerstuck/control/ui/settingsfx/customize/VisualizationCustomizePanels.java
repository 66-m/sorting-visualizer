package io.github.compilerstuck.control.ui.settingsfx.customize;

import io.github.compilerstuck.control.config.visual.CubeSettings;
import java.util.Optional;
import java.util.function.Supplier;

/** Registry of customize panels by visualization id. */
public final class VisualizationCustomizePanels {

  private VisualizationCustomizePanels() {}

  public static boolean hasPanel(String visualizationId) {
    return forId(visualizationId).isPresent();
  }

  public static Optional<Supplier<VisualizationCustomizePanel>> forId(String visualizationId) {
    if (CubeSettings.ID.equals(visualizationId)) {
      return Optional.of(CubeCustomizePanel::new);
    }
    return Optional.empty();
  }
}
