package io.github.compilerstuck.control.config.audio;

import io.github.compilerstuck.control.config.json.JsonObject;
import java.util.Optional;

/**
 * Versioned JSON codec for {@link AudioSettings}: a clipboard envelope (export/import) and a
 * compact prefs blob. Hand-rolled (no JSON library), mirroring {@code VisualizationSettingsCodec}.
 */
public final class AudioSettingsCodec {

  public static final int SCHEMA_VERSION = 1;

  private AudioSettingsCodec() {}

  /** Clipboard / share envelope. */
  public static String encodeEnvelope(AudioSettings settings) {
    if (settings == null) {
      throw new IllegalArgumentException("settings");
    }
    StringBuilder sb = new StringBuilder(160);
    sb.append("{\"schemaVersion\":").append(SCHEMA_VERSION).append(",\"audioSettings\":");
    encode(sb, settings);
    sb.append('}');
    return sb.toString();
  }

  /**
   * Decodes a clipboard envelope. Returns empty on malformed input or unknown schema version.
   * Out-of-range numeric fields are clamped by the {@link AudioSettings} compact constructor.
   */
  public static Optional<DecodeResult> decodeEnvelope(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    try {
      JsonObject root = JsonObject.parse(raw.trim());
      int version = root.getInt("schemaVersion", -1);
      if (version != SCHEMA_VERSION) {
        return Optional.empty();
      }
      JsonObject settingsObj = root.getObject("audioSettings");
      if (settingsObj == null) {
        return Optional.empty();
      }
      AudioSettings settings = decode(settingsObj);
      return Optional.of(new DecodeResult(settings, settingsObj.hadOutOfRangeHint));
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }

  /** Prefs blob: same shape as the object nested inside the envelope. */
  public static String encodeStore(AudioSettings settings) {
    if (settings == null) {
      return "{}";
    }
    StringBuilder sb = new StringBuilder(128);
    encode(sb, settings);
    return sb.toString();
  }

  /** Decodes the prefs blob; returns defaults on malformed input. */
  public static AudioSettings decodeStore(String raw) {
    if (raw == null || raw.isBlank() || "{}".equals(raw.trim())) {
      return AudioSettings.defaults();
    }
    try {
      return decode(JsonObject.parse(raw.trim()));
    } catch (IllegalArgumentException ex) {
      return AudioSettings.defaults();
    }
  }

  private static AudioSettings decode(JsonObject obj) {
    AudioSettings d = AudioSettings.defaults();
    int instrumentProgram = obj.getInt("instrumentProgram", d.instrumentProgram());
    int volume = obj.getInt("volume", d.volume());
    int velocity = obj.getInt("velocity", d.velocity());
    int pan = obj.getInt("pan", d.pan());
    int lowNote = obj.getInt("lowNote", d.lowNote());
    int highNote = obj.getInt("highNote", d.highNote());
    int reverb = obj.getInt("reverb", d.reverb());
    int chorus = obj.getInt("chorus", d.chorus());
    int attackTime = obj.getInt("attackTime", d.attackTime());
    int releaseTime = obj.getInt("releaseTime", d.releaseTime());
    int brightness = obj.getInt("brightness", d.brightness());

    if (isOutOfRange(instrumentProgram)
        || isOutOfRange(volume)
        || velocity < AudioSettings.VELOCITY_MIN
        || velocity > AudioSettings.MIDI_MAX
        || isOutOfRange(pan)
        || isOutOfRange(lowNote)
        || isOutOfRange(highNote)
        || highNote < lowNote
        || isOutOfRange(reverb)
        || isOutOfRange(chorus)
        || isOutOfRange(attackTime)
        || isOutOfRange(releaseTime)
        || isOutOfRange(brightness)) {
      obj.hadOutOfRangeHint = true;
    }

    return new AudioSettings(
        instrumentProgram,
        volume,
        velocity,
        pan,
        lowNote,
        highNote,
        reverb,
        chorus,
        attackTime,
        releaseTime,
        brightness);
  }

  private static boolean isOutOfRange(int value) {
    return value < AudioSettings.MIDI_MIN || value > AudioSettings.MIDI_MAX;
  }

  private static void encode(StringBuilder sb, AudioSettings s) {
    sb.append('{');
    appendInt(sb, "instrumentProgram", s.instrumentProgram());
    sb.append(',');
    appendInt(sb, "volume", s.volume());
    sb.append(',');
    appendInt(sb, "velocity", s.velocity());
    sb.append(',');
    appendInt(sb, "pan", s.pan());
    sb.append(',');
    appendInt(sb, "lowNote", s.lowNote());
    sb.append(',');
    appendInt(sb, "highNote", s.highNote());
    sb.append(',');
    appendInt(sb, "reverb", s.reverb());
    sb.append(',');
    appendInt(sb, "chorus", s.chorus());
    sb.append(',');
    appendInt(sb, "attackTime", s.attackTime());
    sb.append(',');
    appendInt(sb, "releaseTime", s.releaseTime());
    sb.append(',');
    appendInt(sb, "brightness", s.brightness());
    sb.append('}');
  }

  private static void appendInt(StringBuilder sb, String key, int value) {
    sb.append('"').append(key).append("\":").append(value);
  }

  public record DecodeResult(AudioSettings settings, boolean valuesWereClamped) {}
}
