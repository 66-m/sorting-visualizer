package io.github.compilerstuck.control.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.config.AppConfig;
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
    assertEquals(1, algorithm.getDelayStride());
    assertFalse(stateManager.isEqualizePreparing());
  }

  @Test
  @DisplayName("step count above equalize budget applies frame-based delay stride")
  void excessStepsApplyDelayStride() {
    // frameBudget = 60 * 0.1 = 6; highThroughput = 500 * 6 = 3000
    // rawSteps 10_000 > 3000 → stride = ceil(10000/6) = 1667, visualSteps = 6, maxSPF = 1
    sessionManager.setEqualizeSupport(() -> true, () -> 0.1, () -> NO_OP);
    CountingSortStub algorithm = new CountingSortStub(array, 10_000);
    assertTrue(sessionManager.tryArmEqualizePacing(algorithm, NO_OP));

    assertEquals(1667, algorithm.getDelayStride());
    EqualizePacing pacing = stateManager.equalizePacing();
    assertTrue(pacing.isActive());
    assertEquals(6, pacing.totalSteps());
    assertEquals(1, pacing.maxStepsPerFrame());
  }

  @Test
  @DisplayName("planDelayStride uses multi-credit budget when steps fit, else one beat per frame")
  void planDelayStrideChoosesMode() {
    SortingSessionManager.DelayStridePlan fits = SortingSessionManager.planDelayStride(1_000, 2f);
    assertEquals(1, fits.stride());
    assertEquals(1_000, fits.visualSteps());
    assertEquals(AppConfig.EQUALIZE_MAX_STEPS_PER_FRAME, fits.maxStepsPerFrame());

    // 2s → 120 frames; 1.25e9 steps need a large stride and 1 credit/frame
    long bubbleRaw = SortingSessionManager.maxSwapDelaysUpperBound(50_000);
    SortingSessionManager.DelayStridePlan strided =
        SortingSessionManager.planDelayStride(bubbleRaw, 2f);
    assertTrue(strided.stride() > 1);
    assertEquals(1, strided.maxStepsPerFrame());
    assertTrue(strided.visualSteps() <= 120 + 1, () -> "visual=" + strided.visualSteps());
    assertTrue(strided.visualSteps() >= 120 - 1, () -> "visual=" + strided.visualSteps());
  }

  @Test
  @DisplayName("dry-run timeout still arms equalize with estimated stride")
  void dryRunTimeoutArmsWithEstimate() throws Exception {
    sessionManager.setEqualizeSupport(() -> true, () -> 2.0, () -> NO_OP);
    TimeoutAfterPartialStub algorithm = new TimeoutAfterPartialStub(array, 50, 2100);
    assertTrue(sessionManager.tryArmEqualizePacing(algorithm, NO_OP));

    assertTrue(algorithm.getDelayStride() >= 1);
    assertTrue(stateManager.equalizePacing().isActive());
    assertTrue(stateManager.equalizePacing().totalSteps() > 0);
    assertFalse(stateManager.isEqualizePreparing());
    assertFalse(stateManager.isFrameGateSuspended());
  }

  @Test
  @DisplayName("estimateRawSteps distinguishes sparse vs swap-dense timeouts")
  void estimateRawStepsExtrapolatesAndClamps() {
    assertEquals(100, SortingSessionManager.estimateRawSteps(false, 100, 0.5, 40));
    // Completed counts are not clamped to the swap upper bound.
    assertEquals(10_000, SortingSessionManager.estimateRawSteps(false, 10_000, 0, 40));

    // Swap-dense timeout (partial ≥ n): quadratic upper bound.
    assertEquals(
        SortingSessionManager.maxSwapDelaysUpperBound(40),
        SortingSessionManager.estimateRawSteps(true, 40, 0.01, 40));
    assertEquals(
        SortingSessionManager.maxSwapDelaysUpperBound(50),
        SortingSessionManager.estimateRawSteps(true, 0, 0, 50));

    // Sparse timeout (Selection-like, partial < n): extrapolate toward ~n, not n²/2.
    long sparse = SortingSessionManager.estimateRawSteps(true, 5, 5.0 / 50_000, 50_000);
    assertEquals(50_000, sparse);
    assertTrue(sparse < SortingSessionManager.maxSwapDelaysUpperBound(50_000) / 100);
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
    boolean[] sawPreparing = {false};
    Thread sampler =
        new Thread(
            () -> {
              long deadline = System.nanoTime() + 2_000_000_000L;
              while (System.nanoTime() < deadline) {
                if (stateManager.isFrameGateSuspended()) {
                  sawSuspended[0] = true;
                }
                if (stateManager.isEqualizePreparing()) {
                  sawPreparing[0] = true;
                }
                if (sawSuspended[0] && sawPreparing[0]) {
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
    assertTrue(sawPreparing[0], "dry-run should set equalizePreparing while counting");
    assertFalse(stateManager.isFrameGateSuspended());
    assertFalse(stateManager.isEqualizePreparing());
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

  /**
   * Fires {@code partialSteps} delays, sleeps past the equalize dry-run deadline, then attempts
   * more delays so {@link CountingDelayContext} times out with a non-zero partial count.
   */
  private static final class TimeoutAfterPartialStub extends SortingAlgorithm {
    private final int partialSteps;
    private final long sleepMs;

    TimeoutAfterPartialStub(ArrayModel model, int partialSteps, long sleepMs) {
      super(model, NO_OP);
      this.partialSteps = partialSteps;
      this.sleepMs = sleepMs;
      this.name = "TimeoutAfterPartialStub";
    }

    @Override
    public void sort() {
      for (int i = 0; i < partialSteps && !isCancelled(); i++) {
        delay();
      }
      try {
        Thread.sleep(sleepMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      for (int i = 0; i < partialSteps && !isCancelled(); i++) {
        delay();
      }
    }
  }
}
