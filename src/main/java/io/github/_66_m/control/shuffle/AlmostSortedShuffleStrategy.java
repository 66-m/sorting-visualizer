package io.github._66_m.control.shuffle;

import io.github._66_m.control.config.ShuffleStrategy;
import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.model.CancellationToken;
import io.github._66_m.control.model.OperationReporter;
import io.github._66_m.control.render.DelayContext;
import io.github._66_m.visual.Marker;

/** Performs a small number (length/10) of random swaps, leaving the array nearly sorted. */
public class AlmostSortedShuffleStrategy implements ShuffleStrategy {

  @Override
  public void shuffle(
      ArrayModel model, DelayContext ctx, OperationReporter reporter, CancellationToken token) {
    int length = model.getLength();
    int swaps = Math.max(1, length / 10);
    for (int i = 0; i < swaps && !token.isCancelled(); i++) {
      int a = (int) (Math.random() * length);
      int b = (int) (Math.random() * length);
      model.swap(a, b);
      model.setMarker(a, Marker.SET);
      model.setMarker(b, Marker.SET);
      reporter.report("Shuffling (almost).. " + (int) ((double) i / (swaps - 1) * 100) + "%");
      RandomShuffleStrategy.maybeDelay(ctx, i, swaps);
    }
  }
}
