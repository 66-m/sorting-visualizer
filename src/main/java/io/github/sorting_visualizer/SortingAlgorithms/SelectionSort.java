package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;
import io.github.sorting_visualizer.Control.ArrayController;

public class SelectionSort extends SortingAlgorithm {

    public SelectionSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Selection Sort";
        alternativeSize = arrayController.getLength();
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        long startTime = System.nanoTime();

        int n = arrayController.getLength();

        for (int i = 0; i < n && run; i++) {

            int min_index = i;
            for (int j = i + 1; j < n && run; j++) {
                if (arrayController.get(j) < arrayController.get(min_index)) {
                    min_index = j;
                }
                arrayController.addComparisons(1);
            }
            if (delay){
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(10);
                startTime = System.nanoTime();
            }


            arrayController.swap(min_index, i);
            arrayController.setMarker(min_index, Marker.SET);
            arrayController.setMarker(i, Marker.SET);
        }


    }

}
