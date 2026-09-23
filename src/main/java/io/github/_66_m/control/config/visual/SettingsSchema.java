package io.github._66_m.control.config.visual;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Declarative description of one visualization's settings record: every tunable field with its
 * range, default and how it is labelled in the customize dialog. The JSON codec and the customize
 * panel are both generated from it, so a new customizable visualization only needs its settings
 * record plus a schema.
 *
 * <p>Each {@link Param#key()} must name a record component; the schema checks this (and the
 * component types) when it is created.
 *
 * @param <S> the settings record type
 */
public final class SettingsSchema<S extends VisualizationSettings> {

  /** One tunable field, in the order it appears in the customize panel. */
  public sealed interface Param permits DoubleParam, IntParam, BoolParam, EnumParam {
    /** Record component name; also the JSON key. */
    String key();

    /** Row label in the customize panel. */
    String label();

    /** Section heading the row is grouped under. */
    String section();
  }

  /**
   * A continuous value shown as a slider.
   *
   * @param format {@link String#format} pattern for the value label, e.g. {@code "%.2f rad/s"}
   */
  public record DoubleParam(
      String key,
      String label,
      String section,
      double min,
      double max,
      double defaultValue,
      String format)
      implements Param {}

  /** A whole number shown as a slider that snaps to integers. */
  public record IntParam(
      String key, String label, String section, int min, int max, int defaultValue)
      implements Param {}

  /** An on/off value shown as a checkbox. */
  public record BoolParam(String key, String label, String section, boolean defaultValue)
      implements Param {}

  /** One of an enum's constants, shown as a combo box. */
  public record EnumParam(String key, String label, String section, Enum<?> defaultValue)
      implements Param {
    public Enum<?>[] constants() {
      return defaultValue.getDeclaringClass().getEnumConstants();
    }
  }

  private final String id;
  private final Class<S> type;
  private final List<Param> params;
  private final Map<String, Param> byKey;
  private final RecordComponent[] components;
  private final Constructor<S> canonical;

  private SettingsSchema(String id, Class<S> type, List<Param> params) {
    this.id = id;
    this.type = type;
    this.params = List.copyOf(params);
    this.byKey = new LinkedHashMap<>();
    for (Param p : params) {
      if (byKey.put(p.key(), p) != null) {
        throw new IllegalArgumentException(type.getSimpleName() + ": duplicate key " + p.key());
      }
    }
    this.components = type.getRecordComponents();
    if (components == null) {
      throw new IllegalArgumentException(type.getName() + " is not a record");
    }
    for (RecordComponent c : components) {
      Param p = byKey.get(c.getName());
      if (p == null) {
        throw new IllegalArgumentException(type.getSimpleName() + ": no param for " + c.getName());
      }
      if (!accepts(p, c.getType())) {
        throw new IllegalArgumentException(
            type.getSimpleName() + ": param " + p.key() + " does not match " + c.getType());
      }
    }
    if (components.length != params.size()) {
      throw new IllegalArgumentException(type.getSimpleName() + ": params without a component");
    }
    try {
      this.canonical =
          type.getDeclaredConstructor(
              Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new));
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(type.getName(), e);
    }
  }

  /** Creates a schema; {@code params} are listed in customize-panel order. */
  public static <S extends VisualizationSettings> SettingsSchema<S> of(
      String id, Class<S> type, Param... params) {
    return new SettingsSchema<>(id, type, List.of(params));
  }

  public String id() {
    return id;
  }

  public Class<S> type() {
    return type;
  }

  /** Params in customize-panel order. */
  public List<Param> params() {
    return params;
  }

  /** Section headings in order of first appearance. */
  public List<String> sections() {
    LinkedHashSet<String> sections = new LinkedHashSet<>();
    for (Param p : params) {
      sections.add(p.section());
    }
    return new ArrayList<>(sections);
  }

  /** Record components in declaration order (the JSON field order). */
  public List<Param> paramsInRecordOrder() {
    return Arrays.stream(components).map(c -> byKey.get(c.getName())).toList();
  }

  /** Settings with every field at its default. */
  public S defaults() {
    return create(SettingsSchema::defaultValue);
  }

  /** Builds settings from one value per param (boxed double/int/boolean/enum). */
  public S create(Function<Param, Object> valueOf) {
    Object[] args = new Object[components.length];
    for (int i = 0; i < components.length; i++) {
      args[i] = valueOf.apply(byKey.get(components[i].getName()));
    }
    try {
      return canonical.newInstance(args);
    } catch (InvocationTargetException e) {
      throw new IllegalArgumentException(type.getSimpleName(), e.getCause());
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(type.getName(), e);
    }
  }

  /** Reads one field of {@code settings}. */
  public Object get(S settings, Param param) {
    for (RecordComponent c : components) {
      if (c.getName().equals(param.key())) {
        try {
          return c.getAccessor().invoke(settings);
        } catch (ReflectiveOperationException e) {
          throw new IllegalStateException(type.getName() + "." + c.getName(), e);
        }
      }
    }
    throw new IllegalArgumentException(param.key());
  }

  public static Object defaultValue(Param param) {
    return switch (param) {
      case DoubleParam d -> d.defaultValue();
      case IntParam i -> i.defaultValue();
      case BoolParam b -> b.defaultValue();
      case EnumParam e -> e.defaultValue();
    };
  }

  private static boolean accepts(Param param, Class<?> componentType) {
    return switch (param) {
      case DoubleParam d -> componentType == double.class;
      case IntParam i -> componentType == int.class;
      case BoolParam b -> componentType == boolean.class;
      case EnumParam e -> componentType == e.defaultValue().getDeclaringClass();
    };
  }
}
