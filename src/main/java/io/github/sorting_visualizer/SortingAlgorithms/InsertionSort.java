package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;

public class InsertionSort extends SortingAlgorithm {

    public InsertionSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Insertion Sort";
        alternativeSize = arrayController.getLength();
    }

    public InsertionSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Insertion Sort";
        this.alternativeSize = alternativeSize;
    }

    public void sort() {
        MainController.setCurrentOperation(name);
        long startTime = System.nanoTime();

        int n = arrayController.getLength();

        for (int i = 1; i < n && run; ++i) {

            int x = arrayController.get(i);
            int j = i - 1;


            while (j >= 0 && arrayController.get(j) > x && run) {

                arrayController.set(j + 1, arrayController.get(j));

                arrayController.addComparisons(1);

                j = j - 1;
            }

            arrayController.addRealTime(System.nanoTime() - startTime);
            proc.delay(10);
            startTime = System.nanoTime();


            arrayController.set(j + 1, x);


            arrayController.setMarker(j + 1, Marker.SET);

        }
    }

}
