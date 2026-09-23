package io.github._66_m.control.config.visual;

import io.github._66_m.control.config.json.JsonObject;
import io.github._66_m.control.config.visual.SettingsSchema.BoolParam;
import io.github._66_m.control.config.visual.SettingsSchema.DoubleParam;
import io.github._66_m.control.config.visual.SettingsSchema.EnumParam;
import io.github._66_m.control.config.visual.SettingsSchema.IntParam;
import io.github._66_m.control.config.visual.SettingsSchema.Param;

/**
 * Per-visualization JSON encode/decode, generated from each {@link SettingsSchema}.
 * Package-private; used by {@link VisualizationSettingsCodec}.
 *
 * <p>Fields are written in record-component order. On decode, a missing field takes its default; an
 * out-of-range number or unknown enum constant sets {@link JsonObject#hadOutOfRangeHint} and is
 * clamped (by the record constructor) or replaced by the default.
 */
final class VisualizationSettingsCodecs {

  private VisualizationSettingsCodecs() {}

  /** Decodes {@code obj} as the settings of visualization {@code id}, or null if unknown. */
  static VisualizationSettings decode(String id, JsonObject obj) {
    return VisualizationSettingsSchemas.forId(id).map(schema -> decode(schema, obj)).orElse(null);
  }

  static void encode(StringBuilder sb, VisualizationSettings settings) {
    SettingsSchema<?> schema =
        VisualizationSettingsSchemas.forId(settings.visualizationId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Unsupported settings type: " + settings.getClass()));
    encodeWith(schema, sb, settings);
  }

  private static <S extends VisualizationSettings> void encodeWith(
      SettingsSchema<S> schema, StringBuilder sb, VisualizationSettings settings) {
    S typed = schema.type().cast(settings);
    sb.append('{');
    boolean first = true;
    for (Param param : schema.paramsInRecordOrder()) {
      if (!first) {
        sb.append(',');
      }
      first = false;
      Object value = schema.get(typed, param);
      switch (param) {
        case DoubleParam d -> appendNumber(sb, d.key(), (Double) value);
        case IntParam i -> sb.append(quote(i.key())).append(':').append((int) (Integer) value);
        case BoolParam b -> sb.append(quote(b.key())).append(':').append((boolean) (Boolean) value);
        case EnumParam e ->
            sb.append(quote(e.key())).append(':').append(quote(((Enum<?>) value).name()));
      }
    }
    sb.append('}');
  }

  private static VisualizationSettings decode(SettingsSchema<?> schema, JsonObject obj) {
    return schema.create(param -> decodeValue(param, obj));
  }

  private static Object decodeValue(Param param, JsonObject obj) {
    return switch (param) {
      case DoubleParam d -> {
        double v = obj.getDouble(d.key(), d.defaultValue());
        if (v != Numbers.clamp(v, d.min(), d.max())) {
          obj.hadOutOfRangeHint = true;
        }
        yield v;
      }
      case IntParam i -> {
        int v = (int) Math.round(obj.getDouble(i.key(), i.defaultValue()));
        if (v < i.min() || v > i.max()) {
          obj.hadOutOfRangeHint = true;
        }
        yield v;
      }
      case BoolParam b -> obj.getBoolean(b.key(), b.defaultValue());
      case EnumParam e -> {
        String raw = obj.getString(e.key(), e.defaultValue().name());
        for (Enum<?> constant : e.constants()) {
          if (constant.name().equals(raw)) {
            yield constant;
          }
        }
        obj.hadOutOfRangeHint = true;
        yield e.defaultValue();
      }
    };
  }

  static void appendNumber(StringBuilder sb, String key, double value) {
    sb.append(quote(key)).append(':');
    if (Double.isFinite(value)) {
      // Full precision; Locale.ROOT avoids comma decimals.
      sb.append(Double.toString(value));
    } else {
      sb.append('0');
    }
  }

  static String quote(String s) {
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
}
