package io.github.compilerstuck.control.shuffle;

import io.github.compilerstuck.control.config.MainControllerConfig;
import io.github.compilerstuck.control.config.ShuffleStrategy;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.model.CancellationToken;
import io.github.compilerstuck.control.model.OperationReporter;
import io.github.compilerstuck.control.render.DelayContext;
import io.github.compilerstuck.visual.Marker;

/** Fisher-Yates (random) shuffle. */
public class RandomShuffleStrategy implements ShuffleStrategy {

  @Override
  public void shuffle(
      ArrayModel model, DelayContext ctx, OperationReporter reporter, CancellationToken token) {
    int length = model.getLength();
    for (int i = 0; i < length && !token.isCancelled(); i++) {
      int j = (int) (Math.random() * length);
      model.swap(i, j);
      model.setMarker(i, Marker.SET);
      model.setMarker(j, Marker.SET);
      reporter.report("Shuffling.. " + (int) ((double) i / (length - 1) * 100) + "%");
      maybeDelay(ctx, i, length);
    }
  }

  /**
   * Distributes {@link MainControllerConfig#SHUFFLE_VISUAL_STEPS} delays evenly across {@code
   * iterations} loop steps (0-based index {@code i}). Small loops fire multiple delays per step so
   * total pacing stays ~1s.
   */
  static void maybeDelay(DelayContext ctx, int i, int iterations) {
    int steps = MainControllerConfig.SHUFFLE_VISUAL_STEPS;
    int n = Math.max(1, iterations);
    int due = (int) ((long) (i + 1) * steps / n);
    int prev = (int) ((long) i * steps / n);
    for (int d = prev; d < due; d++) {
      ctx.delay();
    }
  }
}
