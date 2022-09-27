package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;

import java.util.Random;

public class CombSort extends SortingAlgorithm {

    public CombSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Comb Sort";
        alternativeSize = arrayController.getLength();
    }

    public CombSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Comb Sort";
        this.alternativeSize = alternativeSize;
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        long startTime = System.nanoTime();

        int n = arrayController.getLength();

        // initialize gap
        int gap = n;

        // Initialize swapped as true to make sure that
        // loop runs
        boolean swapped = true;

        // Keep running while gap is more than 1 and last
        // iteration caused a swap
        while (gap != 1 || swapped && run)
        {
            // Find next gap
            gap = getNextGap(gap);

            // Initialize swapped as false so that we can
            // check if swap happened or not
            swapped = false;

            // Compare all elements with current gap
            for (int i=0; i<n-gap && run; i++)
            {
                if (arrayController.get(i) > arrayController.get(i+gap))
                {
                    // Swap arr[i] and arr[i+gap]
                    arrayController.swap(i,i+gap);

                    // Set swapped
                    swapped = true;
                }
                if (delay && new Random().nextInt(1,4) == 1){
                    arrayController.setMarker(i,Marker.SET);
                    arrayController.setMarker(i+gap,Marker.SET);
                    arrayController.addRealTime(System.nanoTime() - startTime);
                    proc.delay(1);
                    startTime = System.nanoTime();
                }
                arrayController.addComparisons(1);
            }
        }


        arrayController.addRealTime(System.nanoTime() - startTime);
    }

    int getNextGap(int gap)
    {
        // Shrink gap by Shrink factor
        gap = (gap*10)/13;
        return Math.max(gap, 1);
    }
}
