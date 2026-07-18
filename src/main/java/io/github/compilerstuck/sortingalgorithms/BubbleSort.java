package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;

public class BubbleSort extends SortingAlgorithm {

    public BubbleSort(ArrayModel arrayController) {
        super(arrayController);
        this.name = "Bubble Sort";
        alternativeSize = arrayController.getLength();
        this.delayFactor = 1. / 120;
        delayTime = 1;
    }

    public BubbleSort(ArrayModel arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Bubble Sort";
        this.alternativeSize = alternativeSize;
        this.delayFactor = 1. / 120;
        delayTime = 1;

    }

    public void sort() {
        report(name);
        startTime = System.nanoTime();

        int n = arrayController.getLength();
        boolean swapped;
        do {
            swapped = false;
            for (int i = 0; i < n - 1 && !isCancelled(); ++i) {
                if (arrayController.get(i) > arrayController.get(i + 1)) {
                    arrayController.swap(i, i + 1);
                    swapped = true;
                    
                    delay(new int[]{i, i + 1});
                }
                arrayController.addComparisons(1);

            }

            n = n - 1;
        } while (swapped && !isCancelled());

        arrayController.addRealTime(System.nanoTime() - startTime);

    }
}
