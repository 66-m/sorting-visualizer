package io.github.compilerstuck.control.config.visual.json;

import java.util.LinkedHashMap;
import java.util.Map;

/** Minimal JSON object reader for shapes this app emits/accepts. Hand-rolled (no JSON library). */
public final class JsonObject {
  private final Map<String, Object> values = new LinkedHashMap<>();
  public boolean hadOutOfRangeHint;

  public static JsonObject parse(String json) {
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

  public Iterable<String> keys() {
    return values.keySet();
  }

  public String getString(String key, String defaultValue) {
    Object v = values.get(key);
    return v instanceof String s ? s : defaultValue;
  }

  public int getInt(String key, int defaultValue) {
    Object v = values.get(key);
    if (v instanceof Number n) {
      return n.intValue();
    }
    return defaultValue;
  }

  public double getDouble(String key, double defaultValue) {
    Object v = values.get(key);
    if (v instanceof Number n) {
      return n.doubleValue();
    }
    return defaultValue;
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    Object v = values.get(key);
    if (v instanceof Boolean b) {
      return b;
    }
    return defaultValue;
  }

  public JsonObject getObject(String key) {
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
        case 't' -> parseLiteral("true", true);
        case 'f' -> parseLiteral("false", false);
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
