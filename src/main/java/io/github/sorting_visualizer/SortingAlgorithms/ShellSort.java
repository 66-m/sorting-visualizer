package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;

public class ShellSort extends SortingAlgorithm {

    public ShellSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Shell Sort";
        alternativeSize = arrayController.getLength();
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        long startTime = System.nanoTime();

        int n = arrayController.getLength();

        for (int gap = n/2; gap > 0 && run; gap /= 2)
        {

            for (int i = gap; i < n && run; i += 1)
            {
                int temp = arrayController.get(i);

                int j;
                for (j = i; j >= gap && arrayController.get(j - gap) > temp && run; j -= gap) {
                    arrayController.set(j, arrayController.get(j - gap));
                    arrayController.setMarker(j,Marker.SET);
                    arrayController.addComparisons(1);
                }
                arrayController.addComparisons(1);
                arrayController.set(j,temp);
                arrayController.setMarker(i,Marker.SET);
                if (delay){
                    arrayController.addRealTime(System.nanoTime() - startTime);
                    proc.delay(1);
                    startTime = System.nanoTime();
                }
            }

            arrayController.setMarker(gap, Marker.SET);
    }

        arrayController.addRealTime(System.nanoTime() - startTime);


    }

}
