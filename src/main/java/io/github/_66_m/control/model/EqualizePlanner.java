package io.github._66_m.control.model;

import io.github._66_m.control.config.AppConfig;
import io.github._66_m.control.render.DelayContext;
import io.github._66_m.control.shuffle.RecordedShuffle;
import io.github._66_m.sortingalgorithms.BogoSort;
import io.github._66_m.sortingalgorithms.GravitySort;
import io.github._66_m.sortingalgorithms.SortingAlgorithm;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Equalize-sort-duration planning: measures an algorithm with a silent dry-run and arms {@link
 * EqualizePacing} (plus a delay stride) so its visual pass takes roughly the slider's target time.
 */
final class EqualizePlanner {
  private static final Logger LOGGER = Logger.getLogger(EqualizePlanner.class.getName());

  static final DelayContext NO_OP_DELAY =
      () -> {
        /* no-op */
      };

  private final SessionContext ctx;
  private final EqualizeDryRun dryRuns;

  private BooleanSupplier enabled = () -> false;
  private DoubleSupplier targetSec = () -> 10.0;
  private Supplier<DelayContext> productionDelay = () -> null;

  /**
   * When equalize cannot hit the slider target without multi-million undelayed batches, the visual
   * pass runs with {@code delay=false} under a suspended FrameGate (full-speed CPU, live
   * publishes).
   */
  private boolean fastForward;

  EqualizePlanner(SessionContext ctx) {
    this.ctx = ctx;
    this.dryRuns = new EqualizeDryRun(ctx);
  }

  void configure(
      BooleanSupplier enabled,
      DoubleSupplier targetDurationSec,
      Supplier<DelayContext> production) {
    this.enabled = enabled != null ? enabled : () -> false;
    this.targetSec = targetDurationSec != null ? targetDurationSec : () -> 10.0;
    this.productionDelay = production != null ? production : () -> null;
  }

  /** The live FrameGate delay context, or a no-op one when none is wired (unit tests). */
  DelayContext productionDelayOrNoOp() {
    DelayContext production = productionDelay.get();
    return production != null ? production : NO_OP_DELAY;
  }

  boolean isFastForward() {
    return fastForward;
  }

  void resetFastForward() {
    fastForward = false;
  }

  /** True when equalize dry-run can run under a shuffle visual cover (not Gravity/Bogo). */
  boolean shouldOverlapPrepare(SortingAlgorithm algorithm) {
    return enabled.getAsBoolean()
        && algorithm != null
        && !(algorithm instanceof BogoSort)
        && !(algorithm instanceof GravitySort);
  }

  /**
   * Mute-shuffle, dry-run on a clone in parallel, cover with a paced shuffle replay so Prepare..
   * never appears on the happy path. Leaves the array shuffled and pacing armed (when possible).
   */
  void prepareOverlapped(SortingAlgorithm algorithm) {
    // Mute capture must not run under an unsuspended gate: otherwise the render thread grants
    // sort-speed credits nobody consumes, and replay drains that backlog in an instant.
    ctx.state.setFrameGateSuspended(true);
    RecordedShuffle recorded;
    CancellationToken dryToken = new CancellationToken();
    long prepareStartNanos = System.nanoTime();
    CompletableFuture<EqualizeDryRun.Outcome> dryRun;
    try {
      recorded = ctx.array.captureMuteShuffle();
      if (ctx.shouldStop()) {
        return;
      }
      dryRun = dryRuns.startInBackground(algorithm, recorded.post(), dryToken, prepareStartNanos);
    } finally {
      ctx.drainGate();
      // Leave suspended only if we're about to enter the paced replay below; cancel exits here.
      if (ctx.shouldStop()) {
        ctx.state.setFrameGateSuspended(false);
      }
    }

    if (ctx.shouldStop()) {
      return;
    }

    int[] shuffled = recorded.post();
    ctx.state.setShuffling(true);
    ctx.state.setFrameGateSuspended(false);
    try {
      ctx.array.replayRecordedShuffle(recorded);
      joinUnderShuffleCover(dryRun, dryToken);
    } finally {
      ctx.state.setShuffling(false);
      ctx.sound.cutNotes();
      ctx.drainGate();
    }

    ctx.array.restoreContents(shuffled);

    if (ctx.shouldStop()) {
      dryToken.cancel();
      dryRun.cancel(true);
      return;
    }

    EqualizeDryRun.Outcome outcome;
    try {
      outcome = dryRun.get(AppConfig.EQUALIZE_DRY_RUN_TIMEOUT_MS + 500L, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      dryToken.cancel();
      LOGGER.log(Level.WARNING, "Equalize dry-run failed; falling back to live Prepare", e);
      outcome = null;
    }

    float sliderTarget = (float) targetSec.getAsDouble();
    if (outcome == null) {
      // Peer construction failed or dry-run crashed: sync live fallback (may show Prepare..).
      EqualizeDryRun.Outcome live = dryRuns.runLive(algorithm, productionDelayOrNoOp());
      if (live != null) {
        armFromOutcome(algorithm, live, sliderTarget);
      }
    } else if (!outcome.aborted()) {
      armFromOutcome(algorithm, outcome, sliderTarget);
    }
  }

  /**
   * After the ~1s shuffle replay, wait for the dry-run if needed — without extending the shuffle
   * animation/UI past {@link AppConfig#SHUFFLE_DURATION_SEC}. Suspends FrameGate and cuts MIDI.
   */
  private void joinUnderShuffleCover(
      CompletableFuture<EqualizeDryRun.Outcome> dryRun, CancellationToken dryToken) {
    // Suspend first so the next render frame republishes (markers cleared) instead of re-triggering
    // noteOn from the last shuffle snapshot, then cut any note already sounding.
    ctx.state.setFrameGateSuspended(true);
    ctx.sound.cutNotes();
    if (dryRun.isDone()) {
      ctx.state.setFrameGateSuspended(false);
      return;
    }
    // Keep shuffle label at 100% — do not stretch "Shuffling.." with prepare elapsed time.
    ctx.state.setCurrentOperation("Shuffling.. 100%");
    try {
      ctx.drainGate();
      while (!dryRun.isDone()) {
        if (ctx.shouldStop()) {
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
      ctx.state.setFrameGateSuspended(false);
      ctx.sound.cutNotes();
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
  boolean tryArm(SortingAlgorithm algorithm, DelayContext production) {
    if (!enabled.getAsBoolean()) {
      return false;
    }
    if (algorithm instanceof BogoSort) {
      LOGGER.log(Level.INFO, "Equalize skipped for Bogo Sort (unbounded)");
      return false;
    }
    if (ctx.shouldStop()) {
      return false;
    }
    if (ctx.state.equalizePacing().isActive()) {
      return true;
    }

    float sliderTarget = (float) targetSec.getAsDouble();

    // Gravity's full column walk is O(n·max) CPU — impossible in a few seconds at 100k. Estimate
    // beats, then raise columnStride so only ~budget samples are visualized/computed.
    if (algorithm instanceof GravitySort gravity) {
      return armGravity(gravity, sliderTarget);
    }

    EqualizeDryRun.Outcome outcome = dryRuns.runOnClone(algorithm);
    if (outcome != null) {
      if (outcome.aborted() || ctx.shouldStop()) {
        return false;
      }
      return armFromOutcome(algorithm, outcome, sliderTarget);
    }

    // No peer could be constructed (e.g. test stubs): dry-run on the live array instead.
    EqualizeDryRun.Outcome live = dryRuns.runLive(algorithm, production);
    return live != null && armFromOutcome(algorithm, live, sliderTarget);
  }

  private boolean armGravity(GravitySort gravity, float sliderTarget) {
    int naturalBeats = GravitySort.estimateFrameBeats(ctx.array);
    if (naturalBeats <= 0) {
      return false;
    }
    int n = ctx.array.getLength();
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
    return arm(gravity.getName(), visualBeats, visualBeats, sliderTarget, 0);
  }

  private boolean armFromOutcome(
      SortingAlgorithm algorithm, EqualizeDryRun.Outcome outcome, float sliderTarget) {
    int n = ctx.array.getLength();
    long rawSteps =
        EqualizeMath.estimateRawSteps(
            outcome.timedOut(), outcome.partialSteps(), outcome.progressSample(), n);
    if (rawSteps <= 0) {
      return false;
    }

    EqualizeMath.DelayStridePlan plan = EqualizeMath.planDelayStride(rawSteps, sliderTarget);
    if (plan.fastForward()) {
      fastForward = true;
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
      visualFrameBeats =
          EqualizeMath.clampToInt(Math.max(1L, (rawFrameBeats + stride - 1L) / stride));
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

    return arm(
        algorithm.getName(), visualSteps, visualFrameBeats, sliderTarget, plan.maxStepsPerFrame());
  }

  private boolean arm(
      String algorithmName,
      int totalSteps,
      int frameBeats,
      float sliderTarget,
      int maxStepsPerFrame) {
    EqualizePacing pacing = ctx.state.equalizePacing();
    pacing.begin(totalSteps, frameBeats, sliderTarget, ctx.array.getLength(), maxStepsPerFrame);
    LOGGER.log(
        Level.INFO,
        "Equalize armed for {0}: steps={1}, frameBeats={2}, targetSec={3}, batch={4}, maxSteps/frame={5}",
        new Object[] {
          algorithmName,
          totalSteps,
          frameBeats,
          sliderTarget,
          pacing.batchBeats(),
          pacing.maxStepsPerFrame()
        });
    return true;
  }
}
