package io.github._66_m.control.model;

import io.github._66_m.control.config.AppConfig;
import io.github._66_m.control.render.DelayContext;
import io.github._66_m.control.render.TrackingDelayContext;
import io.github._66_m.sortingalgorithms.GravitySort;
import io.github._66_m.sortingalgorithms.SortingAlgorithm;
import io.github._66_m.sound.Sound;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs a sorting session on a background thread: shuffles, paces and executes each algorithm in
 * turn and records a {@link RunResult} for every one that finishes. Equalize-sort-duration planning
 * lives in {@link EqualizePlanner}.
 */
public class SortingSessionManager {
  private static final Logger LOGGER = Logger.getLogger(SortingSessionManager.class.getName());

  private final SessionContext ctx;
  private final EqualizePlanner equalize;
  private final RunResultsRecorder results = new RunResultsRecorder();
  private final int delayBetweenMs;
  private final int delayAfterMs;

  private Thread executionThread;

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
    this.ctx = new SessionContext(arrayController, sound, stateManager);
    this.equalize = new EqualizePlanner(ctx);
    this.delayBetweenMs = delayBetweenMs;
    this.delayAfterMs = delayAfterMs;
  }

  /**
   * Wires the pacing gate so session end can unblock the render thread's {@link
   * FrameGate#awaitIdle()}.
   */
  public void setFrameGate(FrameGate frameGate) {
    ctx.setFrameGate(frameGate);
  }

  /**
   * Optional equalize-sort-duration support. {@code productionDelay} supplies the live FrameGate
   * context (may be null in unit tests).
   */
  public void setEqualizeSupport(
      BooleanSupplier enabled,
      DoubleSupplier targetDurationSec,
      Supplier<DelayContext> productionDelay) {
    equalize.configure(enabled, targetDurationSec, productionDelay);
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

    clearMeasurements();

    ctx.state.setContinueExecution(true);

    OperationReporter reporter = ctx.state::setCurrentOperation;
    ctx.array.setOperationReporter(reporter);

    for (SortingAlgorithm algorithm : algorithms) {
      algorithm.setOperationReporter(reporter);
    }

    executionThread = new Thread(() -> executeSortingAlgorithms(algorithms));
    executionThread.setName("SortingThread");
    executionThread.start();
  }

  /** Cancels the active session token and stops further algorithm execution. */
  public void cancel() {
    ctx.token().cancel();
    ctx.state.setContinueExecution(false);
  }

  /**
   * Aborts the current algorithm so the session can continue with the next one. Does not end the
   * session; call {@link #cancel()} to stop entirely.
   */
  public void skipCurrent() {
    ctx.token().cancel();
    FrameGate gate = ctx.frameGate();
    if (gate != null) {
      gate.cancel();
    }
  }

  CancellationToken getCancellationToken() {
    return ctx.token();
  }

  /** See {@link EqualizePlanner#tryArm}. */
  boolean tryArmEqualizePacing(SortingAlgorithm algorithm, DelayContext production) {
    return equalize.tryArm(algorithm, production);
  }

  /** Executes all algorithms in sequence, collecting measurements. */
  private void executeSortingAlgorithms(List<SortingAlgorithm> algorithms) {
    SortingStateManager state = ctx.state;
    try {
      long sessionStartNanos = System.nanoTime();

      for (SortingAlgorithm algorithm : algorithms) {
        if (!state.shouldContinueExecution()) {
          LOGGER.log(Level.INFO, "Sorting session cancelled by user");
          break;
        }

        // Chapter timestamps mark where each algorithm's segment begins (its shuffle), not where it
        // ends; the end of one run is the start of the next.
        int startSeconds = (int) ((System.nanoTime() - sessionStartNanos) / 1_000_000_000L);
        armAlgorithmToken(algorithm);
        prepareForAlgorithm(algorithm);

        if (!state.shouldContinueExecution()) {
          LOGGER.log(Level.INFO, "Sorting session cancelled by user");
          break;
        }
        if (ctx.token().isCancelled()) {
          LOGGER.log(Level.INFO, "Skipped algorithm during prepare: {0}", algorithm.getName());
          continue;
        }

        executeAlgorithm(algorithm);

        if (!state.shouldContinueExecution()) {
          LOGGER.log(Level.INFO, "Sorting session cancelled by user");
          break;
        }
        if (ctx.token().isCancelled()) {
          LOGGER.log(Level.INFO, "Skipped algorithm: {0}", algorithm.getName());
          continue;
        }

        results.record(algorithm, ctx.array, startSeconds);
        pauseAfterAlgorithm();
      }

      if (state.shouldContinueExecution()
          && state.shouldShowComparisonTable()
          && !results.isEmpty()) {
        state.setShowResults(true);
      }

      state.setRestart(true);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error during sorting session execution", e);
      state.setRestart(true);
    } finally {
      state.setFrameGateSuspended(false);
      state.equalizePacing().clear();
      // Drop unused step credits so the render thread cannot deadlock in awaitIdle().
      ctx.drainGate();
      state.setRunning(false);
    }
  }

  /** Fresh per-algorithm cancel token and FrameGate so a prior skip does not abort the next. */
  private void armAlgorithmToken(SortingAlgorithm algorithm) {
    CancellationToken token = new CancellationToken();
    ctx.setToken(token);
    ctx.array.setCancellationToken(token);
    algorithm.setCancellationToken(token);
    FrameGate gate = ctx.frameGate();
    if (gate != null) {
      gate.reset();
    }
  }

  private void prepareForAlgorithm(SortingAlgorithm algorithm) {
    ctx.sound.cutNotes();
    ctx.state.equalizePacing().clear();
    equalize.resetFastForward();

    // After a skip (or any incomplete sort) the working array is mid-permutation; always start the
    // next shuffle from the identity so the animation begins from a fully sorted bar chart.
    ctx.array.resetArray();

    if (equalize.shouldOverlapPrepare(algorithm)) {
      equalize.prepareOverlapped(algorithm);
    } else {
      ctx.state.setShuffling(true);
      try {
        ctx.array.shuffle();
      } finally {
        ctx.state.setShuffling(false);
        // Drop unused shuffle credits so awaitIdle cannot stall during the inter-phase sleep.
        ctx.drainGate();
      }
    }

    if (ctx.shouldStop()) {
      return;
    }

    sleepWithoutStepCredits(delayBetweenMs, "Thread interrupted during delay");
    ctx.array.resetMeasurements();
  }

  private void executeAlgorithm(SortingAlgorithm algorithm) {
    DelayContext fallback = equalize.productionDelayOrNoOp();
    EqualizePacing pacing = ctx.state.equalizePacing();
    try {
      if (!equalize.isFastForward() && pacing.isActive()) {
        // Armed during overlapped prepare; keep delayStride already set on the algorithm.
        algorithm.setDelayContext(new TrackingDelayContext(fallback, pacing));
      } else if (!equalize.isFastForward() && equalize.tryArm(algorithm, fallback)) {
        algorithm.setDelayContext(new TrackingDelayContext(fallback, pacing));
      } else if (!equalize.isFastForward()) {
        if (algorithm instanceof GravitySort gravity) {
          gravity.setColumnStride(1);
        }
        algorithm.setDelayStride(1);
        algorithm.setDelayContext(fallback);
      }

      if (equalize.isFastForward()) {
        // Work exceeds what responsive FrameGate pacing can finish near the target — run unbound.
        // Keep delay=true so marker side-effects (and thus audio) still run; use a no-op
        // DelayContext so nothing waits on the FrameGate.
        algorithm.setDelay(true);
        algorithm.setDelayStride(1);
        algorithm.setDelayContext(EqualizePlanner.NO_OP_DELAY);
        ctx.state.setFrameGateSuspended(true);
        ctx.sound.cutNotes();
        ctx.drainGate();
      }

      algorithm.beginTiming();
      try {
        algorithm.sort();
      } finally {
        algorithm.endTiming();
      }
    } finally {
      equalize.resetFastForward();
      ctx.state.setFrameGateSuspended(false);
      pacing.clear();
      if (algorithm instanceof GravitySort gravity) {
        gravity.setColumnStride(1);
      }
      algorithm.setDelay(true);
      algorithm.setDelayStride(1);
      algorithm.setDelayContext(fallback);
      // Drop unused equalize credits so awaitIdle cannot stall after a short/strided run.
      ctx.drainGate();
    }
  }

  private void pauseAfterAlgorithm() {
    sleepWithoutStepCredits(delayAfterMs, "Thread interrupted during result pause");
    ctx.array.resetMeasurements();
  }

  /**
   * Mutes and sleeps while telling the render thread to draw without FrameGate pacing. Draining
   * alone is not enough: during the sleep {@code isRunning} stays true, so the next frame would
   * grant fresh credits that nobody consumes until the sleep ends, freezing the view for the whole
   * pause.
   */
  private void sleepWithoutStepCredits(int delayMs, String interruptLog) {
    ctx.state.setFrameGateSuspended(true);
    try {
      ctx.drainGate();
      ctx.sound.withMuted(
          () -> {
            try {
              Thread.sleep(delayMs);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              LOGGER.log(Level.WARNING, interruptLog, e);
            }
          });
    } finally {
      ctx.state.setFrameGateSuspended(false);
    }
  }

  /** Clears all measurement data for a fresh session. */
  public void clearMeasurements() {
    results.clear();
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

  /** Results of the algorithms that finished (not skipped or cancelled), in run order. */
  public List<RunResult> getResults() {
    return results.results();
  }

  public boolean hasResults() {
    return !results.isEmpty();
  }

  /** Prints when each finished algorithm completed to standard output. */
  public void printTimestampsToConsole() {
    printTimestamps(System.out);
  }

  public void printTimestamps(PrintStream out) {
    ResultsExport.printTimestamps(out, getResults());
  }

  /** Writes the session results as CSV (columns aligned with the on-screen results table). */
  public void exportCsv(Path path) throws IOException {
    ResultsExport.writeCsv(path, getResults());
  }
}
