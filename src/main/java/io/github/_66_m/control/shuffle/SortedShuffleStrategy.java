package io.github._66_m.control.shuffle;

import io.github._66_m.control.config.ShuffleStrategy;
import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.model.CancellationToken;
import io.github._66_m.control.model.OperationReporter;
import io.github._66_m.control.render.DelayContext;
import io.github._66_m.visual.Marker;

/** Leaves the array in sorted order while still animating the pass. */
public class SortedShuffleStrategy implements ShuffleStrategy {

  @Override
  public void shuffle(
      ArrayModel model, DelayContext ctx, OperationReporter reporter, CancellationToken token) {
    int length = model.getLength();
    for (int i = 0; i < length && !token.isCancelled(); i++) {
      model.setMarker(i, Marker.SET);
      reporter.report("Shuffling (sorted).. " + (int) ((double) i / (length - 1) * 100) + "%");
      RandomShuffleStrategy.maybeDelay(ctx, i, length);
    }
  }
}
