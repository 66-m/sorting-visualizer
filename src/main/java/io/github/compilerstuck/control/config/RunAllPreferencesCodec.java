package io.github.compilerstuck.control.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serializes run-all order/selection as {@code id:0|1,id:0|1,...}. Malformed tokens are skipped
 * with a warning; never throws.
 */
public final class RunAllPreferencesCodec {

  private static final Logger LOGGER = Logger.getLogger(RunAllPreferencesCodec.class.getName());

  private RunAllPreferencesCodec() {}

  public static String encode(List<RunAllEntryPref> entries) {
    if (entries == null || entries.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (RunAllEntryPref entry : entries) {
      if (entry == null || entry.id() == null || entry.id().isBlank()) {
        continue;
      }
      if (sb.length() > 0) {
        sb.append(',');
      }
      sb.append(entry.id()).append(':').append(entry.selected() ? '1' : '0');
    }
    return sb.toString();
  }

  public static List<RunAllEntryPref> decode(String raw) {
    List<RunAllEntryPref> result = new ArrayList<>();
    if (raw == null || raw.isBlank()) {
      return result;
    }
    int start = 0;
    while (start <= raw.length()) {
      int comma = raw.indexOf(',', start);
      String token = comma < 0 ? raw.substring(start) : raw.substring(start, comma);
      start = comma < 0 ? raw.length() + 1 : comma + 1;
      String trimmed = token.trim();
      if (trimmed.isEmpty()) {
        continue;
      }
      int colon = trimmed.lastIndexOf(':');
      if (colon <= 0 || colon == trimmed.length() - 1) {
        LOGGER.log(Level.WARNING, "Skipping malformed runAllEntries token: {0}", trimmed);
        continue;
      }
      String id = trimmed.substring(0, colon).trim();
      String flag = trimmed.substring(colon + 1).trim();
      if (id.isEmpty() || (!"0".equals(flag) && !"1".equals(flag))) {
        LOGGER.log(Level.WARNING, "Skipping malformed runAllEntries token: {0}", trimmed);
        continue;
      }
      result.add(new RunAllEntryPref(id, "1".equals(flag)));
    }
    return result;
  }
}
