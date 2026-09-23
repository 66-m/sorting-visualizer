package io.github._66_m.control.config.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github._66_m.control.config.visual.SettingsSchema.BoolParam;
import io.github._66_m.control.config.visual.SettingsSchema.DoubleParam;
import org.junit.jupiter.api.Test;

class SettingsSchemaTest {

  @Test
  void everyRegisteredSchemaBuildsItsDefaults() {
    for (SettingsSchema<?> schema : VisualizationSettingsSchemas.all()) {
      assertEquals(schema.id(), schema.defaults().visualizationId());
    }
  }

  @Test
  void schemaRejectsAParamThatNamesNoRecordComponent() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            SettingsSchema.of(
                HoopsSettings.ID,
                HoopsSettings.class,
                new DoubleParam("radiusScal", "Radius", "LAYOUT", 0.5, 1.0, 0.9, "%.3f")));
  }

  @Test
  void schemaRejectsAParamOfTheWrongType() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            SettingsSchema.of(
                HoopsSettings.ID,
                HoopsSettings.class,
                new BoolParam("radiusScale", "Radius", "LAYOUT", true)));
  }
}
