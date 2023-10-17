package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

import java.util.Random;

public class ShakerSort extends SortingAlgorithm {

    public ShakerSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Shaker Sort";
        alternativeSize = arrayController.getLength();
        delayFactor = 1. / 100;
    }

    public ShakerSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Shaker Sort";
        this.alternativeSize = alternativeSize;
        delayFactor = 1. / 100;
    }

    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        boolean swapped = true;
        int start = 0;
        int end = arrayController.getLength();

        while (swapped && run) {
            swapped = false;

            for (int i = start; i < end - 1 && run; ++i) {
                if (arrayController.get(i) > arrayController.get(i + 1)) {
                    arrayController.swap(i, i + 1);
                    swapped = true;
                    
                    delay(new int[]{i, i + 1});
                }
                arrayController.addComparisons(1);

            }

            if (!swapped)
                break;

            swapped = false;

            end = end - 1;

            for (int i = end - 1; i >= start && run; i--) {
                if (arrayController.get(i) > arrayController.get(i + 1)) {
                    arrayController.swap(i, i + 1);
                    swapped = true;
                    
                    delay(new int[]{i, i + 1});
                }
                arrayController.addComparisons(1);

            }

            start = start + 1;
        }


        arrayController.addRealTime(System.nanoTime() - startTime);

    }
}
