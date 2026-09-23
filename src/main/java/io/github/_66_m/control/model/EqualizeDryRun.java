package io.github._66_m.control.model;

import io.github._66_m.control.config.AppConfig;
import io.github._66_m.control.render.CountingDelayContext;
import io.github._66_m.control.render.DelayContext;
import io.github._66_m.control.render.PrepareProgressDelayContext;
import io.github._66_m.sortingalgorithms.SortingAlgorithm;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Silent equalize dry-runs: sort once without visuals, counting delay steps, so the visual pass can
 * be paced to the target duration.
 */
final class EqualizeDryRun {

  /** Counts collected by a silent equalize dry-run. */
  record Outcome(
      long partialSteps,
      long partialFrameBeats,
      double progressSample,
      boolean timedOut,
      boolean aborted) {}

  private final SessionContext ctx;

  EqualizeDryRun(SessionContext ctx) {
    this.ctx = ctx;
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

  /**
   * Starts a dry-run of a peer of {@code algorithm} on a clone of {@code shuffled} in the
   * background. Completes with null when no peer can be constructed.
   */
  CompletableFuture<Outcome> startInBackground(
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

    CancellationToken sessionToken = ctx.token();
    CountingDelayContext counter = newCounter(prepareStartNanos, sessionToken, dryToken);

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
          return new Outcome(
              counter.stepCount(),
              counter.frameBeatCount(),
              clone.getSortedPercentage(),
              counter.timedOut(),
              counter.aborted());
        });
  }

  /**
   * Synchronous dry-run of {@code algorithm}'s peer on a snapshot of the live array.
   *
   * @return the outcome, or null when no peer can be constructed
   */
  Outcome runOnClone(SortingAlgorithm algorithm) {
    int[] snapshot = Arrays.copyOf(ctx.array.getArray(), ctx.array.getLength());
    ArrayController clone = new ArrayController(snapshot.length);
    clone.restoreContents(snapshot);
    SortingAlgorithm peer = createPeerAlgorithm(algorithm, clone);
    if (peer == null) {
      return null;
    }
    return runOn(peer, clone);
  }

  private Outcome runOn(SortingAlgorithm peer, ArrayController target) {
    CancellationToken dryToken = new CancellationToken();
    CancellationToken sessionToken = ctx.token();
    CountingDelayContext counter = newCounter(System.nanoTime(), sessionToken, dryToken);

    peer.setDelayStride(1);
    ctx.state.setFrameGateSuspended(true);
    ctx.drainGate();

    peer.setDelayContext(counter);
    peer.setOperationReporter(OperationReporter.NOOP);
    peer.setCancellationToken(dryToken);
    try {
      ctx.sound.withMuted(peer::sort);
    } finally {
      peer.endTiming();
      ctx.drainGate();
      ctx.state.setFrameGateSuspended(false);
    }

    if (sessionToken.isCancelled() || !ctx.state.shouldContinueExecution()) {
      return new Outcome(0, 0, 0, false, true);
    }
    target.update();
    return new Outcome(
        counter.stepCount(),
        counter.frameBeatCount(),
        target.getSortedPercentage(),
        counter.timedOut(),
        counter.aborted());
  }

  /**
   * Live-array dry-run with {@code Prepare..} progress, used when a peer algorithm cannot be
   * constructed. Restores the array afterwards.
   *
   * @return the outcome, or null when the run was cancelled or aborted
   */
  Outcome runLive(SortingAlgorithm algorithm, DelayContext production) {
    ArrayController array = ctx.array;
    int[] snapshot = Arrays.copyOf(array.getArray(), array.getLength());
    OperationReporter previousReporter = ctx.state::setCurrentOperation;
    CancellationToken dryToken = new CancellationToken();
    CancellationToken sessionToken = ctx.token();
    long prepareStartNanos = System.nanoTime();
    CountingDelayContext counter = newCounter(prepareStartNanos, sessionToken, dryToken);
    PrepareProgressDelayContext progressCounter =
        new PrepareProgressDelayContext(
            counter,
            prepareStartNanos,
            timeoutNanos(),
            ctx.state::setCurrentOperation,
            ctx.state::setEqualizePrepareProgress);

    algorithm.setDelayStride(1);
    // Render must not grant FrameGate credits during the dry-run (nothing consumes them).
    ctx.state.setFrameGateSuspended(true);
    ctx.state.setEqualizePreparing(true);
    ctx.drainGate();

    algorithm.setDelayContext(progressCounter);
    algorithm.setOperationReporter(OperationReporter.NOOP);
    algorithm.setCancellationToken(dryToken);

    long partialSteps;
    long partialFrameBeats;
    double progressSample;
    boolean timedOut;
    boolean aborted;
    try {
      ctx.sound.withMuted(algorithm::sort);
      progressCounter.complete();
    } finally {
      // Sample progress before restore so timeout extrapolation sees dry-run work.
      partialSteps = counter.stepCount();
      partialFrameBeats = counter.frameBeatCount();
      timedOut = counter.timedOut();
      aborted = counter.aborted();
      array.update();
      progressSample = array.getSortedPercentage();

      algorithm.endTiming();
      algorithm.setCancellationToken(sessionToken);
      algorithm.setOperationReporter(previousReporter);
      algorithm.setDelayContext(production);
      array.restoreContents(snapshot);
      array.resetMeasurements();
      ctx.drainGate();
      ctx.state.setEqualizePreparing(false);
      ctx.state.setFrameGateSuspended(false);
    }

    if (sessionToken.isCancelled() || !ctx.state.shouldContinueExecution() || aborted) {
      return null;
    }
    return new Outcome(partialSteps, partialFrameBeats, progressSample, timedOut, false);
  }

  private static CountingDelayContext newCounter(
      long prepareStartNanos, CancellationToken sessionToken, CancellationToken dryToken) {
    return new CountingDelayContext(
        prepareStartNanos + timeoutNanos(), sessionToken::isCancelled, dryToken::cancel);
  }

  private static long timeoutNanos() {
    return TimeUnit.MILLISECONDS.toNanos(AppConfig.EQUALIZE_DRY_RUN_TIMEOUT_MS);
  }
}
