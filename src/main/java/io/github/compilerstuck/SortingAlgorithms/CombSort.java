package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;

public class CombSort extends SortingAlgorithm {

    public CombSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Comb Sort";
        alternativeSize = arrayController.getLength();
        delayFactor = 0.25;
    }

    public CombSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Comb Sort";
        this.alternativeSize = alternativeSize;
        delayFactor = 0.25;
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        int n = arrayController.getLength();

        int gap = n;

        boolean swapped = true;

        while (gap != 1 || swapped && run) {
            gap = getNextGap(gap);

            swapped = false;

            for (int i = 0; i < n - gap && run; i++) {
                if (arrayController.get(i) > arrayController.get(i + gap)) {
                    arrayController.swap(i, i + gap);

                    swapped = true;
                }
                
                delay(new int[]{i, i + gap});

                arrayController.addComparisons(1);
            }
        }


        arrayController.addRealTime(System.nanoTime() - startTime);
    }

    int getNextGap(int gap) {
        // Shrink gap by Shrink factor
        gap = (gap * 10) / 13;
        return Math.max(gap, 1);
    }
}
