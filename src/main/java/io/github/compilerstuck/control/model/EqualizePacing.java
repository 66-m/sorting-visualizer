package io.github.compilerstuck.control.model;

import io.github.compilerstuck.control.config.AppConfig;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Live equalize-sort-duration pacing shared between the sort worker and the render thread. When
 * inactive, the render path uses the normal steps-per-frame speed setting.
 *
 * <p>Algorithms that use {@code delayFrame()} (e.g. Gravity Sort) are normally capped at one beat
 * per published frame. Under equalization this class enables:
 *
 * <ul>
 *   <li><b>Batching</b> when the slider target is below that floor — beats consume plain step
 *       credits so multiple columns can share a frame (slightly chunkier, but hits the target).
 *   <li><b>Stretching</b> when the target is above the floor — extra published frames are inserted
 *       between beats so the run lasts ~the slider duration.
 * </ul>
 *
 * <p>Grants are capped per frame so large arrays (e.g. Gravity at 100k) cannot pack thousands of
 * heavy beats into one draw and freeze the UI. The run then takes longer than the slider target.
 */
public final class EqualizePacing {
  private static final float MIN_REMAINING_SEC = 1e-3f;

  private volatile boolean active;
  private volatile int totalSteps;
  private volatile int frameBeats;
  private volatile float sliderTargetSec;
  private volatile int maxStepsPerFrame = AppConfig.EQUALIZE_MAX_STEPS_PER_FRAME;
  private volatile long startNanos;

  private final AtomicInteger stepsConsumed = new AtomicInteger();
  private final AtomicInteger frameBeatsConsumed = new AtomicInteger();

  /** Fractional extra frames owed while stretching {@code delayFrame} beats. */
  private double frameWaitDebt;

  public boolean isActive() {
    return active;
  }

  public int totalSteps() {
    return totalSteps;
  }

  public int frameBeats() {
    return frameBeats;
  }

  /** Slider target duration (seconds); also the wall-clock goal when batching/stretching. */
  public float effectiveTargetSec() {
    return sliderTargetSec;
  }

  public float sliderTargetSec() {
    return sliderTargetSec;
  }

  public int maxStepsPerFrame() {
    return maxStepsPerFrame;
  }

  public int stepsConsumed() {
    return stepsConsumed.get();
  }

  public int frameBeatsConsumed() {
    return frameBeatsConsumed.get();
  }

  /**
   * True when the slider asks for a shorter run than one published frame per {@code delayFrame}
   * beat. Callers should treat {@code delayFrame} like {@code delay} (no credit drain).
   */
  public boolean batchBeats() {
    if (!active || frameBeats <= 0 || sliderTargetSec <= 0f) {
      return false;
    }
    return sliderTargetSec < AppConfig.equalizeFrameFloorSec(frameBeats);
  }

  /**
   * After a real {@code delayFrame} beat (non-batching), how many additional published frames to
   * wait so stretched runs hit {@link #sliderTargetSec()}.
   */
  public int takeExtraFrameWaits() {
    if (!active || batchBeats() || frameBeats <= 0 || sliderTargetSec <= 0f) {
      return 0;
    }
    double idealFramesPerBeat =
        (sliderTargetSec * (double) AppConfig.TARGET_FRAME_RATE) / (double) frameBeats;
    frameWaitDebt += Math.max(0d, idealFramesPerBeat - 1d);
    int extra = (int) frameWaitDebt;
    frameWaitDebt -= extra;
    return Math.max(0, extra);
  }

  /** Arms pacing for one algorithm run. No-op when {@code totalSteps <= 0}. */
  public void begin(int totalSteps, int frameBeats, float sliderTargetSec) {
    begin(totalSteps, frameBeats, sliderTargetSec, 0);
  }

  /**
   * @param arrayLength used to cap batched {@code delayFrame} beats per draw frame on large arrays
   */
  public void begin(int totalSteps, int frameBeats, float sliderTargetSec, int arrayLength) {
    begin(totalSteps, frameBeats, sliderTargetSec, arrayLength, 0);
  }

  /**
   * @param maxStepsPerFrameOverride when {@code > 0}, caps grants per frame (e.g. {@code 1} for
   *     strided {@code delay()} algorithms so {@code awaitIdle} cannot stall on leftover credits)
   */
  public void begin(
      int totalSteps,
      int frameBeats,
      float sliderTargetSec,
      int arrayLength,
      int maxStepsPerFrameOverride) {
    if (totalSteps <= 0) {
      clear();
      return;
    }
    this.totalSteps = totalSteps;
    this.frameBeats = Math.max(0, frameBeats);
    this.sliderTargetSec = Math.max(0f, sliderTargetSec);
    if (maxStepsPerFrameOverride > 0) {
      this.maxStepsPerFrame = maxStepsPerFrameOverride;
    } else {
      this.maxStepsPerFrame =
          this.frameBeats > 0
              ? AppConfig.equalizeMaxFrameBeatsPerFrame(arrayLength)
              : AppConfig.EQUALIZE_MAX_STEPS_PER_FRAME;
    }
    this.stepsConsumed.set(0);
    this.frameBeatsConsumed.set(0);
    this.frameWaitDebt = 0d;
    this.startNanos = System.nanoTime();
    this.active = true;
  }

  public void clear() {
    active = false;
    totalSteps = 0;
    frameBeats = 0;
    sliderTargetSec = 0f;
    maxStepsPerFrame = AppConfig.EQUALIZE_MAX_STEPS_PER_FRAME;
    stepsConsumed.set(0);
    frameBeatsConsumed.set(0);
    frameWaitDebt = 0d;
    startNanos = 0L;
  }

  public void recordStep() {
    if (active) {
      stepsConsumed.incrementAndGet();
    }
  }

  public void recordFrameBeat() {
    if (active) {
      stepsConsumed.incrementAndGet();
      frameBeatsConsumed.incrementAndGet();
    }
  }

  /**
   * Credits to grant this frame under schedule correction. Returns {@code -1} when inactive so the
   * caller can fall back to the speed slider.
   */
  public int stepsForDelta(float deltaSeconds) {
    if (!active || totalSteps <= 0) {
      return -1;
    }
    int remainingSteps = Math.max(0, totalSteps - stepsConsumed.get());
    if (remainingSteps == 0) {
      // Budget exhausted but the worker may still be awaiting (underestimated step count /
      // delay stride). Sprint at the per-frame cap instead of dripping 1 credit/frame.
      return maxStepsPerFrame;
    }
    float elapsedSec = (System.nanoTime() - startNanos) / 1_000_000_000f;
    float remainingTime = Math.max(MIN_REMAINING_SEC, sliderTargetSec - elapsedSec);

    if (!batchBeats()) {
      // Non-batched frame beats still need ~1 credit per published frame; do not try to "catch up"
      // past that with huge grants (leftover credits are drained by delayFrame).
      int remainingFrameBeats = Math.max(0, frameBeats - frameBeatsConsumed.get());
      float frameFloorRemaining = AppConfig.equalizeFrameFloorSec(remainingFrameBeats);
      remainingTime = Math.max(remainingTime, frameFloorRemaining);
    }

    int grant = AppConfig.equalizedSortStepsForDelta(deltaSeconds, remainingSteps, remainingTime);
    return Math.min(grant, maxStepsPerFrame);
  }
}
