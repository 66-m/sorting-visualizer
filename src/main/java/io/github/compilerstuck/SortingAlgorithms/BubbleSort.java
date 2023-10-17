package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

import java.util.Random;

public class BubbleSort extends SortingAlgorithm {

    public BubbleSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Bubble Sort";
        alternativeSize = arrayController.getLength();
        this.delayFactor = 1. / 120;
        delayTime = 1;
    }


    public BubbleSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Bubble Sort";
        this.alternativeSize = alternativeSize;
        this.delayFactor = 1. / 120;
        delayTime = 1;

    }

    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        int n = arrayController.getLength();
        boolean swapped;
        do {
            swapped = false;
            for (int i = 0; i < n - 1 && run; ++i) {
                if (arrayController.get(i) > arrayController.get(i + 1)) {
                    arrayController.swap(i, i + 1);
                    swapped = true;
                    
                    delay(new int[]{i, i + 1});
                }
                arrayController.addComparisons(1);


            }


            n = n - 1;
        } while (swapped && run);


        arrayController.addRealTime(System.nanoTime() - startTime);

    }
}
