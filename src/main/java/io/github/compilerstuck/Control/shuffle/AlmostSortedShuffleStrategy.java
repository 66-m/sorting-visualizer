package io.github.compilerstuck.Control.shuffle;

import io.github.compilerstuck.Control.ArrayModel;
import io.github.compilerstuck.Control.ProcessingContext;
import io.github.compilerstuck.Control.ShuffleStrategy;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.SortingAlgorithms.SortingAlgorithm;
import io.github.compilerstuck.Visual.Marker;

/**
 * Performs a small number (length/10) of random swaps, leaving the array
 * nearly sorted.
 */
public class AlmostSortedShuffleStrategy implements ShuffleStrategy {

    @Override
    public void shuffle(ArrayModel model, ProcessingContext ctx) {
        int length = model.getLength();
        int swaps = length / 10;
        for (int i = 0; i < swaps && SortingAlgorithm.isRun(); i++) {
            int a = (int) (Math.random() * length);
            int b = (int) (Math.random() * length);
            model.swap(a, b);
            model.setMarker(a, Marker.SET);
            model.setMarker(b, Marker.SET);
            MainController.setCurrentOperation("Shuffling (almost).. " + (int) ((double) i / (swaps - 1) * 100) + "%");
            RandomShuffleStrategy.maybeDelay(ctx, i, length);
        }
    }
}
