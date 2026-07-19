package io.github.compilerstuck.control.config.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VisualizationSettingsCodecTest {

  @Test
  void envelopeRoundTrip() {
    CubeSettings original = new CubeSettings(0.5, 200, false, 4.0);
    String json = VisualizationSettingsCodec.encodeEnvelope(original);
    Optional<VisualizationSettingsCodec.DecodeResult> decoded =
        VisualizationSettingsCodec.decodeEnvelope(json);
    assertTrue(decoded.isPresent());
    assertEquals(original, decoded.get().settings());
    assertFalse(decoded.get().valuesWereClamped());
  }

  @Test
  void storeRoundTrip() {
    CubeSettings cube = CubeSettings.defaults();
    String blob = VisualizationSettingsCodec.encodeStore(Map.of("cube", cube));
    Map<String, VisualizationSettings> map = VisualizationSettingsCodec.decodeStore(blob);
    assertEquals(1, map.size());
    assertEquals(cube, map.get("cube"));
  }

  @Test
  void wrongSchemaRejected() {
    String json =
        "{\"schemaVersion\":99,\"visualizationId\":\"cube\",\"settings\":{\"fillOpacity\":120}}";
    assertTrue(VisualizationSettingsCodec.decodeEnvelope(json).isEmpty());
  }

  @Test
  void unknownVisualizationRejected() {
    String json =
        "{\"schemaVersion\":1,\"visualizationId\":\"bars\",\"settings\":{\"fillOpacity\":120}}";
    assertTrue(VisualizationSettingsCodec.decodeEnvelope(json).isEmpty());
  }

  @Test
  void missingFieldsUseDefaults() {
    String json = "{\"schemaVersion\":1,\"visualizationId\":\"cube\",\"settings\":{}}";
    Optional<VisualizationSettingsCodec.DecodeResult> decoded =
        VisualizationSettingsCodec.decodeEnvelope(json);
    assertTrue(decoded.isPresent());
    assertEquals(CubeSettings.defaults(), decoded.get().settings());
  }

  @Test
  void outOfRangeFieldsClamped() {
    String json =
        "{\"schemaVersion\":1,\"visualizationId\":\"cube\",\"settings\":{"
            + "\"rotationSpeedRadPerSec\":99,\"fillOpacity\":999,\"sceneScaleDivisor\":0.1}}";
    Optional<VisualizationSettingsCodec.DecodeResult> decoded =
        VisualizationSettingsCodec.decodeEnvelope(json);
    assertTrue(decoded.isPresent());
    CubeSettings s = (CubeSettings) decoded.get().settings();
    assertEquals(CubeSettings.ROTATION_SPEED_MAX, s.rotationSpeedRadPerSec(), 1e-9);
    assertEquals(CubeSettings.FILL_OPACITY_MAX, s.fillOpacity());
    assertEquals(CubeSettings.SCENE_SCALE_DIVISOR_MIN, s.sceneScaleDivisor(), 1e-9);
    assertTrue(decoded.get().valuesWereClamped());
  }

  @Test
  void malformedEnvelopeRejected() {
    assertTrue(VisualizationSettingsCodec.decodeEnvelope("not-json").isEmpty());
    assertTrue(VisualizationSettingsCodec.decodeEnvelope("").isEmpty());
    assertTrue(VisualizationSettingsCodec.decodeStore("nope").isEmpty());
  }
}
