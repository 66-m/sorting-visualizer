package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;

public class DoubleSelectionSort extends SortingAlgorithm {

    public DoubleSelectionSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Double Selection Sort";
        alternativeSize = arrayController.getLength();
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        long startTime = System.nanoTime();

        for (int i = 0, j = arrayController.getLength() - 1; i < j && run; i++, j--)
        {
            int min = arrayController.get(i), max = arrayController.get(i);
            int min_i = i, max_i = i;
            for (int k = i; k <= j && run; k++)
            {
                if (arrayController.get(k) > max)
                {
                    max = arrayController.get(k);
                    max_i = k;
                }
                else if (arrayController.get(k) < min)
                {
                    min = arrayController.get(k);
                    min_i = k;
                }
                arrayController.addComparisons(1);

            }

            arrayController.swap(i, min_i);

            if (arrayController.get(min_i) == max)
                arrayController.swap(j, min_i);
            else
                arrayController.swap(j, max_i);
            arrayController.addComparisons(1);

            if (delay){
                arrayController.setMarker(j, Marker.SET);
                arrayController.setMarker(min_i, Marker.SET);
                arrayController.setMarker(max_i, Marker.SET);
                arrayController.setMarker(j,Marker.SET);
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(10);
                startTime = System.nanoTime();
            }

        }

        arrayController.addRealTime(System.nanoTime() - startTime);

    }

}
