package io.github.compilerstuck.control.model;

import static org.junit.jupiter.api.Assertions.*;

import io.github.compilerstuck.control.config.ShuffleType;
import io.github.compilerstuck.control.render.DelayContext;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import io.github.compilerstuck.sound.Sound;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SortingSessionManagerTest {

  private static final DelayContext NO_OP_PROCESSING = () -> {};

  private ArrayController arrayModel;
  private FakeSound sound;
  private SortingStateManager stateManager;
  private SortingSessionManager sessionManager;

  @BeforeEach
  void setUp() {
    arrayModel = new ArrayController(5);
    arrayModel.setShuffleType(ShuffleType.SORTED);
    sound = new FakeSound(arrayModel);
    stateManager = new SortingStateManager();
    sessionManager = new SortingSessionManager(arrayModel, sound, stateManager, 0, 0);
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
    InstantSortAlgorithm algorithm = new InstantSortAlgorithm(arrayModel);
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
  @DisplayName("session end drains leftover FrameGate credits")
  void sessionEndDrainsFrameGate() {
    FrameGate gate = new FrameGate();
    gate.grant(16);
    sessionManager.setFrameGate(gate);
    stateManager.setRunning(true);

    sessionManager.startSortingSession(List.of(new InstantSortAlgorithm(arrayModel)));
    sessionManager.waitForCompletion();

    assertEquals(0, gate.availableCredits());
    assertFalse(gate.isCancelled());
  }

  @Test
  @DisplayName("post-sort pause drains leftover credits so awaitIdle is not blocked")
  void postSortPauseDrainsLeftoverCredits() throws Exception {
    FrameGate gate = new FrameGate();
    SortingSessionManager delayedSession =
        new SortingSessionManager(arrayModel, sound, stateManager, 0, 250);
    delayedSession.setFrameGate(gate);
    stateManager.setRunning(true);

    CountDownLatch algorithmFinished = new CountDownLatch(1);
    AtomicBoolean idleDuringPause = new AtomicBoolean(false);
    AtomicBoolean suspendedDuringPause = new AtomicBoolean(false);
    Thread renderWaiter =
        new Thread(
            () -> {
              try {
                assertTrue(algorithmFinished.await(2, TimeUnit.SECONDS));
                Thread.sleep(20);
                suspendedDuringPause.set(stateManager.isFrameGateSuspended());
                long start = System.nanoTime();
                gate.awaitIdle();
                idleDuringPause.set(System.nanoTime() - start < TimeUnit.MILLISECONDS.toNanos(100));
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            },
            "awaitIdle-during-pause");
    renderWaiter.start();

    delayedSession.startSortingSession(
        List.of(new CreditLeavingSortAlgorithm(arrayModel, gate, algorithmFinished)));
    delayedSession.waitForCompletion();
    renderWaiter.join(2000);

    assertTrue(suspendedDuringPause.get(), "frame gate should be suspended during post-sort pause");
    assertTrue(idleDuringPause.get(), "awaitIdle should return quickly during post-sort pause");
    assertEquals(0, gate.availableCredits());
    assertFalse(stateManager.isFrameGateSuspended());
  }

  @Test
  @DisplayName("prepareForAlgorithm clears shuffling flag and drains FrameGate after shuffle")
  void prepareClearsShufflingAndDrainsGate() {
    FrameGate gate = new FrameGate();
    sessionManager.setFrameGate(gate);
    arrayModel.setDelayContext(
        () -> {
          gate.grant(1);
          try {
            gate.awaitStep();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });
    stateManager.setRunning(true);

    sessionManager.startSortingSession(List.of(new InstantSortAlgorithm(arrayModel)));
    sessionManager.waitForCompletion();

    assertFalse(stateManager.isShuffling());
    assertEquals(0, gate.availableCredits());
  }

  @Test
  @DisplayName("skip mid-run advances to the next algorithm without recording the skipped one")
  void skipCurrentAdvancesToNextAlgorithm() {
    UntilCancelledAlgorithm first = new UntilCancelledAlgorithm(arrayModel);
    InstantSortAlgorithm second = new InstantSortAlgorithm(arrayModel);
    stateManager.setRunning(true);
    stateManager.setShowComparisonTable(true);

    sessionManager.startSortingSession(List.of(first, second));

    awaitUntilRunning(first);
    sessionManager.skipCurrent();
    sessionManager.waitForCompletion();

    assertTrue(stateManager.shouldContinueExecution());
    assertEquals(1, sessionManager.getCompletedAlgorithms().size());
    assertEquals("InstantSort", sessionManager.getCompletedAlgorithms().get(0).getName());
    assertEquals(1, sessionManager.getComparisons().size());
    assertTrue(stateManager.shouldShowResults());
    assertTrue(stateManager.shouldRestart());
    assertFalse(stateManager.isRunning());
  }

  @Test
  @DisplayName("cancel mid-run stops execution and clears running state")
  void cancelMidRunStopsExecution() {
    UntilCancelledAlgorithm algorithm = new UntilCancelledAlgorithm(arrayModel);
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

    sessionManager.startSortingSession(List.of(new InstantSortAlgorithm(arrayModel)));
    sessionManager.waitForCompletion();

    assertTrue(stateManager.shouldShowResults());
  }

  @Test
  @DisplayName("exportCsv writes header and algorithm name after completed run")
  void exportCsvWritesExpectedContent(@TempDir Path tempDir) throws Exception {
    InstantSortAlgorithm algorithm = new InstantSortAlgorithm(arrayModel);
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

    sessionManager.startSortingSession(List.of(new InstantSortAlgorithm(arrayModel)));
    sessionManager.waitForCompletion();

    List<Boolean> muteCalls = sound.getMuteCalls();
    assertTrue(muteCalls.size() >= 2, "expected mute calls during prepare");
    assertEquals(true, muteCalls.get(0));
    assertEquals(false, muteCalls.get(1));
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

    FakeSound(ArrayModel arrayModel) {
      super(arrayModel);
    }

    @Override
    public void playSound(int index) {}

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

    InstantSortAlgorithm(ArrayModel arrayModel) {
      super(arrayModel, NO_OP_PROCESSING);
      model = arrayModel;
      name = "InstantSort";
      setDelay(false);
    }

    @Override
    public void sort() {
      report(name);
      for (int i = 0; i < model.getLength(); i++) {
        model.set(i, i);
      }
    }
  }

  /** Leaves unused FrameGate credits, mimicking a high steps-per-frame budget at sort end. */
  private static class CreditLeavingSortAlgorithm extends SortingAlgorithm {
    private final ArrayModel model;
    private final FrameGate gate;
    private final CountDownLatch finished;

    CreditLeavingSortAlgorithm(ArrayModel arrayModel, FrameGate gate, CountDownLatch finished) {
      super(arrayModel, NO_OP_PROCESSING);
      model = arrayModel;
      this.gate = gate;
      this.finished = finished;
      name = "CreditLeavingSort";
      setDelay(false);
    }

    @Override
    public void sort() {
      report(name);
      for (int i = 0; i < model.getLength(); i++) {
        model.set(i, i);
      }
      gate.grant(500);
      finished.countDown();
    }
  }

  private static class UntilCancelledAlgorithm extends SortingAlgorithm {
    private volatile boolean running;

    UntilCancelledAlgorithm(ArrayModel arrayModel) {
      super(arrayModel, NO_OP_PROCESSING);
      name = "UntilCancelled";
      setDelay(false);
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
