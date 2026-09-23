package io.github._66_m.control.model;

import io.github._66_m.control.config.AppConfig;

/** Pure arithmetic behind equalize-sort-duration pacing. */
final class EqualizeMath {

  private EqualizeMath() {}

  /** Result of {@link #planDelayStride(long, float)}. */
  record DelayStridePlan(
      int stride, int visualSteps, int maxStepsPerFrame, long budget, boolean fastForward) {}

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

  static int clampToInt(long value) {
    if (value > Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    if (value < 0) {
      return 0;
    }
    return (int) value;
  }
}
