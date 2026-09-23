package io.github._66_m.control.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github._66_m.control.config.CanvasBackground;
import io.github._66_m.control.config.ShuffleType;
import io.github._66_m.control.render.DelayContext;
import io.github._66_m.control.render.RenderSystem;
import io.github._66_m.control.ui.ResultsTableRenderer;
import io.github._66_m.sortingalgorithms.SortingAlgorithm;
import io.github._66_m.sound.SilentSound;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Pins the observable output of a finished session (CSV export, console timestamps, results table
 * text) so internal refactors of result bookkeeping cannot change it by accident.
 */
class SessionResultsGoldenTest {

  private static final DelayContext NO_OP = () -> {};

  private ArrayController array;
  private SortingStateManager state;
  private SortingSessionManager session;

  @BeforeEach
  void runTwoDeterministicAlgorithms() {
    array = new ArrayController(6);
    array.setShuffleType(ShuffleType.REVERSE);
    state = new SortingStateManager();
    session = new SortingSessionManager(array, new SilentSound(array), state, 0, 0);
    state.setRunning(true);
    session.startSortingSession(
        List.of(
            new FixedWorkAlgorithm(array, "Plain Sort", 3, 1),
            new FixedWorkAlgorithm(array, "Quoted \"Comma, Sort\"", 5, 2)));
    session.waitForCompletion();
  }

  @Test
  void csvExportIsStable(@TempDir Path dir) throws Exception {
    Path csv = dir.resolve("results.csv");
    session.exportCsv(csv);

    List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
    assertEquals(3, lines.size());
    assertEquals(
        "algorithm,elements,comparisons,est_time_ms,swaps,writes_main,writes_aux", lines.get(0));
    assertRow(lines.get(1), "Plain Sort,6,3,", ",1,8,6");
    assertRow(lines.get(2), "\"Quoted \"\"Comma, Sort\"\"\",6,5,", ",2,10,12");
  }

  @Test
  void consoleTimestampsAreStable() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream original = System.out;
    System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
    try {
      session.printTimestampsToConsole();
    } finally {
      System.setOut(original);
    }
    String nl = System.lineSeparator();
    assertEquals(
        "\n\nTimestamps:\n" + nl + "00:00 Plain Sort" + nl + "00:00 Quoted \"Comma, Sort\"" + nl,
        out.toString(StandardCharsets.UTF_8));
  }

  @Test
  void resultsTableTextIsStable() {
    List<String> texts = new ArrayList<>();
    new ResultsTableRenderer()
        .render(recordingRenderSystem(texts), CanvasBackground.DARK, session.getResults());

    assertEquals(
        List.of(
            "Alg. name",
            "Elements",
            "Comparisons",
            "Est. real time",
            "Swaps",
            "Writes main",
            "Writes aux",
            "Plain Sort",
            "6",
            "3",
            texts.get(10),
            "1",
            "8",
            "6",
            "Quoted \"Comma, Sort\"",
            "6",
            "5",
            texts.get(17),
            "2",
            "10",
            "12"),
        texts);
    assertTrue(texts.get(10).matches("~\\d+,\\d\\dms"), texts.get(10));
    assertTrue(texts.get(17).matches("~\\d+,\\d\\dms"), texts.get(17));
  }

  /** The time column is wall-clock based; everything around it must match exactly. */
  private static void assertRow(String line, String prefix, String suffix) {
    assertTrue(line.startsWith(prefix), line);
    assertTrue(line.endsWith(suffix), line);
    String time = line.substring(prefix.length(), line.length() - suffix.length());
    assertTrue(time.matches("\\d+\\.\\d\\d"), "time column: " + time);
    assertEquals(7, csvFieldCount(line), "every row must have one field per header column");
  }

  /** Counts CSV fields, treating commas inside double quotes as part of the field. */
  private static int csvFieldCount(String line) {
    int fields = 1;
    boolean quoted = false;
    for (char c : line.toCharArray()) {
      if (c == '"') {
        quoted = !quoted;
      } else if (c == ',' && !quoted) {
        fields++;
      }
    }
    return fields;
  }

  private static RenderSystem recordingRenderSystem(List<String> texts) {
    return (RenderSystem)
        Proxy.newProxyInstance(
            RenderSystem.class.getClassLoader(),
            new Class<?>[] {RenderSystem.class},
            (proxy, method, args) -> {
              switch (method.getName()) {
                case "drawText" -> texts.add((String) args[0]);
                case "getWidth" -> {
                  return 1600;
                }
                case "getHeight" -> {
                  return 900;
                }
                default -> {}
              }
              Class<?> r = method.getReturnType();
              if (r == boolean.class) return false;
              if (r == int.class) return 0;
              if (r == float.class) return 0f;
              if (r == double.class) return 0d;
              if (r == long.class) return 0L;
              return null;
            });
  }

  /** Performs a fixed amount of counted work so every metric except time is deterministic. */
  private static final class FixedWorkAlgorithm extends SortingAlgorithm {
    private final ArrayModel model;
    private final int comparisons;
    private final int swaps;

    FixedWorkAlgorithm(ArrayModel model, String name, int comparisons, int swaps) {
      super(model, NO_OP);
      this.model = model;
      setName(name);
      setDelay(false);
      setAlternativeSize(model.getLength());
      this.comparisons = comparisons;
      this.swaps = swaps;
    }

    @Override
    public void sort() {
      model.addComparisons(comparisons);
      for (int i = 0; i < swaps; i++) {
        model.swap(0, 1);
      }
      model.addWritesAux(model.getLength() * swaps);
      for (int i = 0; i < model.getLength(); i++) {
        model.set(i, i);
      }
    }
  }
}
