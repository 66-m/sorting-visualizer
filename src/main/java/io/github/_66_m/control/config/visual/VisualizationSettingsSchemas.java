package io.github._66_m.control.config.visual;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Every customizable visualization's {@link SettingsSchema}, keyed by visualization id. */
public final class VisualizationSettingsSchemas {

  private static final List<SettingsSchema<?>> ALL =
      List.of(
          CircleSettings.SCHEMA,
          ColorGradientGraphSettings.SCHEMA,
          CubeSettings.SCHEMA,
          CubicLinesSettings.SCHEMA,
          DisparityChordsSettings.SCHEMA,
          DisparityCircleSettings.SCHEMA,
          DisparityCircleScatterSettings.SCHEMA,
          DisparityCircleScatterLinkedSettings.SCHEMA,
          DisparityPlaneSettings.SCHEMA,
          DisparitySphereHoopsSettings.SCHEMA,
          DisparitySquareScatterSettings.SCHEMA,
          HoopsSettings.SCHEMA,
          ImageHorizontalSettings.SCHEMA,
          ImageVerticalSettings.SCHEMA,
          MorphingShellSettings.SCHEMA,
          MosaicSquaresSettings.SCHEMA,
          NumberPlotSettings.SCHEMA,
          PhyllotaxisSettings.SCHEMA,
          PlaneSettings.SCHEMA,
          PyramidSettings.SCHEMA,
          ScatterPlotSettings.SCHEMA,
          ScatterPlotLinkedSettings.SCHEMA,
          SphereSettings.SCHEMA,
          SphereHoopsSettings.SCHEMA,
          SphericDisparityLinesSettings.SCHEMA,
          SwirlDotsSettings.SCHEMA);

  private static final Map<String, SettingsSchema<?>> BY_ID = index(ALL);

  private VisualizationSettingsSchemas() {}

  public static List<SettingsSchema<?>> all() {
    return ALL;
  }

  public static Optional<SettingsSchema<?>> forId(String visualizationId) {
    return Optional.ofNullable(BY_ID.get(visualizationId));
  }

  private static Map<String, SettingsSchema<?>> index(List<SettingsSchema<?>> schemas) {
    Map<String, SettingsSchema<?>> byId = new LinkedHashMap<>();
    for (SettingsSchema<?> schema : schemas) {
      if (byId.put(schema.id(), schema) != null) {
        throw new IllegalStateException("Duplicate settings schema id: " + schema.id());
      }
    }
    return Map.copyOf(byId);
  }
}
