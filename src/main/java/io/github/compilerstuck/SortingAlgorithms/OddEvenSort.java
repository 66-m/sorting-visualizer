package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;

public class OddEvenSort extends SortingAlgorithm {

    public OddEvenSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Odd Even Sort";
        alternativeSize = arrayController.getLength();
        delayFactor = 1. / 55;
    }

    public OddEvenSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Odd Even Sort";
        this.alternativeSize = alternativeSize;
        delayFactor = 1. / 55;
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        delayFactor = 1. / (arrayController.getLength()/36);

        boolean isSorted = false; // Initially array is unsorted

        while (!isSorted && run) {
            isSorted = true;
            //int temp = 0;

            // Perform Bubble sort on odd indexed element
            for (int i = 1; i <= arrayController.getLength() - 2 && run; i = i + 2) {
                if (arrayController.get(i) > arrayController.get(i + 1)) {
                    arrayController.swap(i, i + 1);
                    isSorted = false;

                    delay(new int[]{i, i + 1});
                }
                arrayController.addComparisons(1);
            }

            // Perform Bubble sort on even indexed element
            for (int i = 0; i <= arrayController.getLength() - 2 && run; i = i + 2) {
                if (arrayController.get(i) > arrayController.get(i + 1)) {
                    arrayController.swap(i, i + 1);
                    isSorted = false;
                    
                    delay(new int[]{i, i + 1});
                }

                arrayController.addComparisons(1);
            }
        }

        arrayController.addRealTime(System.nanoTime() - startTime);
    }
}
