package io.github.compilerstuck.Control.shuffle;

import io.github.compilerstuck.Control.model.ArrayModel;
import io.github.compilerstuck.Control.render.ProcessingContext;
import io.github.compilerstuck.Control.config.ShuffleStrategy;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.SortingAlgorithms.SortingAlgorithm;
import io.github.compilerstuck.Visual.Marker;

/**
 * Reverses the array in-place.
 */
public class ReverseShuffleStrategy implements ShuffleStrategy {

    @Override
    public void shuffle(ArrayModel model, ProcessingContext ctx) {
        int length = model.getLength();
        int half = length / 2;
        for (int i = 0; i < half && SortingAlgorithm.isRun(); i++) {
            int j = length - 1 - i;
            model.swap(i, j);
            model.setMarker(i, Marker.SET);
            model.setMarker(j, Marker.SET);
            MainController.setCurrentOperation("Shuffling (reverse).. " + (int) (i / (half - 1.) * 100) + "%");
            RandomShuffleStrategy.maybeDelay(ctx, i, length);
        }
    }
}
