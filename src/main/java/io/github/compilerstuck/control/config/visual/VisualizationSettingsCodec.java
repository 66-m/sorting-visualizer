package io.github.compilerstuck.control.config.visual;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Versioned JSON codec for clipboard envelopes and the prefs map of per-visualization settings.
 * Hand-rolled (no JSON library); only the shapes this app emits/accepts.
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
        .append(quote(settings.visualizationId()))
        .append(",\"settings\":");
    appendSettingsObject(sb, settings);
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
      VisualizationSettings settings = decodeSettings(id, settingsObj);
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
        sb.append(quote(e.getKey())).append(':');
        appendSettingsObject(sb, e.getValue());
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
        VisualizationSettings settings = decodeSettings(key, obj);
        if (settings != null) {
          out.put(key, settings);
        }
      }
      return Collections.unmodifiableMap(out);
    } catch (IllegalArgumentException ex) {
      return Map.of();
    }
  }

  private static VisualizationSettings decodeSettings(String id, JsonObject obj) {
    if (CubeSettings.ID.equals(id)) {
      CubeSettings defaults = CubeSettings.defaults();
      double rot = obj.getDouble("rotationSpeedRadPerSec", defaults.rotationSpeedRadPerSec());
      int opacity = (int) Math.round(obj.getDouble("fillOpacity", defaults.fillOpacity()));
      boolean wire = obj.getBoolean("wireframeEnabled", defaults.wireframeEnabled());
      double scale = obj.getDouble("sceneScaleDivisor", defaults.sceneScaleDivisor());
      if (rot != clamp(rot, CubeSettings.ROTATION_SPEED_MIN, CubeSettings.ROTATION_SPEED_MAX)
          || opacity < CubeSettings.FILL_OPACITY_MIN
          || opacity > CubeSettings.FILL_OPACITY_MAX
          || scale
              != clamp(
                  scale,
                  CubeSettings.SCENE_SCALE_DIVISOR_MIN,
                  CubeSettings.SCENE_SCALE_DIVISOR_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      // tiltDegrees / boxSpinXRad from older blobs are ignored (fixed in the renderer).
      return new CubeSettings(rot, opacity, wire, scale);
    }
    return null;
  }

  private static void appendSettingsObject(StringBuilder sb, VisualizationSettings settings) {
    if (settings instanceof CubeSettings cube) {
      sb.append('{');
      appendNumber(sb, "rotationSpeedRadPerSec", cube.rotationSpeedRadPerSec());
      sb.append(',');
      sb.append(quote("fillOpacity")).append(':').append(cube.fillOpacity());
      sb.append(',');
      sb.append(quote("wireframeEnabled")).append(':').append(cube.wireframeEnabled());
      sb.append(',');
      appendNumber(sb, "sceneScaleDivisor", cube.sceneScaleDivisor());
      sb.append('}');
      return;
    }
    throw new IllegalArgumentException("Unsupported settings type: " + settings.getClass());
  }

  private static void appendNumber(StringBuilder sb, String key, double value) {
    sb.append(quote(key)).append(':');
    if (Double.isFinite(value)) {
      // Full precision; Locale.ROOT avoids comma decimals.
      sb.append(Double.toString(value));
    } else {
      sb.append('0');
    }
  }

  private static String quote(String s) {
    StringBuilder sb = new StringBuilder(s.length() + 2);
    sb.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\', '"' -> sb.append('\\').append(c);
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        default -> sb.append(c);
      }
    }
    sb.append('"');
    return sb.toString();
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  public record DecodeResult(VisualizationSettings settings, boolean valuesWereClamped) {}

  /** Minimal JSON object reader for our emitted shapes. */
  static final class JsonObject {
    private final Map<String, Object> values = new LinkedHashMap<>();
    boolean hadOutOfRangeHint;

    static JsonObject parse(String json) {
      Parser p = new Parser(json);
      Object value = p.parseValue();
      p.skipWs();
      if (!p.eof()) {
        throw new IllegalArgumentException("Trailing input");
      }
      if (!(value instanceof JsonObject obj)) {
        throw new IllegalArgumentException("Expected object");
      }
      return obj;
    }

    Iterable<String> keys() {
      return values.keySet();
    }

    String getString(String key, String defaultValue) {
      Object v = values.get(key);
      return v instanceof String s ? s : defaultValue;
    }

    int getInt(String key, int defaultValue) {
      Object v = values.get(key);
      if (v instanceof Number n) {
        return n.intValue();
      }
      return defaultValue;
    }

    double getDouble(String key, double defaultValue) {
      Object v = values.get(key);
      if (v instanceof Number n) {
        return n.doubleValue();
      }
      return defaultValue;
    }

    boolean getBoolean(String key, boolean defaultValue) {
      Object v = values.get(key);
      if (v instanceof Boolean b) {
        return b;
      }
      return defaultValue;
    }

    JsonObject getObject(String key) {
      Object v = values.get(key);
      return v instanceof JsonObject obj ? obj : null;
    }

    private static final class Parser {
      private final String s;
      private int i;

      Parser(String s) {
        this.s = s;
      }

      boolean eof() {
        return i >= s.length();
      }

      void skipWs() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
          i++;
        }
      }

      Object parseValue() {
        skipWs();
        if (eof()) {
          throw new IllegalArgumentException("Unexpected end");
        }
        char c = s.charAt(i);
        return switch (c) {
          case '{' -> parseObject();
          case '"' -> parseString();
          case 't' -> parseLiteral("true", Boolean.TRUE);
          case 'f' -> parseLiteral("false", Boolean.FALSE);
          case 'n' -> parseLiteral("null", null);
          case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> parseNumber();
          default -> throw new IllegalArgumentException("Unexpected '" + c + "'");
        };
      }

      JsonObject parseObject() {
        expect('{');
        JsonObject obj = new JsonObject();
        skipWs();
        if (peek('}')) {
          i++;
          return obj;
        }
        while (true) {
          skipWs();
          String key = parseString();
          skipWs();
          expect(':');
          Object value = parseValue();
          obj.values.put(key, value);
          skipWs();
          if (peek('}')) {
            i++;
            return obj;
          }
          expect(',');
        }
      }

      String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (!eof()) {
          char c = s.charAt(i++);
          if (c == '"') {
            return sb.toString();
          }
          if (c == '\\') {
            if (eof()) {
              throw new IllegalArgumentException("Bad escape");
            }
            char e = s.charAt(i++);
            sb.append(
                switch (e) {
                  case '"', '\\', '/' -> e;
                  case 'n' -> '\n';
                  case 'r' -> '\r';
                  case 't' -> '\t';
                  default -> throw new IllegalArgumentException("Bad escape");
                });
          } else {
            sb.append(c);
          }
        }
        throw new IllegalArgumentException("Unterminated string");
      }

      Number parseNumber() {
        int start = i;
        if (peek('-')) {
          i++;
        }
        while (!eof() && Character.isDigit(s.charAt(i))) {
          i++;
        }
        if (!eof() && s.charAt(i) == '.') {
          i++;
          while (!eof() && Character.isDigit(s.charAt(i))) {
            i++;
          }
        }
        if (!eof() && (s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
          i++;
          if (!eof() && (s.charAt(i) == '+' || s.charAt(i) == '-')) {
            i++;
          }
          while (!eof() && Character.isDigit(s.charAt(i))) {
            i++;
          }
        }
        String num = s.substring(start, i);
        if (num.indexOf('.') >= 0 || num.indexOf('e') >= 0 || num.indexOf('E') >= 0) {
          return Double.parseDouble(num);
        }
        long l = Long.parseLong(num);
        if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
          return (int) l;
        }
        return l;
      }

      Object parseLiteral(String lit, Object value) {
        if (s.regionMatches(i, lit, 0, lit.length())) {
          i += lit.length();
          return value;
        }
        throw new IllegalArgumentException("Expected " + lit);
      }

      void expect(char c) {
        skipWs();
        if (eof() || s.charAt(i) != c) {
          throw new IllegalArgumentException("Expected '" + c + "'");
        }
        i++;
      }

      boolean peek(char c) {
        return !eof() && s.charAt(i) == c;
      }
    }
  }
}
