package io.github.compilerstuck.Control.shuffle;

import io.github.compilerstuck.Control.config.ShuffleStrategy;
import io.github.compilerstuck.Control.model.ArrayModel;
import io.github.compilerstuck.Control.model.CancellationToken;
import io.github.compilerstuck.Control.model.OperationReporter;
import io.github.compilerstuck.Control.render.ProcessingContext;
import io.github.compilerstuck.Visual.Marker;

/**
 * Performs a small number (length/10) of random swaps, leaving the array
 * nearly sorted.
 */
public class AlmostSortedShuffleStrategy implements ShuffleStrategy {

    @Override
    public void shuffle(ArrayModel model, ProcessingContext ctx, OperationReporter reporter, CancellationToken token) {
        int length = model.getLength();
        int swaps = length / 10;
        for (int i = 0; i < swaps && !token.isCancelled(); i++) {
            int a = (int) (Math.random() * length);
            int b = (int) (Math.random() * length);
            model.swap(a, b);
            model.setMarker(a, Marker.SET);
            model.setMarker(b, Marker.SET);
            reporter.report("Shuffling (almost).. " + (int) ((double) i / (swaps - 1) * 100) + "%");
            RandomShuffleStrategy.maybeDelay(ctx, i, length);
        }
    }
}
