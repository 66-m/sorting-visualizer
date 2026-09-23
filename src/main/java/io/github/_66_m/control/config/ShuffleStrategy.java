package io.github._66_m.control.config;

import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.model.CancellationToken;
import io.github._66_m.control.model.OperationReporter;
import io.github._66_m.control.render.DelayContext;

/**
 * Strategy interface for array shuffle behaviours.
 *
 * <p>Each implementation defines how an {@link ArrayModel} is shuffled and how visual progress is
 * reported. Pacing uses {@link DelayContext#delay()} so the strategy itself is independent of the
 * graphics runtime.
 */
public interface ShuffleStrategy {

  /**
   * Shuffle (or otherwise arrange) the array held by {@code model} and call {@code ctx.delay()} at
   * appropriate intervals to produce a visible animation.
   *
   * @param model the array to operate on
   * @param ctx pacing port for frame-gate steps
   * @param reporter reports progress labels for the UI
   * @param token cancellation signal; stop early when cancelled
   */
  void shuffle(
      ArrayModel model, DelayContext ctx, OperationReporter reporter, CancellationToken token);
}
