package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;

public class HeapSort extends SortingAlgorithm {

    long startTime;

    public HeapSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Heap Sort";
        alternativeSize = arrayController.getLength();
    }

    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        int n = arrayController.getLength();

        for (int i = n / 2 - 1; i >= 0 && run; i--)
            heapify(arrayController, n, i);

        for (int i=n-1; i>=0 && run; i--)
        {
            int temp = arrayController.get(0);
            arrayController.set(0,arrayController.get(i));
            arrayController.set(i,temp);

            if (delay){
                arrayController.setMarker(i, Marker.SET);
                arrayController.setMarker(0, Marker.SET);
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(1);
                startTime = System.nanoTime();
            }

            heapify(arrayController, i, 0);
        }

        arrayController.addRealTime(System.nanoTime() - startTime);
    }

    private void heapify(ArrayController arrayController, int n, int i)
    {
        int largest = i;
        int l = 2*i + 1;
        int r = 2*i + 2;

        if (l < n && arrayController.get(l) > arrayController.get(largest))
            largest = l;
        arrayController.addComparisons(1);

        if (r < n && arrayController.get(r) > arrayController.get(largest))
            largest = r;
        arrayController.addComparisons(1);

        if (largest != i && run)
        {
            arrayController.swap(i,largest);

            if (delay){
                arrayController.setMarker(i, Marker.SET);
                arrayController.setMarker(largest, Marker.SET);
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(1);
                startTime = System.nanoTime();
            }

            heapify(arrayController, n, largest);
        }

    }

}
