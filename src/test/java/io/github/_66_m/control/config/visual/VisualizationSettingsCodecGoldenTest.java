package io.github._66_m.control.config.visual;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Pins the JSON produced and accepted for every visualization's settings, so saved preferences and
 * shared clipboard strings keep working across refactors.
 */
class VisualizationSettingsCodecGoldenTest {

  @Test
  void codecOutputMatchesGoldenFile() throws Exception {
    StringBuilder out = new StringBuilder();
    for (Class<? extends VisualizationSettings> type :
        VisualizationSettingsGolden.settingsTypes()) {
      String id = VisualizationSettingsGolden.idOf(type);
      VisualizationSettings defaults = VisualizationSettingsGolden.defaultsOf(type);
      out.append("== ").append(id).append('\n');
      out.append("defaults ")
          .append(VisualizationSettingsCodec.encodeEnvelope(defaults))
          .append('\n');
      for (boolean high : new boolean[] {true, false}) {
        var decoded =
            VisualizationSettingsCodec.decodeEnvelope(
                    VisualizationSettingsGolden.extremeEnvelope(type, high))
                .orElseThrow();
        out.append(high ? "max " : "min ")
            .append("clamped=")
            .append(decoded.valuesWereClamped())
            .append(' ')
            .append(VisualizationSettingsCodec.encodeEnvelope(decoded.settings()))
            .append('\n');
      }
      out.append("nonDefault ")
          .append(
              VisualizationSettingsCodec.encodeEnvelope(
                  VisualizationSettingsGolden.nonDefault(type)))
          .append('\n');
    }

    Map<String, VisualizationSettings> store =
        VisualizationSettingsGolden.settingsTypes().stream()
            .collect(
                Collectors.toMap(
                    VisualizationSettingsGolden::idOf,
                    VisualizationSettingsGolden::nonDefault,
                    (a, b) -> a,
                    java.util.LinkedHashMap::new));
    String encodedStore = VisualizationSettingsCodec.encodeStore(store);
    out.append("== store\n").append(encodedStore).append('\n');
    out.append("storeRoundTrip ")
        .append(
            VisualizationSettingsCodec.encodeStore(
                VisualizationSettingsCodec.decodeStore(encodedStore)))
        .append('\n');

    VisualizationSettingsGolden.assertGolden("visualization-settings-codec.txt", out.toString());
  }

  @Test
  void everySettingsTypeIsCovered() {
    assertFalse(VisualizationSettingsGolden.settingsTypes().isEmpty());
  }
}
