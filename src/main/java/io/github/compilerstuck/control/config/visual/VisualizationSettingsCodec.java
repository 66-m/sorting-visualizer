package io.github.compilerstuck.control.config.visual;

import io.github.compilerstuck.control.config.json.JsonObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Versioned JSON codec for clipboard envelopes and the prefs map of per-visualization settings.
 * Hand-rolled (no JSON library); only the shapes this app emits/accepts.
 *
 * <p>Per-visualization encode/decode lives in {@link VisualizationSettingsCodecs}.
 */
public final class VisualizationSettingsCodec {

  public static final int SCHEMA_VERSION = 1;

  private VisualizationSettingsCodec() {}

  /** Clipboard / share envelope. */
  public static String encodeEnvelope(VisualizationSettings settings) {
    if (settings == null) {
      throw new IllegalArgumentException("settings");
    }
    StringBuilder sb = new StringBuilder(128);
    sb.append("{\"schemaVersion\":")
        .append(SCHEMA_VERSION)
        .append(",\"visualizationId\":")
        .append(VisualizationSettingsCodecs.quote(settings.visualizationId()))
        .append(",\"settings\":");
    VisualizationSettingsCodecs.encode(sb, settings);
    sb.append('}');
    return sb.toString();
  }

  /**
   * Decodes a clipboard envelope. Returns empty on malformed input, unknown schema, or unknown
   * visualization id. Out-of-range numeric fields are clamped by the settings record constructors.
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
      String id = root.getString("visualizationId", "");
      if (id.isBlank()) {
        return Optional.empty();
      }
      JsonObject settingsObj = root.getObject("settings");
      if (settingsObj == null) {
        return Optional.empty();
      }
      VisualizationSettings settings = VisualizationSettingsCodecs.decode(id, settingsObj);
      if (settings == null) {
        return Optional.empty();
      }
      boolean clamped = settingsObj.hadOutOfRangeHint;
      return Optional.of(new DecodeResult(settings, clamped));
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }

  /** Prefs blob: {@code {"cube":{...}, ...}}. */
  public static String encodeStore(Map<String, VisualizationSettings> byId) {
    StringBuilder sb = new StringBuilder(64);
    sb.append('{');
    boolean first = true;
    if (byId != null) {
      for (Map.Entry<String, VisualizationSettings> e : byId.entrySet()) {
        if (e.getKey() == null || e.getKey().isBlank() || e.getValue() == null) {
          continue;
        }
        if (!e.getKey().equals(e.getValue().visualizationId())) {
          continue;
        }
        if (!first) {
          sb.append(',');
        }
        first = false;
        sb.append(VisualizationSettingsCodecs.quote(e.getKey())).append(':');
        VisualizationSettingsCodecs.encode(sb, e.getValue());
      }
    }
    sb.append('}');
    return sb.toString();
  }

  /** Decodes prefs blob; skips unknown ids / malformed entries. */
  public static Map<String, VisualizationSettings> decodeStore(String raw) {
    if (raw == null || raw.isBlank() || "{}".equals(raw.trim())) {
      return Map.of();
    }
    try {
      JsonObject root = JsonObject.parse(raw.trim());
      Map<String, VisualizationSettings> out = new LinkedHashMap<>();
      for (String key : root.keys()) {
        JsonObject obj = root.getObject(key);
        if (obj == null) {
          continue;
        }
        VisualizationSettings settings = VisualizationSettingsCodecs.decode(key, obj);
        if (settings != null) {
          out.put(key, settings);
        }
      }
      return Collections.unmodifiableMap(out);
    } catch (IllegalArgumentException ex) {
      return Map.of();
    }
  }

  public record DecodeResult(VisualizationSettings settings, boolean valuesWereClamped) {}
}
