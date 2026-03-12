package io.github.compilerstuck.Control.shuffle;

import io.github.compilerstuck.Control.model.ArrayModel;
import io.github.compilerstuck.Control.render.ProcessingContext;
import io.github.compilerstuck.Control.config.ShuffleStrategy;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.SortingAlgorithms.SortingAlgorithm;
import io.github.compilerstuck.Visual.Marker;

/**
 * Leaves the array in sorted order while still animating the pass.
 */
public class SortedShuffleStrategy implements ShuffleStrategy {

    @Override
    public void shuffle(ArrayModel model, ProcessingContext ctx) {
        int length = model.getLength();
        for (int i = 0; i < length && SortingAlgorithm.isRun(); i++) {
            model.setMarker(i, Marker.SET);
            MainController.setCurrentOperation("Shuffling (sorted).. " + (int) ((double) i / (length - 1) * 100) + "%");
            RandomShuffleStrategy.maybeDelay(ctx, i, length);
        }
    }
}
