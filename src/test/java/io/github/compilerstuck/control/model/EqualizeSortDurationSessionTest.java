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
import io.github.compilerstuck.sortingalgorithms.MergeSort;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import io.github.compilerstuck.sound.Sound;
import java.util.Arrays;
import java.util.List;
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

    // 2s → 120 frames; 1.25e9 Bubble steps exceed responsive work budget → fast-forward.
    long bubbleRaw = SortingSessionManager.maxSwapDelaysUpperBound(50_000);
    SortingSessionManager.DelayStridePlan strided =
        SortingSessionManager.planDelayStride(bubbleRaw, 2f);
    assertTrue(strided.fastForward());

    // Fits in work budget with capped stride + catch-up credits.
    long mediumRaw = SortingSessionManager.maxSwapDelaysUpperBound(5_000);
    SortingSessionManager.DelayStridePlan medium =
        SortingSessionManager.planDelayStride(mediumRaw, 2f);
    assertFalse(medium.fastForward());
    assertTrue(medium.stride() > 1);
    assertTrue(medium.stride() <= AppConfig.EQUALIZE_MAX_DELAY_STRIDE);
    assertTrue(medium.maxStepsPerFrame() >= 1);
  }

  @Test
  @DisplayName("dry-run timeout still arms equalize with estimated stride")
  void dryRunTimeoutArmsWithEstimate() throws Exception {
    sessionManager.setEqualizeSupport(() -> true, () -> 2.0, () -> NO_OP);
    TimeoutAfterPartialStub algorithm =
        new TimeoutAfterPartialStub(array, 50, AppConfig.EQUALIZE_DRY_RUN_TIMEOUT_MS + 100);
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

    // Timed-out mid Merge-like sample with ≥ n delays uses Bubble bound (exact Merge counts
    // come from a completed dry-run with timedOut=false instead).
    assertEquals(
        SortingSessionManager.maxSwapDelaysUpperBound(50_000),
        SortingSessionManager.estimateRawSteps(true, 50_000, 0.10, 50_000));
  }

  @Test
  @DisplayName("Merge Sort dry-run completes with exact step count (not Bubble n² bound)")
  void mergeDryRunCompletesWithExactCount() {
    reverseInPlace(array);
    MergeSort merge = new MergeSort(array);
    assertTrue(sessionManager.tryArmEqualizePacing(merge, NO_OP));

    EqualizePacing pacing = stateManager.equalizePacing();
    assertTrue(pacing.isActive());
    // n=32 Merge is well under the dry-run timeout; exact count ≪ n²/2.
    assertTrue(pacing.totalSteps() < SortingSessionManager.maxSwapDelaysUpperBound(32));
    assertEquals(1, merge.getDelayStride());
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
    // Deterministic stand-ins for ALMOST_SORTED / REVERSE: Math.random() almost-sorted swaps
    // make a fixed 5× ratio flaky, while reverse is always n(n-1)/2 Bubble swaps.
    ArrayController almost = new ArrayController(n);
    int[] a = almost.getArray();
    int nearSortedSwaps = Math.max(1, n / 10);
    for (int i = 0; i < nearSortedSwaps; i++) {
      int idx = 5 + i * 8;
      int tmp = a[idx];
      a[idx] = a[idx + 1];
      a[idx + 1] = tmp;
    }

    ArrayController reverse = new ArrayController(n);
    reverseInPlace(reverse);

    long almostSteps = countSteps(new BubbleSort(almost), almost);
    long reverseSteps = countSteps(new BubbleSort(reverse), reverse);

    assertEquals(n * (n - 1L) / 2, reverseSteps);
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
  @DisplayName("peer clone dry-run arms pacing without Prepare UI")
  void cloneDryRunDoesNotSetPreparing() {
    reverseInPlace(array);
    int[] before = Arrays.copyOf(array.getArray(), array.getLength());
    BubbleSort bubble = new BubbleSort(array);

    boolean[] sawPreparing = {false};
    Thread sampler =
        new Thread(
            () -> {
              long deadline = System.nanoTime() + 2_000_000_000L;
              while (System.nanoTime() < deadline) {
                if (stateManager.isEqualizePreparing()) {
                  sawPreparing[0] = true;
                  return;
                }
                Thread.onSpinWait();
              }
            },
            "prepare-sampler");
    sampler.start();
    assertTrue(sessionManager.tryArmEqualizePacing(bubble, NO_OP));
    try {
      sampler.join(3000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    assertFalse(sawPreparing[0], "clone dry-run must not set equalizePreparing");
    assertArrayEquals(before, array.getArray());
    assertTrue(stateManager.equalizePacing().isActive());
    assertTrue(stateManager.equalizePacing().totalSteps() > 0);
    assertFalse(stateManager.isEqualizePreparing());
  }

  @Test
  @DisplayName("overlapped prepare hides Prepare and arms pacing under shuffle cover")
  void overlappedPrepareHidesPrepareUi() throws Exception {
    array.setShuffleType(ShuffleType.REVERSE);
    array.setDelayContext(NO_OP);
    array.setOperationReporter(OperationReporter.NOOP);

    BubbleSort bubble = new BubbleSort(array);
    boolean[] sawPreparing = {false};
    Thread sampler =
        new Thread(
            () -> {
              long deadline = System.nanoTime() + 5_000_000_000L;
              while (System.nanoTime() < deadline) {
                if (stateManager.isEqualizePreparing()) {
                  sawPreparing[0] = true;
                  return;
                }
                if (stateManager.shouldRestart()) {
                  return;
                }
                Thread.onSpinWait();
              }
            },
            "overlap-prepare-sampler");
    sampler.start();

    stateManager.setRunning(true);
    sessionManager.startSortingSession(List.of(bubble));
    sessionManager.waitForCompletion();
    sampler.join(1000);

    assertFalse(sawPreparing[0], "Prepare UI must stay hidden during overlapped prepare");
    assertTrue(sessionManager.hasResults());
    assertTrue(array.isSorted());
    assertFalse(stateManager.isEqualizePreparing());
    assertFalse(stateManager.isShuffling());
  }

  @Test
  @DisplayName("cancel during overlapped prepare aborts without leaving Prepare stuck")
  void cancelDuringOverlappedPrepare() throws Exception {
    array.setShuffleType(ShuffleType.SORTED);
    array.setDelayContext(NO_OP);
    sessionManager.setEqualizeSupport(() -> true, () -> 10.0, () -> NO_OP);

    SlowPeerSort slow = new SlowPeerSort(array);
    stateManager.setRunning(true);
    sessionManager.startSortingSession(List.of(slow));

    long deadline = System.nanoTime() + 2_000_000_000L;
    while (!stateManager.isShuffling() && System.nanoTime() < deadline) {
      Thread.onSpinWait();
    }
    sessionManager.cancel();
    sessionManager.waitForCompletion();

    assertFalse(stateManager.isEqualizePreparing());
    assertFalse(stateManager.isShuffling());
    assertFalse(stateManager.isRunning());
    assertFalse(sessionManager.hasResults());
  }

  @Test
  @DisplayName("createPeerAlgorithm copies alternativeSize when present")
  void createPeerCopiesAlternativeSize() {
    BubbleSort bubble = new BubbleSort(array);
    bubble.setAlternativeSize(99);
    SortingAlgorithm peer = SortingSessionManager.createPeerAlgorithm(bubble, array);
    assertTrue(peer instanceof BubbleSort);
    assertEquals(99, peer.getAlternativeSize());
  }

  @Test
  @DisplayName("createPeerAlgorithm returns null for stubs without ArrayModel ctor")
  void createPeerReturnsNullForStub() {
    CountingSortStub stub = new CountingSortStub(array, 3);
    assertEquals(null, SortingSessionManager.createPeerAlgorithm(stub, array));
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
    assertTrue(sawPreparing[0], "live fallback should set equalizePreparing while counting");
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

  /** Peer-constructible algorithm that sleeps so cancel can hit mid dry-run / shuffle cover. */
  private static final class SlowPeerSort extends SortingAlgorithm {
    SlowPeerSort(ArrayModel model) {
      super(model, NO_OP);
      this.name = "SlowPeerSort";
    }

    @Override
    public void sort() {
      try {
        Thread.sleep(1_500);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      for (int i = 0; i < 5 && !isCancelled(); i++) {
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
