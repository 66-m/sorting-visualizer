package io.github._66_m.control.model;

import io.github._66_m.control.ui.TimeEstimateFormat;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Writes session results as CSV or as a console timestamp list. */
public final class ResultsExport {

  private ResultsExport() {}

  /** Writes comparison metrics as CSV (columns aligned with the on-screen results table). */
  public static void writeCsv(Path path, List<RunResult> results) throws IOException {
    try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      writer.write("algorithm,elements,comparisons,est_time_ms,swaps,writes_main,writes_aux\n");
      for (RunResult r : results) {
        writer.write(csvEscape(r.algorithmName()));
        writer.write(',');
        writer.write(Integer.toString(r.elements()));
        writer.write(',');
        writer.write(Long.toString(r.comparisons()));
        writer.write(',');
        // Dot decimal separator: a comma would split the value into two CSV columns.
        writer.write(TimeEstimateFormat.formatMillis(r.realTimeNanos()));
        writer.write(',');
        writer.write(Long.toString(r.swaps()));
        writer.write(',');
        writer.write(Long.toString(r.writesMain()));
        writer.write(',');
        writer.write(Long.toString(r.writesAux()));
        writer.write('\n');
      }
    }
  }

  /** Prints when each algorithm finished, relative to the session start, as {@code mm:ss name}. */
  public static void printTimestamps(PrintStream out, List<RunResult> results) {
    out.println("\n\nTimestamps:\n");
    for (RunResult r : results) {
      int seconds = r.elapsedSeconds();
      out.println(String.format("%02d:%02d", seconds / 60, seconds % 60) + " " + r.algorithmName());
    }
  }

  private static String csvEscape(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return '"' + value.replace("\"", "\"\"") + '"';
    }
    return value;
  }
}
