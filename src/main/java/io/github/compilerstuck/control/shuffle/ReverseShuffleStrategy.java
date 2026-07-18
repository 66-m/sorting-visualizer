package io.github.compilerstuck.control.shuffle;

import io.github.compilerstuck.control.config.ShuffleStrategy;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.model.CancellationToken;
import io.github.compilerstuck.control.model.OperationReporter;
import io.github.compilerstuck.control.render.ProcessingContext;
import io.github.compilerstuck.visual.Marker;

/** Reverses the array in-place. */
public class ReverseShuffleStrategy implements ShuffleStrategy {

  @Override
  public void shuffle(
      ArrayModel model,
      ProcessingContext ctx,
      OperationReporter reporter,
      CancellationToken token) {
    int length = model.getLength();
    int half = length / 2;
    for (int i = 0; i < half && !token.isCancelled(); i++) {
      int j = length - 1 - i;
      model.swap(i, j);
      model.setMarker(i, Marker.SET);
      model.setMarker(j, Marker.SET);
      reporter.report("Shuffling (reverse).. " + (int) (i / (half - 1.) * 100) + "%");
      RandomShuffleStrategy.maybeDelay(ctx, i, length);
    }
  }
}
