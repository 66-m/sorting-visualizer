package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

public class InsertionSort extends SortingAlgorithm {

    public InsertionSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Insertion Sort";
        alternativeSize = arrayController.getLength();
        delayTime = 10;
    }

    public InsertionSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Insertion Sort";
        this.alternativeSize = alternativeSize;
        delayTime = 10;
    }

    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        int n = arrayController.getLength();

        for (int i = 1; i < n && run; ++i) {

            int x = arrayController.get(i);
            int j = i - 1;


            while (j >= 0 && arrayController.get(j) > x && run) {

                arrayController.set(j + 1, arrayController.get(j));

                arrayController.addComparisons(1);

                j = j - 1;
            }

            
            arrayController.set(j + 1, x);

            delay(new int[]{j + 1});
        }
    }

}
