package io.github.compilerstuck.control.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.config.ShuffleType;
import io.github.compilerstuck.control.render.CountingDelayContext;
import io.github.compilerstuck.control.render.DelayContext;
import io.github.compilerstuck.sortingalgorithms.BogoSort;
import io.github.compilerstuck.sortingalgorithms.BubbleSort;
import io.github.compilerstuck.sortingalgorithms.GravitySort;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import io.github.compilerstuck.sound.Sound;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EqualizeSortDurationSessionTest {

  private static final DelayContext NO_OP = () -> {};

  private ArrayController array;
  private SortingStateManager stateManager;
  private SortingSessionManager sessionManager;

  @BeforeEach
  void setUp() {
    array = new ArrayController(32);
    stateManager = new SortingStateManager();
    FakeSound sound = new FakeSound(array);
    sessionManager = new SortingSessionManager(array, sound, stateManager, 0, 0);
    sessionManager.setEqualizeSupport(() -> true, () -> 10.0, () -> NO_OP);
  }

  @Test
  @DisplayName("dry-run restores array and arms equalize pacing with counted steps")
  void dryRunRestoresAndArmsPacing() {
    reverseInPlace(array);
    int[] before = Arrays.copyOf(array.getArray(), array.getLength());

    CountingSortStub algorithm = new CountingSortStub(array, 7);
    assertTrue(sessionManager.tryArmEqualizePacing(algorithm, NO_OP));

    assertArrayEquals(before, array.getArray());
    assertEquals(0, array.getComparisons());
    assertEquals(0, array.getSwaps());
    assertEquals(0, array.getWrites());
    assertTrue(stateManager.equalizePacing().isActive());
    assertEquals(7, stateManager.equalizePacing().totalSteps());
    assertEquals(0, stateManager.equalizePacing().frameBeats());
    assertEquals(10f, stateManager.equalizePacing().effectiveTargetSec());
  }

  @Test
  @DisplayName("Bogo Sort is skipped for equalization")
  void bogoIsSkipped() {
    BogoSort bogo = new BogoSort(array);
    assertFalse(sessionManager.tryArmEqualizePacing(bogo, NO_OP));
    assertFalse(stateManager.equalizePacing().isActive());
  }

  @Test
  @DisplayName("Bubble Sort visual steps differ strongly by shuffle type")
  void bubbleStepsVaryByShuffle() {
    int n = 40;
    ArrayController almost = new ArrayController(n);
    almost.setShuffleType(ShuffleType.ALMOST_SORTED);
    shuffleSilent(almost);

    ArrayController reverse = new ArrayController(n);
    reverse.setShuffleType(ShuffleType.REVERSE);
    shuffleSilent(reverse);

    long almostSteps = countSteps(new BubbleSort(almost), almost);
    long reverseSteps = countSteps(new BubbleSort(reverse), reverse);

    assertTrue(
        reverseSteps > almostSteps * 5, () -> "reverse=" + reverseSteps + " almost=" + almostSteps);
  }

  @Test
  @DisplayName("Gravity Sort equalize uses stride so large-n budgets stay near the target")
  void gravityEqualizeUsesStrideForBudget() {
    array = new ArrayController(120);
    reverseInPlace(array);
    FakeSound sound = new FakeSound(array);
    sessionManager = new SortingSessionManager(array, sound, stateManager, 0, 0);
    sessionManager.setEqualizeSupport(() -> true, () -> 1.0, () -> NO_OP);

    int natural = GravitySort.estimateFrameBeats(array);
    assertEquals(119, natural);

    GravitySort gravity = new GravitySort(array);
    assertTrue(sessionManager.tryArmEqualizePacing(gravity, NO_OP));

    // budget = maxPerFrame(120)*60*1 ≈ 64*60 = 3840 > 119 → stride 1
    assertEquals(1, gravity.getColumnStride());
    EqualizePacing pacing = stateManager.equalizePacing();
    assertEquals(natural, pacing.totalSteps());
    assertTrue(pacing.batchBeats());
  }

  @Test
  @DisplayName("Gravity equalize at 100k uses a large stride and few visual beats")
  void gravityEqualizeLargeNUsesLargeStride() {
    array = new ArrayController(10_000);
    reverseInPlace(array);
    FakeSound sound = new FakeSound(array);
    sessionManager = new SortingSessionManager(array, sound, stateManager, 0, 0);
    sessionManager.setEqualizeSupport(() -> true, () -> 2.0, () -> NO_OP);

    GravitySort gravity = new GravitySort(array);
    assertTrue(sessionManager.tryArmEqualizePacing(gravity, NO_OP));

    assertTrue(gravity.getColumnStride() > 1);
    EqualizePacing pacing = stateManager.equalizePacing();
    assertTrue(pacing.totalSteps() < 5_000, () -> "visual beats=" + pacing.totalSteps());
    assertEquals(
        GravitySort.countVisualBeats(
            GravitySort.estimateFrameBeats(array), gravity.getColumnStride()),
        pacing.totalSteps());
  }

  @Test
  @DisplayName("Gravity sparse path with stride still finishes sorted")
  void gravityStrideStillSorts() {
    ArrayController big = new ArrayController(200);
    reverseInPlace(big);
    // Force sparse path
    GravitySort gravity = new GravitySort(big);
    gravity.setColumnStride(17);
    gravity.setDelay(false);
    gravity.sort();
    assertTrue(big.isSorted());
  }

  @Test
  @DisplayName("non-Gravity dry-run suspends FrameGate and drains leftover credits")
  void dryRunSuspendsFrameGate() throws Exception {
    FrameGate gate = new FrameGate();
    sessionManager.setFrameGate(gate);
    gate.grant(50);

    SlowCountingStub slow = new SlowCountingStub(array, 3, 80);
    boolean[] sawSuspended = {false};
    Thread sampler =
        new Thread(
            () -> {
              long deadline = System.nanoTime() + 2_000_000_000L;
              while (System.nanoTime() < deadline) {
                if (stateManager.isFrameGateSuspended()) {
                  sawSuspended[0] = true;
                  return;
                }
                Thread.onSpinWait();
              }
            },
            "suspend-sampler");
    sampler.start();
    assertTrue(sessionManager.tryArmEqualizePacing(slow, NO_OP));
    sampler.join(3000);
    assertTrue(sawSuspended[0], "dry-run should suspend FrameGate while counting");
    assertFalse(stateManager.isFrameGateSuspended());
    assertEquals(0, gate.availableCredits());
  }

  @Test
  @DisplayName("restoreContents copies without bumping write counters")
  void restoreContentsDoesNotCountWrites() {
    int[] snapshot = Arrays.copyOf(array.getArray(), array.getLength());
    array.swap(0, array.getLength() - 1);
    long writes = array.getWrites();
    array.restoreContents(snapshot);
    assertEquals(writes, array.getWrites());
    assertArrayEquals(snapshot, array.getArray());
  }

  private static long countSteps(SortingAlgorithm algorithm, ArrayController model) {
    CountingDelayContext counter = new CountingDelayContext();
    algorithm.setDelayContext(counter);
    algorithm.setDelay(true);
    algorithm.sort();
    return counter.stepCount();
  }

  private static void shuffleSilent(ArrayController model) {
    model.setDelayContext(NO_OP);
    model.setOperationReporter(OperationReporter.NOOP);
    model.shuffle();
  }

  private static void reverseInPlace(ArrayController model) {
    int n = model.getLength();
    for (int i = 0; i < n / 2; i++) {
      int a = model.get(i);
      int b = model.get(n - 1 - i);
      // avoid swap counters for setup clarity
      model.getArray()[i] = b;
      model.getArray()[n - 1 - i] = a;
    }
  }

  private static final class FakeSound extends Sound {
    FakeSound(ArrayModel arrayModel) {
      super(arrayModel);
    }

    @Override
    public void playSound(int index) {}

    @Override
    public void mute(boolean mute) {}
  }

  /** Deterministic algorithm that fires a fixed number of delay() calls. */
  private static final class CountingSortStub extends SortingAlgorithm {
    private final int steps;

    CountingSortStub(ArrayModel model, int steps) {
      super(model, NO_OP);
      this.steps = steps;
      this.name = "CountingSortStub";
    }

    @Override
    public void sort() {
      for (int i = 0; i < steps && !isCancelled(); i++) {
        delay();
      }
    }
  }

  /** Like {@link CountingSortStub} but sleeps so a sampler can observe FrameGate suspension. */
  private static final class SlowCountingStub extends SortingAlgorithm {
    private final int steps;
    private final long sleepMs;

    SlowCountingStub(ArrayModel model, int steps, long sleepMs) {
      super(model, NO_OP);
      this.steps = steps;
      this.sleepMs = sleepMs;
      this.name = "SlowCountingStub";
    }

    @Override
    public void sort() {
      try {
        Thread.sleep(sleepMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      for (int i = 0; i < steps && !isCancelled(); i++) {
        delay();
      }
    }
  }
}
