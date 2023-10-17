package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

public class SelectionSort extends SortingAlgorithm {

    public SelectionSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Selection Sort";
        alternativeSize = arrayController.getLength();
        delayTime = 10;
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        int n = arrayController.getLength();

        for (int i = 0; i < n && run; i++) {

            int min_index = i;
            for (int j = i + 1; j < n && run; j++) {
                if (arrayController.get(j) < arrayController.get(min_index)) {
                    min_index = j;
                }
                arrayController.addComparisons(1);
            }

            arrayController.swap(min_index, i);

            delay(new int[]{i, min_index});


        }


    }

}
