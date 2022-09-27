package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;

public class QuickSortDualPivot extends SortingAlgorithm {

    long startTime;

    public QuickSortDualPivot(ArrayController arrayController) {
        super(arrayController);
        this.name = "Quick Sort (Dual Pivot)";
        alternativeSize = arrayController.getLength();
    }

    public QuickSortDualPivot(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Quick Sort (Dual Pivot)";
        this.alternativeSize = alternativeSize;
    }

    public void sort(){
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        sort(arrayController, 0, arrayController.getLength() - 1);

        arrayController.addRealTime(System.nanoTime() - startTime);
    }

    private void sort(ArrayController arrayController, int left, int right) {
        if (right > left && run) {
            // Choose outermost elements as pivots
            if (arrayController.get(left) > arrayController.get(right)) {
                arrayController.swap(left, right);
                if (delay){
                    arrayController.setMarker(left, Marker.SET);
                    arrayController.setMarker(right,Marker.SET);
                    arrayController.addRealTime(System.nanoTime() - startTime);
                    proc.delay(3);
                    startTime = System.nanoTime();
                }
            }
            arrayController.addComparisons(1);
            int p = arrayController.get(left), q = arrayController.get(right);

            // Partition A according to invariant below
            int l = left + 1, g = right - 1, k = l;
            while (k <= g && run) {
                if (arrayController.get(k) < p) {
                    arrayController.swap(k, l);
                    if (delay){
                        arrayController.setMarker(l,Marker.SET);
                        arrayController.setMarker(k,Marker.SET);
                        arrayController.addRealTime(System.nanoTime() - startTime);
                        proc.delay(3);
                        startTime = System.nanoTime();
                    }
                    ++l;
                } else if (arrayController.get(k) >= q) {
                    while (arrayController.get(g) > q && k < g && run) {
                        --g;
                        arrayController.addComparisons(1);
                    }
                    arrayController.addComparisons(1);
                    arrayController.swap(k, g);
                    if (delay){
                        arrayController.setMarker(g,Marker.SET);
                        arrayController.setMarker(k,Marker.SET);
                        arrayController.addRealTime(System.nanoTime() - startTime);
                        proc.delay(3);
                        startTime = System.nanoTime();
                    }
                    --g;
                    if (arrayController.get(k) < p) {
                        arrayController.swap(k, l);
                        if (delay){
                            arrayController.setMarker(k,Marker.SET);
                            arrayController.setMarker(l,Marker.SET);
                            arrayController.addRealTime(System.nanoTime() - startTime);
                            proc.delay(3);
                            startTime = System.nanoTime();
                        }
                        ++l;
                    }
                    arrayController.addComparisons(1);
                }
                arrayController.addComparisons(1);
                ++k;
            }
            --l; ++g;

            // Swap pivots to final place
            arrayController.swap(left, l);
            arrayController.swap(right, g);
            if (delay){
                arrayController.setMarker(left,Marker.SET);
                arrayController.setMarker(left,Marker.SET);
                arrayController.setMarker(l,Marker.SET);
                arrayController.setMarker(g,Marker.SET);
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(3);
                startTime = System.nanoTime();
            }

            // Recursively sort partitions
            sort(arrayController,left, l - 1);
            sort(arrayController,l + 1, g - 1);
            sort(arrayController,g + 1, right);
        }

    }
}
