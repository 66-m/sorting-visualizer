package io.github._66_m.control.shuffle;

import io.github._66_m.control.config.ShuffleStrategy;
import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.model.CancellationToken;
import io.github._66_m.control.model.OperationReporter;
import io.github._66_m.control.render.DelayContext;
import io.github._66_m.visual.Marker;

/** Reverses the array in-place. */
public class ReverseShuffleStrategy implements ShuffleStrategy {

  @Override
  public void shuffle(
      ArrayModel model, DelayContext ctx, OperationReporter reporter, CancellationToken token) {
    int length = model.getLength();
    int half = length / 2;
    for (int i = 0; i < half && !token.isCancelled(); i++) {
      int j = length - 1 - i;
      model.swap(i, j);
      model.setMarker(i, Marker.SET);
      model.setMarker(j, Marker.SET);
      reporter.report("Shuffling (reverse).. " + (int) (i / (half - 1.) * 100) + "%");
      RandomShuffleStrategy.maybeDelay(ctx, i, half);
    }
  }
}
