package io.github.compilerstuck.control.config.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class AudioSettingsCodecTest {

  @Test
  void envelopeRoundTrip() {
    AudioSettings original = new AudioSettings(80, 110, 100, 64, 24, 72, 10, 5, 70, 60, 90);
    String json = AudioSettingsCodec.encodeEnvelope(original);
    Optional<AudioSettingsCodec.DecodeResult> decoded = AudioSettingsCodec.decodeEnvelope(json);
    assertTrue(decoded.isPresent());
    assertEquals(original, decoded.get().settings());
    assertFalse(decoded.get().valuesWereClamped());
  }

  @Test
  void storeRoundTrip() {
    AudioSettings settings = AudioSettings.defaults();
    String blob = AudioSettingsCodec.encodeStore(settings);
    assertEquals(settings, AudioSettingsCodec.decodeStore(blob));
  }

  @Test
  void emptyStoreDecodesToDefaults() {
    assertEquals(AudioSettings.defaults(), AudioSettingsCodec.decodeStore("{}"));
    assertEquals(AudioSettings.defaults(), AudioSettingsCodec.decodeStore(""));
    assertEquals(AudioSettings.defaults(), AudioSettingsCodec.decodeStore(null));
  }

  @Test
  void wrongSchemaRejected() {
    String json = "{\"schemaVersion\":99,\"audioSettings\":{\"volume\":100}}";
    assertTrue(AudioSettingsCodec.decodeEnvelope(json).isEmpty());
  }

  @Test
  void malformedJsonRejected() {
    assertTrue(AudioSettingsCodec.decodeEnvelope("not json").isEmpty());
    assertTrue(AudioSettingsCodec.decodeEnvelope(null).isEmpty());
    assertTrue(AudioSettingsCodec.decodeEnvelope("").isEmpty());
  }

  @Test
  void missingFieldsUseDefaults() {
    String json = "{\"schemaVersion\":1,\"audioSettings\":{}}";
    Optional<AudioSettingsCodec.DecodeResult> decoded = AudioSettingsCodec.decodeEnvelope(json);
    assertTrue(decoded.isPresent());
    assertEquals(AudioSettings.defaults(), decoded.get().settings());
    assertFalse(decoded.get().valuesWereClamped());
  }

  @Test
  void outOfRangeFieldsClampedAndFlagged() {
    String json =
        "{\"schemaVersion\":1,\"audioSettings\":{"
            + "\"instrumentProgram\":999,\"volume\":-10,\"velocity\":0,\"highNote\":5,\"lowNote\":50}}";
    Optional<AudioSettingsCodec.DecodeResult> decoded = AudioSettingsCodec.decodeEnvelope(json);
    assertTrue(decoded.isPresent());
    AudioSettings s = decoded.get().settings();
    assertEquals(127, s.instrumentProgram());
    assertEquals(0, s.volume());
    assertEquals(1, s.velocity());
    assertEquals(50, s.lowNote());
    assertEquals(50, s.highNote());
    assertTrue(decoded.get().valuesWereClamped());
  }
}
