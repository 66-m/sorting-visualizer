package io.github.compilerstuck.control.model;

import static org.junit.jupiter.api.Assertions.*;

import io.github.compilerstuck.control.config.DelayStrategy;
import io.github.compilerstuck.control.config.ShuffleType;
import io.github.compilerstuck.control.render.ProcessingContext;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import io.github.compilerstuck.sound.Sound;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SortingSessionManagerTest {

  private static final ProcessingContext NO_OP_PROCESSING = ms -> {};

  private ArrayController arrayController;
  private FakeSound sound;
  private SortingStateManager stateManager;
  private SortingSessionManager sessionManager;

  @BeforeEach
  void setUp() {
    arrayController = new ArrayController(5);
    arrayController.setShuffleType(ShuffleType.SORTED);
    sound = new FakeSound(arrayController);
    stateManager = new SortingStateManager();
    sessionManager = new SortingSessionManager(arrayController, sound, stateManager, 0, 0);
  }

  @Test
  @DisplayName("empty algorithm list is a no-op and does not hang")
  void emptyAlgorithmListIsNoOp() {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);

    sessionManager.startSortingSession(Collections.emptyList());
    sessionManager.waitForCompletion();

    assertTrue(System.nanoTime() < deadline, "waitForCompletion should return immediately");
    assertFalse(sessionManager.hasResults());
    assertFalse(stateManager.isRunning());
  }

  @Test
  @DisplayName("single instant algorithm completes with measurements and restart flag")
  void singleInstantAlgorithmCompletes() {
    InstantSortAlgorithm algorithm = new InstantSortAlgorithm(arrayController);
    stateManager.setRunning(true);

    sessionManager.startSortingSession(List.of(algorithm));
    sessionManager.waitForCompletion();

    assertTrue(sessionManager.hasResults());
    assertFalse(sessionManager.getComparisons().isEmpty());
    assertFalse(sessionManager.getRealTime().isEmpty());
    assertFalse(sessionManager.getSwaps().isEmpty());
    assertFalse(sessionManager.getWritesMain().isEmpty());
    assertFalse(sessionManager.getWritesAux().isEmpty());
    assertTrue(stateManager.shouldRestart());
    assertFalse(stateManager.isRunning());
  }

  @Test
  @DisplayName("cancel mid-run stops execution and clears running state")
  void cancelMidRunStopsExecution() {
    UntilCancelledAlgorithm algorithm = new UntilCancelledAlgorithm(arrayController);
    stateManager.setRunning(true);

    sessionManager.startSortingSession(List.of(algorithm));

    awaitUntilRunning(algorithm);
    sessionManager.cancel();
    sessionManager.waitForCompletion();

    assertFalse(stateManager.shouldContinueExecution());
    assertTrue(sessionManager.getCancellationToken().isCancelled());
    assertFalse(stateManager.isRunning());
  }

  @Test
  @DisplayName("comparison table flag enables showResults after successful run")
  void comparisonTableEnablesShowResults() {
    stateManager.setShowComparisonTable(true);
    stateManager.setRunning(true);

    sessionManager.startSortingSession(List.of(new InstantSortAlgorithm(arrayController)));
    sessionManager.waitForCompletion();

    assertTrue(stateManager.shouldShowResults());
  }

  @Test
  @DisplayName("exportCsv writes header and algorithm name after completed run")
  void exportCsvWritesExpectedContent(@TempDir Path tempDir) throws Exception {
    InstantSortAlgorithm algorithm = new InstantSortAlgorithm(arrayController);
    stateManager.setRunning(true);

    sessionManager.startSortingSession(List.of(algorithm));
    sessionManager.waitForCompletion();

    Path csv = tempDir.resolve("results.csv");
    sessionManager.exportCsv(csv, List.of(algorithm));

    String content = Files.readString(csv);
    assertTrue(
        content.startsWith(
            "algorithm,elements,comparisons,est_time_ms,swaps,writes_main,writes_aux\n"));
    assertTrue(content.contains("InstantSort"));
  }

  @Test
  @DisplayName("prepareForAlgorithm mutes and unmutes sound")
  void prepareMutesSound() {
    stateManager.setRunning(true);

    sessionManager.startSortingSession(List.of(new InstantSortAlgorithm(arrayController)));
    sessionManager.waitForCompletion();

    List<Boolean> muteCalls = sound.getMuteCalls();
    assertTrue(muteCalls.size() >= 2, "expected mute calls during prepare");
    assertEquals(Boolean.TRUE, muteCalls.get(0));
    assertEquals(Boolean.FALSE, muteCalls.get(1));
  }

  private static void awaitUntilRunning(UntilCancelledAlgorithm algorithm) {
    long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(500);
    while (!algorithm.isRunning() && System.nanoTime() < deadline) {
      Thread.onSpinWait();
    }
    assertTrue(algorithm.isRunning(), "algorithm thread should have entered sort()");
  }

  private static final class FakeSound extends Sound {
    private final List<Boolean> muteCalls = new ArrayList<>();

    FakeSound(ArrayModel arrayController) {
      super(arrayController);
    }

    @Override
    public void playSound(int value) {}

    @Override
    public void mute(boolean mute) {
      muteCalls.add(mute);
    }

    List<Boolean> getMuteCalls() {
      return new ArrayList<>(muteCalls);
    }
  }

  private static class InstantSortAlgorithm extends SortingAlgorithm {
    private final ArrayModel model;

    InstantSortAlgorithm(ArrayModel arrayController) {
      super(arrayController, NO_OP_PROCESSING);
      model = arrayController;
      name = "InstantSort";
      setDelay(false);
      setDelayStrategy(DelayStrategy.never());
    }

    @Override
    public void sort() {
      report(name);
      for (int i = 0; i < model.getLength(); i++) {
        model.set(i, i);
      }
    }
  }

  private static class UntilCancelledAlgorithm extends SortingAlgorithm {
    private volatile boolean running;

    UntilCancelledAlgorithm(ArrayModel arrayController) {
      super(arrayController, NO_OP_PROCESSING);
      name = "UntilCancelled";
      setDelay(false);
      setDelayStrategy(DelayStrategy.never());
    }

    boolean isRunning() {
      return running;
    }

    @Override
    public void sort() {
      running = true;
      report(name);
      while (!isCancelled()) {
        Thread.onSpinWait();
      }
    }
  }
}
