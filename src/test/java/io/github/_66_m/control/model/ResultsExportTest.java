package io.github._66_m.control.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResultsExportTest {

  @Test
  void timestampsUseEachAlgorithmsStartTime() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ResultsExport.printTimestamps(
        new PrintStream(out, true, StandardCharsets.UTF_8),
        List.of(result("Quicksort", 0), result("Merge Sort", 11), result("Tim Sort", 206)));

    String nl = System.lineSeparator();
    assertEquals(
        "\n\nTimestamps:\n"
            + nl
            + "00:00 Quicksort"
            + nl
            + "00:11 Merge Sort"
            + nl
            + "03:26 Tim Sort"
            + nl,
        out.toString(StandardCharsets.UTF_8));
  }

  private static RunResult result(String name, int startSeconds) {
    return new RunResult(name, 100, 0, 0, 0, 0, 0, startSeconds);
  }
}
