package io.github._66_m.control.config.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Shared helpers for golden-file tests over every per-visualization settings record. Run tests with
 * {@code -Dgolden.update=true} to (re)write the golden files from the current code.
 */
public final class VisualizationSettingsGolden {

  private VisualizationSettingsGolden() {}

  /** Every settings record type, ordered by visualization id. */
  public static List<Class<? extends VisualizationSettings>> settingsTypes() {
    return new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("io.github._66_m.control.config.visual")
            .stream()
            .filter(c -> c.isAssignableTo(VisualizationSettings.class))
            .filter(JavaClass::isRecord)
            .map(c -> c.reflect().asSubclass(VisualizationSettings.class))
            .<Class<? extends VisualizationSettings>>map(c -> c)
            .sorted(Comparator.comparing(VisualizationSettingsGolden::idOf))
            .toList();
  }

  public static VisualizationSettings defaultsOf(Class<? extends VisualizationSettings> type) {
    try {
      return (VisualizationSettings) type.getMethod("defaults").invoke(null);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(type.getName(), e);
    }
  }

  public static String idOf(Class<? extends VisualizationSettings> type) {
    return defaultsOf(type).visualizationId();
  }

  /**
   * A clipboard envelope whose settings push every field to an extreme: numbers to {@code +/-1e9},
   * booleans to {@code high}, enums to an unknown constant.
   */
  public static String extremeEnvelope(Class<? extends VisualizationSettings> type, boolean high) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"schemaVersion\":1,\"visualizationId\":\"")
        .append(idOf(type))
        .append("\",\"settings\":{");
    RecordComponent[] components = type.getRecordComponents();
    for (int i = 0; i < components.length; i++) {
      RecordComponent c = components[i];
      if (i > 0) {
        sb.append(',');
      }
      sb.append('"').append(c.getName()).append("\":");
      Class<?> t = c.getType();
      if (t == boolean.class) {
        sb.append(high);
      } else if (t.isEnum()) {
        sb.append("\"NO_SUCH_CONSTANT\"");
      } else {
        sb.append(high ? "1e9" : "-1e9");
      }
    }
    return sb.append("}}").toString();
  }

  /** A settings instance with every field moved off its default (in range), via the codec. */
  public static VisualizationSettings nonDefault(Class<? extends VisualizationSettings> type) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"schemaVersion\":1,\"visualizationId\":\"")
        .append(idOf(type))
        .append("\",\"settings\":{");
    RecordComponent[] components = type.getRecordComponents();
    VisualizationSettings defaults = defaultsOf(type);
    VisualizationSettings max =
        VisualizationSettingsCodec.decodeEnvelope(extremeEnvelope(type, true))
            .orElseThrow()
            .settings();
    VisualizationSettings min =
        VisualizationSettingsCodec.decodeEnvelope(extremeEnvelope(type, false))
            .orElseThrow()
            .settings();
    for (int i = 0; i < components.length; i++) {
      RecordComponent c = components[i];
      if (i > 0) {
        sb.append(',');
      }
      sb.append('"').append(c.getName()).append("\":");
      Class<?> t = c.getType();
      Object d = value(c, defaults);
      if (t == boolean.class) {
        sb.append(!(Boolean) d);
      } else if (t.isEnum()) {
        Object[] constants = t.getEnumConstants();
        sb.append('"').append(constants[constants.length - 1]).append('"');
      } else {
        double lo = ((Number) value(c, min)).doubleValue();
        double hi = ((Number) value(c, max)).doubleValue();
        // Three quarters of the way from min to max: in range and (almost surely) not the default.
        sb.append(lo + (hi - lo) * 0.75);
      }
    }
    sb.append("}}");
    return VisualizationSettingsCodec.decodeEnvelope(sb.toString()).orElseThrow().settings();
  }

  public static Object value(RecordComponent c, Object record) {
    try {
      return c.getAccessor().invoke(record);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(c.getName(), e);
    }
  }

  /** Compares {@code actual} with the golden file, or rewrites it with -Dgolden.update=true. */
  public static void assertGolden(String name, String actual) throws IOException {
    Path file = Path.of("src/test/resources/golden", name);
    if (Boolean.getBoolean("golden.update")) {
      Files.createDirectories(file.getParent());
      Files.writeString(file, actual, StandardCharsets.UTF_8);
      return;
    }
    assertEquals(Files.readString(file, StandardCharsets.UTF_8), actual, "golden file " + file);
  }
}
