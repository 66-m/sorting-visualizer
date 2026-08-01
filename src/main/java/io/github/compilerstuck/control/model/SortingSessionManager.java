package io.github.compilerstuck.control.model;

import io.github.compilerstuck.control.config.AppConfig;
import io.github.compilerstuck.control.render.CountingDelayContext;
import io.github.compilerstuck.control.render.DelayContext;
import io.github.compilerstuck.control.render.PrepareProgressDelayContext;
import io.github.compilerstuck.control.render.TrackingDelayContext;
import io.github.compilerstuck.control.shuffle.RecordedShuffle;
import io.github.compilerstuck.control.ui.TimeEstimateFormat;
import io.github.compilerstuck.sortingalgorithms.BogoSort;
import io.github.compilerstuck.sortingalgorithms.GravitySort;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import io.github.compilerstuck.sound.Sound;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the sorting session orchestration and thread coordination. Separated from UI concerns to
 * handle algorithm execution, measurements collection, and timing independently.
 */
public class SortingSessionManager {
  private static final Logger LOGGER = Logger.getLogger(SortingSessionManager.class.getName());

  private final ArrayController arrayController;
  private final Sound sound;
  private final SortingStateManager stateManager;
  private final int delayBetweenMs;
  private final int delayAfterMs;

  private final List<String> comparisons = new ArrayList<>();
  private final List<String> realTime = new ArrayList<>();
  private final List<String> swaps = new ArrayList<>();
  private final List<String> writesMain = new ArrayList<>();
  private final List<String> writesAux = new ArrayList<>();
  private final List<Integer> timestamps = new ArrayList<>();
  private final List<SortingAlgorithm> completedAlgorithms = new ArrayList<>();

  private Thread executionThread;
  private volatile CancellationToken cancellationToken = CancellationToken.alwaysActive();
  private volatile FrameGate frameGate;

  private BooleanSupplier equalizeEnabled = () -> false;
  private DoubleSupplier equalizeTargetSec = () -> 10.0;
  private Supplier<DelayContext> productionDelayContext = () -> null;

  /**
   * When equalize cannot hit the slider target without multi-million undelayed batches, the visual
   * pass runs with {@code delay=false} under a suspended FrameGate (full-speed CPU, live
   * publishes).
   */
  private boolean equalizeFastForward;

  public SortingSessionManager(
      ArrayController arrayController, Sound sound, SortingStateManager stateManager) {
    this(
        arrayController,
        sound,
        stateManager,
        AppConfig.DELAY_BETWEEN_ALGORITHMS,
        AppConfig.DELAY_AFTER_SORT_RESULT);
  }

  public SortingSessionManager(
      ArrayController arrayController,
      Sound sound,
      SortingStateManager stateManager,
      int delayBetweenMs,
      int delayAfterMs) {
    this.arrayController = arrayController;
    this.sound = sound;
    this.stateManager = stateManager;
    this.delayBetweenMs = delayBetweenMs;
    this.delayAfterMs = delayAfterMs;
  }

  /**
   * Wires the pacing gate so session end can unblock the render thread's {@link
   * FrameGate#awaitIdle()}.
   */
  public void setFrameGate(FrameGate frameGate) {
    this.frameGate = frameGate;
  }

  /**
   * Optional equalize-sort-duration support. {@code productionDelay} supplies the live FrameGate
   * context (may be null in unit tests).
   */
  public void setEqualizeSupport(
      BooleanSupplier enabled,
      DoubleSupplier targetDurationSec,
      Supplier<DelayContext> productionDelay) {
    this.equalizeEnabled = enabled != null ? enabled : () -> false;
    this.equalizeTargetSec = targetDurationSec != null ? targetDurationSec : () -> 10.0;
    this.productionDelayContext = productionDelay != null ? productionDelay : () -> null;
  }

  /**
   * Starts the sorting algorithm execution in a background thread.
   *
   * @param algorithms the list of algorithms to execute
   */
  public void startSortingSession(List<SortingAlgorithm> algorithms) {
    if (algorithms == null || algorithms.isEmpty()) {
      LOGGER.log(Level.WARNING, "Attempted to start sorting session with empty algorithm list");
      return;
    }

    // Clear previous results
    clearMeasurements();

    stateManager.setContinueExecution(true);

    OperationReporter reporter = stateManager::setCurrentOperation;
    arrayController.setOperationReporter(reporter);

    for (SortingAlgorithm algorithm : algorithms) {
      algorithm.setOperationReporter(reporter);
    }

    executionThread = new Thread(() -> executeSortingAlgorithms(algorithms));
    executionThread.setName("SortingThread");
    executionThread.start();
  }

  /** Cancels the active session token and stops further algorithm execution. */
  public void cancel() {
    cancellationToken.cancel();
    stateManager.setContinueExecution(false);
  }

  /**
   * Aborts the current algorithm so the session can continue with the next one. Does not end the
   * session; call {@link #cancel()} to stop entirely.
   */
  public void skipCurrent() {
    cancellationToken.cancel();
    FrameGate gate = frameGate;
    if (gate != null) {
      gate.cancel();
    }
  }

  CancellationToken getCancellationToken() {
    return cancellationToken;
  }

  /** Executes all algorithms in sequence, collecting measurements. */
  private void executeSortingAlgorithms(List<SortingAlgorithm> algorithms) {
    try {
      int startTime = (int) (System.currentTimeMillis() / 1000L);

      for (SortingAlgorithm algorithm : algorithms) {
        if (!stateManager.shouldContinueExecution()) {
          LOGGER.log(Level.INFO, "Sorting session cancelled by user");
          break;
        }

        armAlgorithmToken(algorithm);
        prepareForAlgorithm(algorithm);

        if (!stateManager.shouldContinueExecution()) {
          LOGGER.log(Level.INFO, "Sorting session cancelled by user");
          break;
        }
        if (cancellationToken.isCancelled()) {
          LOGGER.log(Level.INFO, "Skipped algorithm during prepare: {0}", algorithm.getName());
          continue;
        }

        executeAlgorithm(algorithm);

        if (!stateManager.shouldContinueExecution()) {
          LOGGER.log(Level.INFO, "Sorting session cancelled by user");
          break;
        }
        if (cancellationToken.isCancelled()) {
          LOGGER.log(Level.INFO, "Skipped algorithm: {0}", algorithm.getName());
          continue;
        }

        recordTimestamp(startTime);
        recordMeasurements();
        completedAlgorithms.add(algorithm);
        pauseAfterAlgorithm();
      }

      if (stateManager.shouldContinueExecution()
          && stateManager.shouldShowComparisonTable()
          && !completedAlgorithms.isEmpty()) {
        stateManager.setShowResults(true);
      }

      stateManager.setRestart(true);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error during sorting session execution", e);
      stateManager.setRestart(true);
    } finally {
      stateManager.setFrameGateSuspended(false);
      stateManager.equalizePacing().clear();
      // Drop unused step credits so the render thread cannot deadlock in awaitIdle().
      FrameGate gate = frameGate;
      if (gate != null) {
        gate.drain();
      }
      stateManager.setRunning(false);
    }
  }

  /** Fresh per-algorithm cancel token and FrameGate so a prior skip does not abort the next. */
  private void armAlgorithmToken(SortingAlgorithm algorithm) {
    CancellationToken token = new CancellationToken();
    this.cancellationToken = token;
    arrayController.setCancellationToken(token);
    algorithm.setCancellationToken(token);
    FrameGate gate = frameGate;
    if (gate != null) {
      gate.reset();
    }
  }

  private void recordTimestamp(int startTime) {
    timestamps.add((int) (System.currentTimeMillis() / 1000L) - startTime);
  }

  private void prepareForAlgorithm(SortingAlgorithm algorithm) {
    sound.cutNotes();
    stateManager.equalizePacing().clear();
    equalizeFastForward = false;

    // After a skip (or any incomplete sort) the working array is mid-permutation; always start the
    // next shuffle from the identity so the animation begins from a fully sorted bar chart.
    arrayController.resetArray();

    if (shouldOverlapEqualizePrepare(algorithm)) {
      prepareWithOverlappedEqualize(algorithm);
      return;
    }

    stateManager.setShuffling(true);
    try {
      arrayController.shuffle();
    } finally {
      stateManager.setShuffling(false);
      // Drop unused shuffle credits so awaitIdle cannot stall during the inter-phase sleep.
      FrameGate gate = frameGate;
      if (gate != null) {
        gate.drain();
      }
    }

    if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
      return;
    }

    sleepWithoutStepCredits(delayBetweenMs, "Thread interrupted during delay");
    arrayController.resetMeasurements();
  }

  /** True when equalize dry-run can run under a shuffle visual cover (not Gravity/Bogo). */
  private boolean shouldOverlapEqualizePrepare(SortingAlgorithm algorithm) {
    return equalizeEnabled.getAsBoolean()
        && algorithm != null
        && !(algorithm instanceof BogoSort)
        && !(algorithm instanceof GravitySort);
  }

  /**
   * Mute-shuffle, dry-run on a clone in parallel, cover with a paced shuffle replay so Prepare..
   * never appears on the happy path.
   */
  private void prepareWithOverlappedEqualize(SortingAlgorithm algorithm) {
    // Mute capture must not run under an unsuspended gate: otherwise the render thread grants
    // sort-speed credits nobody consumes, and replay drains that backlog in an instant.
    stateManager.setFrameGateSuspended(true);
    RecordedShuffle recorded;
    CancellationToken dryToken = new CancellationToken();
    long prepareStartNanos = System.nanoTime();
    CompletableFuture<DryRunOutcome> dryRun;
    try {
      recorded = arrayController.captureMuteShuffle();
      if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
        return;
      }
      dryRun = startBackgroundDryRun(algorithm, recorded.post(), dryToken, prepareStartNanos);
    } finally {
      FrameGate gate = frameGate;
      if (gate != null) {
        gate.drain();
      }
      // Leave suspended only if we're about to enter the paced replay below; cancel exits here.
      if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
        stateManager.setFrameGateSuspended(false);
      }
    }

    if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
      return;
    }

    int[] shuffled = recorded.post();
    stateManager.setShuffling(true);
    stateManager.setFrameGateSuspended(false);
    try {
      arrayController.replayRecordedShuffle(recorded);
      joinDryRunUnderShuffleCover(dryRun, dryToken);
    } finally {
      stateManager.setShuffling(false);
      sound.cutNotes();
      FrameGate gate = frameGate;
      if (gate != null) {
        gate.drain();
      }
    }

    arrayController.restoreContents(shuffled);

    if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
      dryToken.cancel();
      dryRun.cancel(true);
      return;
    }

    DryRunOutcome outcome;
    try {
      outcome = dryRun.get(AppConfig.EQUALIZE_DRY_RUN_TIMEOUT_MS + 500L, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      dryToken.cancel();
      LOGGER.log(Level.WARNING, "Equalize dry-run failed; falling back to live Prepare", e);
      outcome = null;
    }

    float sliderTarget = (float) equalizeTargetSec.getAsDouble();
    if (outcome == null) {
      // Peer construction failed or dry-run crashed: sync live fallback (may show Prepare..).
      DelayContext production = productionDelayContext.get();
      DelayContext fallback =
          production != null
              ? production
              : () -> {
                /* no-op */
              };
      tryArmEqualizePacingLive(algorithm, fallback, sliderTarget);
    } else if (!outcome.aborted()) {
      armFromDryRunOutcome(algorithm, outcome, sliderTarget);
    }

    if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
      return;
    }

    sleepWithoutStepCredits(delayBetweenMs, "Thread interrupted during delay");
    arrayController.resetMeasurements();
  }

  private CompletableFuture<DryRunOutcome> startBackgroundDryRun(
      SortingAlgorithm algorithm,
      int[] shuffled,
      CancellationToken dryToken,
      long prepareStartNanos) {
    ArrayController clone = new ArrayController(shuffled.length);
    clone.restoreContents(shuffled);
    SortingAlgorithm peer = createPeerAlgorithm(algorithm, clone);
    if (peer == null) {
      return CompletableFuture.completedFuture(null);
    }

    CancellationToken sessionToken = cancellationToken;
    long prepareTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(AppConfig.EQUALIZE_DRY_RUN_TIMEOUT_MS);
    CountingDelayContext counter =
        new CountingDelayContext(
            prepareStartNanos + prepareTimeoutNanos, sessionToken::isCancelled, dryToken::cancel);

    return CompletableFuture.supplyAsync(
        () -> {
          peer.setDelayStride(1);
          peer.setDelayContext(counter);
          peer.setOperationReporter(OperationReporter.NOOP);
          peer.setCancellationToken(dryToken);
          try {
            // CountingDelayContext does not play sound; avoid withMuted races with the session
            // thread.
            peer.sort();
          } finally {
            peer.endTiming();
          }
          clone.update();
          return new DryRunOutcome(
              counter.stepCount(),
              counter.frameBeatCount(),
              clone.getSortedPercentage(),
              counter.timedOut(),
              counter.aborted());
        });
  }

  /**
   * After the ~1s shuffle replay, wait for the dry-run if needed — without extending the shuffle
   * animation/UI past {@link AppConfig#SHUFFLE_DURATION_SEC}. Suspends FrameGate and cuts MIDI.
   */
  private void joinDryRunUnderShuffleCover(
      CompletableFuture<DryRunOutcome> dryRun, CancellationToken dryToken) {
    // Suspend first so the next render frame republishes (markers cleared) instead of re-triggering
    // noteOn from the last shuffle snapshot, then cut any note already sounding.
    stateManager.setFrameGateSuspended(true);
    sound.cutNotes();
    if (dryRun.isDone()) {
      stateManager.setFrameGateSuspended(false);
      return;
    }
    // Keep shuffle label at 100% — do not stretch "Shuffling.." with prepare elapsed time.
    stateManager.setCurrentOperation("Shuffling.. 100%");
    try {
      FrameGate gate = frameGate;
      if (gate != null) {
        gate.drain();
      }
      while (!dryRun.isDone()) {
        if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
          dryToken.cancel();
          dryRun.cancel(true);
          break;
        }
        try {
          Thread.sleep(16L);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          dryToken.cancel();
          dryRun.cancel(true);
          break;
        }
      }
    } finally {
      stateManager.setFrameGateSuspended(false);
      sound.cutNotes();
    }
  }

  private void executeAlgorithm(SortingAlgorithm algorithm) {
    DelayContext production = productionDelayContext.get();
    DelayContext fallback =
        production != null
            ? production
            : () -> {
              /* no-op */
            };
    try {
      if (!equalizeFastForward && stateManager.equalizePacing().isActive()) {
        // Armed during overlapped prepare; keep delayStride already set on the algorithm.
        algorithm.setDelayContext(
            new TrackingDelayContext(fallback, stateManager.equalizePacing()));
      } else if (!equalizeFastForward && tryArmEqualizePacing(algorithm, fallback)) {
        algorithm.setDelayContext(
            new TrackingDelayContext(fallback, stateManager.equalizePacing()));
      } else if (!equalizeFastForward) {
        if (algorithm instanceof GravitySort gravity) {
          gravity.setColumnStride(1);
        }
        algorithm.setDelayStride(1);
        algorithm.setDelayContext(fallback);
      }

      if (equalizeFastForward) {
        // Work exceeds what responsive FrameGate pacing can finish near the target — run unbound.
        // Keep delay=true so marker side-effects (and thus audio) still run; use a no-op
        // DelayContext
        // so nothing waits on the FrameGate.
        algorithm.setDelay(true);
        algorithm.setDelayStride(1);
        algorithm.setDelayContext(
            () -> {
              /* no-op */
            });
        stateManager.setFrameGateSuspended(true);
        sound.cutNotes();
        FrameGate gate = frameGate;
        if (gate != null) {
          gate.drain();
        }
        algorithm.beginTiming();
        try {
          algorithm.sort();
        } finally {
          algorithm.endTiming();
        }
        return;
      }

      algorithm.beginTiming();
      try {
        algorithm.sort();
      } finally {
        algorithm.endTiming();
      }
    } finally {
      equalizeFastForward = false;
      stateManager.setFrameGateSuspended(false);
      stateManager.equalizePacing().clear();
      if (algorithm instanceof GravitySort gravity) {
        gravity.setColumnStride(1);
      }
      algorithm.setDelay(true);
      algorithm.setDelayStride(1);
      algorithm.setDelayContext(fallback);
      // Drop unused equalize credits so awaitIdle cannot stall after a short/strided run.
      FrameGate gate = frameGate;
      if (gate != null) {
        gate.drain();
      }
    }
  }

  /**
   * Silent dry-run that counts visual steps, restores the array, and arms {@link EqualizePacing}.
   * When the counted (or estimated) step total exceeds the equalize frame budget, a delay stride is
   * applied so the visual pass can still hit the slider target.
   *
   * <p>Prefer a clone + peer algorithm (no {@code Prepare..} UI). Falls back to a live dry-run with
   * Prepare progress when a peer cannot be constructed (e.g. test stubs).
   *
   * @return true when equalize pacing was armed for the visual pass
   */
  boolean tryArmEqualizePacing(SortingAlgorithm algorithm, DelayContext production) {
    if (!equalizeEnabled.getAsBoolean()) {
      return false;
    }
    if (algorithm instanceof BogoSort) {
      LOGGER.log(Level.INFO, "Equalize skipped for Bogo Sort (unbounded)");
      return false;
    }
    if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
      return false;
    }
    if (stateManager.equalizePacing().isActive()) {
      return true;
    }

    float sliderTarget = (float) equalizeTargetSec.getAsDouble();

    // Gravity's full column walk is O(n·max) CPU — impossible in a few seconds at 100k. Estimate
    // beats, then raise columnStride so only ~budget samples are visualized/computed.
    if (algorithm instanceof GravitySort gravity) {
      int naturalBeats = GravitySort.estimateFrameBeats(arrayController);
      if (naturalBeats <= 0) {
        return false;
      }
      int n = arrayController.getLength();
      int maxPerFrame = AppConfig.equalizeMaxFrameBeatsPerFrame(n);
      int budget =
          Math.max(
              1,
              Math.round(maxPerFrame * AppConfig.TARGET_FRAME_RATE * Math.max(0.1f, sliderTarget)));
      int stride = Math.max(1, (naturalBeats + budget - 1) / budget);
      gravity.setColumnStride(stride);
      int visualBeats = GravitySort.countVisualBeats(naturalBeats, stride);
      LOGGER.log(
          Level.INFO,
          "Gravity equalize stride={0} (naturalBeats={1}, budget={2}, visualBeats={3})",
          new Object[] {stride, naturalBeats, budget, visualBeats});
      return armEqualize(algorithm.getName(), visualBeats, visualBeats, sliderTarget, 0);
    }

    int[] snapshot = Arrays.copyOf(arrayController.getArray(), arrayController.getLength());
    ArrayController clone = new ArrayController(snapshot.length);
    clone.restoreContents(snapshot);
    SortingAlgorithm peer = createPeerAlgorithm(algorithm, clone);
    if (peer != null) {
      DryRunOutcome outcome = runDryRunOn(peer, clone, /* reportPrepare= */ false);
      if (outcome == null || outcome.aborted()) {
        return false;
      }
      if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
        return false;
      }
      return armFromDryRunOutcome(algorithm, outcome, sliderTarget);
    }

    return tryArmEqualizePacingLive(algorithm, production, sliderTarget);
  }

  /**
   * Live-array dry-run with {@code Prepare..} progress. Used when a peer algorithm cannot be
   * constructed.
   */
  private boolean tryArmEqualizePacingLive(
      SortingAlgorithm algorithm, DelayContext production, float sliderTarget) {
    int[] snapshot = Arrays.copyOf(arrayController.getArray(), arrayController.getLength());
    OperationReporter previousReporter = stateManager::setCurrentOperation;
    CancellationToken dryToken = new CancellationToken();
    CancellationToken sessionToken = cancellationToken;
    long prepareStartNanos = System.nanoTime();
    long prepareTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(AppConfig.EQUALIZE_DRY_RUN_TIMEOUT_MS);
    CountingDelayContext counter =
        new CountingDelayContext(
            prepareStartNanos + prepareTimeoutNanos, sessionToken::isCancelled, dryToken::cancel);
    PrepareProgressDelayContext progressCounter =
        new PrepareProgressDelayContext(
            counter,
            prepareStartNanos,
            prepareTimeoutNanos,
            stateManager::setCurrentOperation,
            stateManager::setEqualizePrepareProgress);

    algorithm.setDelayStride(1);
    // Render must not grant FrameGate credits during the dry-run (nothing consumes them).
    stateManager.setFrameGateSuspended(true);
    stateManager.setEqualizePreparing(true);
    FrameGate gate = frameGate;
    if (gate != null) {
      gate.drain();
    }

    algorithm.setDelayContext(progressCounter);
    algorithm.setOperationReporter(OperationReporter.NOOP);
    algorithm.setCancellationToken(dryToken);

    long partialSteps = 0L;
    long partialFrameBeats = 0L;
    double progressSample = 0d;
    boolean timedOut;
    boolean aborted;
    try {
      sound.withMuted(algorithm::sort);
      progressCounter.complete();
    } finally {
      // Sample progress before restore so timeout extrapolation sees dry-run work.
      partialSteps = counter.stepCount();
      partialFrameBeats = counter.frameBeatCount();
      timedOut = counter.timedOut();
      aborted = counter.aborted();
      arrayController.update();
      progressSample = arrayController.getSortedPercentage();

      algorithm.endTiming();
      algorithm.setCancellationToken(sessionToken);
      algorithm.setOperationReporter(previousReporter);
      algorithm.setDelayContext(production);
      arrayController.restoreContents(snapshot);
      arrayController.resetMeasurements();
      if (gate != null) {
        gate.drain();
      }
      stateManager.setEqualizePreparing(false);
      stateManager.setFrameGateSuspended(false);
    }

    if (sessionToken.isCancelled() || !stateManager.shouldContinueExecution()) {
      return false;
    }
    if (aborted) {
      return false;
    }

    return armFromDryRunOutcome(
        algorithm,
        new DryRunOutcome(partialSteps, partialFrameBeats, progressSample, timedOut, false),
        sliderTarget);
  }

  private DryRunOutcome runDryRunOn(
      SortingAlgorithm peer, ArrayController target, boolean reportPrepare) {
    CancellationToken dryToken = new CancellationToken();
    CancellationToken sessionToken = cancellationToken;
    long prepareStartNanos = System.nanoTime();
    long prepareTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(AppConfig.EQUALIZE_DRY_RUN_TIMEOUT_MS);
    CountingDelayContext counter =
        new CountingDelayContext(
            prepareStartNanos + prepareTimeoutNanos, sessionToken::isCancelled, dryToken::cancel);
    DelayContext delays = counter;
    if (reportPrepare) {
      delays =
          new PrepareProgressDelayContext(
              counter,
              prepareStartNanos,
              prepareTimeoutNanos,
              stateManager::setCurrentOperation,
              stateManager::setEqualizePrepareProgress);
    }

    peer.setDelayStride(1);
    stateManager.setFrameGateSuspended(true);
    if (reportPrepare) {
      stateManager.setEqualizePreparing(true);
    }
    FrameGate gate = frameGate;
    if (gate != null) {
      gate.drain();
    }

    peer.setDelayContext(delays);
    peer.setOperationReporter(OperationReporter.NOOP);
    peer.setCancellationToken(dryToken);
    try {
      sound.withMuted(peer::sort);
      if (delays instanceof PrepareProgressDelayContext progress) {
        progress.complete();
      }
    } finally {
      peer.endTiming();
      if (gate != null) {
        gate.drain();
      }
      if (reportPrepare) {
        stateManager.setEqualizePreparing(false);
      }
      stateManager.setFrameGateSuspended(false);
    }

    if (sessionToken.isCancelled() || !stateManager.shouldContinueExecution()) {
      return new DryRunOutcome(0, 0, 0, false, true);
    }
    target.update();
    return new DryRunOutcome(
        counter.stepCount(),
        counter.frameBeatCount(),
        target.getSortedPercentage(),
        counter.timedOut(),
        counter.aborted());
  }

  private boolean armFromDryRunOutcome(
      SortingAlgorithm algorithm, DryRunOutcome outcome, float sliderTarget) {
    int n = arrayController.getLength();
    long rawSteps =
        estimateRawSteps(outcome.timedOut(), outcome.partialSteps(), outcome.progressSample(), n);
    if (rawSteps <= 0) {
      return false;
    }

    DelayStridePlan plan = planDelayStride(rawSteps, sliderTarget);
    if (plan.fastForward()) {
      equalizeFastForward = true;
      algorithm.setDelayStride(1);
      LOGGER.log(
          Level.INFO,
          "Equalize fast-forward for {0}: rawSteps={1} exceeds responsive budget for {2}s target",
          new Object[] {algorithm.getName(), rawSteps, sliderTarget});
      return false;
    }

    int stride = plan.stride();
    int visualSteps = plan.visualSteps();

    long rawFrameBeats = outcome.partialFrameBeats();
    if (outcome.timedOut() && outcome.partialFrameBeats() > 0L && outcome.partialSteps() > 0L) {
      rawFrameBeats =
          Math.max(
              outcome.partialFrameBeats(),
              Math.round(
                  outcome.partialFrameBeats()
                      * ((double) rawSteps / (double) outcome.partialSteps())));
    }
    int visualFrameBeats = 0;
    if (rawFrameBeats > 0L) {
      visualFrameBeats = clampToInt(Math.max(1L, (rawFrameBeats + stride - 1L) / stride));
    }

    algorithm.setDelayStride(stride);
    if (outcome.timedOut()) {
      LOGGER.log(
          Level.INFO,
          "Equalize dry-run timed out for {0}; estimated steps={1}, stride={2}, visualSteps={3}, maxSteps/frame={4}",
          new Object[] {
            algorithm.getName(), rawSteps, stride, visualSteps, plan.maxStepsPerFrame()
          });
    } else if (stride > 1) {
      LOGGER.log(
          Level.INFO,
          "Equalize stride={0} for {1} (rawSteps={2}, frameBudget={3}, visualSteps={4})",
          new Object[] {stride, algorithm.getName(), rawSteps, plan.budget(), visualSteps});
    }

    return armEqualize(
        algorithm.getName(), visualSteps, visualFrameBeats, sliderTarget, plan.maxStepsPerFrame());
  }

  /**
   * Constructs a fresh algorithm instance bound to {@code model} via the standard {@code
   * (ArrayModel)} constructor. Returns null when that ctor is unavailable (test stubs).
   */
  static SortingAlgorithm createPeerAlgorithm(SortingAlgorithm prototype, ArrayModel model) {
    if (prototype == null || model == null) {
      return null;
    }
    try {
      Constructor<? extends SortingAlgorithm> ctor =
          prototype.getClass().getConstructor(ArrayModel.class);
      SortingAlgorithm peer = ctor.newInstance(model);
      if (prototype.getAlternativeSize() != 0) {
        peer.setAlternativeSize(prototype.getAlternativeSize());
      }
      return peer;
    } catch (ReflectiveOperationException e) {
      return null;
    }
  }

  /** Counts collected by a silent equalize dry-run. */
  record DryRunOutcome(
      long partialSteps,
      long partialFrameBeats,
      double progressSample,
      boolean timedOut,
      boolean aborted) {}

  /**
   * Chooses delay stride so equalize can hit {@code sliderTarget}.
   *
   * <ul>
   *   <li>If {@code rawSteps} fits in {@code EQUALIZE_MAX_STEPS_PER_FRAME × 60 × target}, stride is
   *       1 and multi-credit frames are used.
   *   <li>Otherwise stride is {@code ceil(rawSteps / (60 × target))}, capped at {@link
   *       AppConfig#EQUALIZE_MAX_DELAY_STRIDE}. When the cap binds, multiple credits per frame are
   *       allowed (up to the work budget) so the run can still approach the target.
   *   <li>If even {@link AppConfig#EQUALIZE_MAX_WORK_PER_FRAME} × frameBudget cannot cover {@code
   *       rawSteps}, {@link DelayStridePlan#fastForward()} is set — caller should run unbound.
   * </ul>
   */
  static DelayStridePlan planDelayStride(long rawSteps, float sliderTarget) {
    float target = Math.max(0.1f, sliderTarget);
    long frameBudget =
        Math.max(1L, Math.round((double) AppConfig.TARGET_FRAME_RATE * (double) target));
    long highThroughputBudget = (long) AppConfig.EQUALIZE_MAX_STEPS_PER_FRAME * frameBudget;
    if (rawSteps <= highThroughputBudget) {
      return new DelayStridePlan(
          1,
          clampToInt(rawSteps),
          AppConfig.EQUALIZE_MAX_STEPS_PER_FRAME,
          highThroughputBudget,
          false);
    }

    long maxWork = (long) AppConfig.EQUALIZE_MAX_WORK_PER_FRAME * frameBudget;
    if (rawSteps > maxWork) {
      // Cannot finish near the target without multi-million batches per frame.
      return new DelayStridePlan(1, 1, 1, frameBudget, true);
    }

    int idealStride = clampToInt(Math.max(1L, (rawSteps + frameBudget - 1L) / frameBudget));
    int stride = Math.min(idealStride, AppConfig.EQUALIZE_MAX_DELAY_STRIDE);
    int visualSteps = clampToInt(Math.max(1L, (rawSteps + (long) stride - 1L) / stride));
    int maxStepsPerFrame = 1;
    if (stride < idealStride) {
      int needed = clampToInt(Math.max(1L, ((long) visualSteps + frameBudget - 1L) / frameBudget));
      int byWork = Math.max(1, AppConfig.EQUALIZE_MAX_WORK_PER_FRAME / Math.max(1, stride));
      maxStepsPerFrame = Math.min(needed, byWork);
    }
    return new DelayStridePlan(stride, visualSteps, maxStepsPerFrame, frameBudget, false);
  }

  /** Result of {@link #planDelayStride(long, float)}. */
  record DelayStridePlan(
      int stride, int visualSteps, int maxStepsPerFrame, long budget, boolean fastForward) {}

  /**
   * Raw delay count for equalize arming. Completed dry-runs keep the counted total.
   *
   * <p>On timeout:
   *
   * <ul>
   *   <li>Swap-dense algorithms (Bubble/Shaker/Gnome) usually already counted {@code ≥ n} delays —
   *       use the quadratic upper bound (sorted-% stays low while most swaps remain).
   *   <li>Sparse-delay algorithms (Selection/Cycle/…) have {@code < n} delays — extrapolate from
   *       sorted progress (floored at {@code n}) so we do not arm a Bubble-sized stride that skips
   *       almost every real delay and stalls {@code FrameGate.awaitIdle}.
   * </ul>
   *
   * <p>{@code n log n} sorts (Merge/Quick/…) are expected to finish the dry-run inside the timeout
   * with an exact count ({@code timedOut == false}); do not apply the Bubble bound to a partial
   * mid-run sample.
   */
  static long estimateRawSteps(boolean timedOut, long partialSteps, double progressSample, int n) {
    if (!timedOut) {
      return Math.max(0L, partialSteps);
    }
    long upper = maxSwapDelaysUpperBound(n);
    if (partialSteps <= 0L) {
      return upper;
    }
    if (n > 0 && partialSteps >= n) {
      return upper;
    }
    // At least one element of progress so early Selection placements extrapolate toward ~n.
    double progress = Math.max(progressSample, n > 0 ? 1.0d / n : 0.02d);
    long extrapolated = Math.round(partialSteps / progress);
    long atLeastN = Math.max(n, partialSteps);
    return Math.min(Math.max(atLeastN, extrapolated), upper);
  }

  /** Conservative upper bound for delay-per-swap algorithms: {@code n*(n-1)/2}. */
  static long maxSwapDelaysUpperBound(int n) {
    if (n <= 1) {
      return 1L;
    }
    return (long) n * (n - 1L) / 2L;
  }

  private boolean armEqualize(
      String algorithmName,
      int totalSteps,
      int frameBeats,
      float sliderTarget,
      int maxStepsPerFrame) {
    stateManager
        .equalizePacing()
        .begin(totalSteps, frameBeats, sliderTarget, arrayController.getLength(), maxStepsPerFrame);
    LOGGER.log(
        Level.INFO,
        "Equalize armed for {0}: steps={1}, frameBeats={2}, targetSec={3}, batch={4}, maxSteps/frame={5}",
        new Object[] {
          algorithmName,
          totalSteps,
          frameBeats,
          sliderTarget,
          stateManager.equalizePacing().batchBeats(),
          stateManager.equalizePacing().maxStepsPerFrame()
        });
    return true;
  }

  private static int clampToInt(long value) {
    if (value > Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    if (value < 0) {
      return 0;
    }
    return (int) value;
  }

  private void recordMeasurements() {
    comparisons.add(Long.toString(arrayController.getComparisons()));
    realTime.add(Double.toString(arrayController.getRealTime()));
    swaps.add(Long.toString(arrayController.getSwaps()));
    writesMain.add(Long.toString(arrayController.getWrites()));
    writesAux.add(Long.toString(arrayController.getWritesAux()));
  }

  private void pauseAfterAlgorithm() {
    sleepWithoutStepCredits(delayAfterMs, "Thread interrupted during result pause");
    arrayController.resetMeasurements();
  }

  /**
   * Mutes and sleeps while telling the render thread to draw without FrameGate pacing. Draining
   * alone is not enough: during the sleep {@code isRunning} stays true, so the next frame would
   * grant fresh credits that nobody consumes until the sleep ends, freezing the view for the whole
   * pause.
   */
  private void sleepWithoutStepCredits(int delayMs, String interruptLog) {
    stateManager.setFrameGateSuspended(true);
    try {
      FrameGate gate = frameGate;
      if (gate != null) {
        gate.drain();
      }
      sound.withMuted(
          () -> {
            try {
              Thread.sleep(delayMs);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              LOGGER.log(Level.WARNING, interruptLog, e);
            }
          });
    } finally {
      stateManager.setFrameGateSuspended(false);
    }
  }

  /** Clears all measurement data for a fresh session. */
  public void clearMeasurements() {
    comparisons.clear();
    realTime.clear();
    swaps.clear();
    writesMain.clear();
    writesAux.clear();
    timestamps.clear();
    completedAlgorithms.clear();
  }

  /** Waits for the current sorting session to complete. */
  public void waitForCompletion() {
    if (executionThread != null && executionThread.isAlive()) {
      try {
        executionThread.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        LOGGER.log(Level.WARNING, "Interrupted while waiting for sorting thread", e);
      }
    }
  }

  // Getters for measurements
  public List<String> getComparisons() {
    return new ArrayList<>(comparisons);
  }

  public List<String> getRealTime() {
    return new ArrayList<>(realTime);
  }

  public List<String> getSwaps() {
    return new ArrayList<>(swaps);
  }

  public List<String> getWritesMain() {
    return new ArrayList<>(writesMain);
  }

  public List<String> getWritesAux() {
    return new ArrayList<>(writesAux);
  }

  /** Algorithms that finished (not skipped or cancelled) in this session, in run order. */
  public List<SortingAlgorithm> getCompletedAlgorithms() {
    return new ArrayList<>(completedAlgorithms);
  }

  /** Logs timing information to console after sorting completes. */
  public void printTimestampsToConsole(List<SortingAlgorithm> algorithms) {
    System.out.println("\n\nTimestamps:\n");
    for (int i = 0; i < algorithms.size() && i < timestamps.size(); i++) {
      int seconds = timestamps.get(i);
      int minutes = seconds / 60;
      int secs = seconds % 60;
      String time = String.format("%02d:%02d", minutes, secs);
      System.out.println(time + " " + algorithms.get(i).getName());
    }
  }

  public boolean hasResults() {
    return !comparisons.isEmpty();
  }

  /** Writes comparison metrics as CSV (columns aligned with the on-screen results table). */
  public void exportCsv(Path path, List<SortingAlgorithm> algorithms) throws IOException {
    try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      writer.write("algorithm,elements,comparisons,est_time_ms,swaps,writes_main,writes_aux\n");
      int rows = algorithms == null ? 0 : algorithms.size();
      for (int i = 0; i < rows; i++) {
        SortingAlgorithm alg = algorithms.get(i);
        writer.write(csvEscape(alg.getName()));
        writer.write(',');
        writer.write(Integer.toString(alg.getAlternativeSize()));
        writer.write(',');
        writer.write(i < comparisons.size() ? comparisons.get(i) : "");
        writer.write(',');
        if (i < realTime.size()) {
          double raw = Double.parseDouble(realTime.get(i));
          writer.write(TimeEstimateFormat.format(raw));
        }
        writer.write(',');
        writer.write(i < swaps.size() ? swaps.get(i) : "");
        writer.write(',');
        writer.write(i < writesMain.size() ? writesMain.get(i) : "");
        writer.write(',');
        writer.write(i < writesAux.size() ? writesAux.get(i) : "");
        writer.write('\n');
      }
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
