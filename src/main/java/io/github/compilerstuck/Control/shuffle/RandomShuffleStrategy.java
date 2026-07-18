package io.github.compilerstuck.Control.shuffle;

import io.github.compilerstuck.Control.config.ShuffleStrategy;
import io.github.compilerstuck.Control.model.ArrayModel;
import io.github.compilerstuck.Control.model.CancellationToken;
import io.github.compilerstuck.Control.model.OperationReporter;
import io.github.compilerstuck.Control.render.ProcessingContext;
import io.github.compilerstuck.Visual.Marker;

/**
 * Fisher-Yates (random) shuffle.
 */
public class RandomShuffleStrategy implements ShuffleStrategy {

    @Override
    public void shuffle(ArrayModel model, ProcessingContext ctx, OperationReporter reporter, CancellationToken token) {
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
     * Fires a delay only at the {@code maxSortingTime} evenly-spaced
     * checkpoints — avoids allocating an ArrayList on every iteration.
     */
    static void maybeDelay(ProcessingContext ctx, int i, int length) {
        int maxSortingTime = 1000;
        int step = Math.max(1, (length - 1) / (maxSortingTime - 1));
        if (i % step == 0) {
            int delayTime = maxSortingTime / Math.min(maxSortingTime, length);
            ctx.delay(delayTime);
        }
    }
}
