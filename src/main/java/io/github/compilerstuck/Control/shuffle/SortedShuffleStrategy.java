package io.github.compilerstuck.Control.shuffle;

import io.github.compilerstuck.Control.config.ShuffleStrategy;
import io.github.compilerstuck.Control.model.ArrayModel;
import io.github.compilerstuck.Control.model.CancellationToken;
import io.github.compilerstuck.Control.model.OperationReporter;
import io.github.compilerstuck.Control.render.ProcessingContext;
import io.github.compilerstuck.Visual.Marker;

/**
 * Leaves the array in sorted order while still animating the pass.
 */
public class SortedShuffleStrategy implements ShuffleStrategy {

    @Override
    public void shuffle(ArrayModel model, ProcessingContext ctx, OperationReporter reporter, CancellationToken token) {
        int length = model.getLength();
        for (int i = 0; i < length && !token.isCancelled(); i++) {
            model.setMarker(i, Marker.SET);
            reporter.report("Shuffling (sorted).. " + (int) ((double) i / (length - 1) * 100) + "%");
            RandomShuffleStrategy.maybeDelay(ctx, i, length);
        }
    }
}
