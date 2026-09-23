package io.github._66_m.control.ui.settingsfx.customize;

import io.github._66_m.control.config.visual.SettingsSchema;
import io.github._66_m.control.config.visual.VisualizationSettings;
import io.github._66_m.control.config.visual.VisualizationSettingsSchemas;
import java.util.Optional;
import java.util.function.Supplier;

/** Customize panels by visualization id, generated from each visualization's settings schema. */
public final class VisualizationCustomizePanels {

  private VisualizationCustomizePanels() {}

  public static boolean hasPanel(String visualizationId) {
    return forId(visualizationId).isPresent();
  }

  /** Defaults for a visualization id, if it has a customize panel. */
  public static Optional<VisualizationSettings> defaultsFor(String visualizationId) {
    return VisualizationSettingsSchemas.forId(visualizationId).map(SettingsSchema::defaults);
  }

  public static Optional<Supplier<VisualizationCustomizePanel>> forId(String visualizationId) {
    return VisualizationSettingsSchemas.forId(visualizationId).map(schema -> panelFactory(schema));
  }

  private static <S extends VisualizationSettings>
      Supplier<VisualizationCustomizePanel> panelFactory(SettingsSchema<S> schema) {
    return () -> new SchemaCustomizePanel<>(schema);
  }
}
