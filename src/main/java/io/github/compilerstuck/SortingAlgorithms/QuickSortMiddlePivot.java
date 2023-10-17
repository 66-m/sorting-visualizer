package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

public class QuickSortMiddlePivot extends SortingAlgorithm {

    public QuickSortMiddlePivot(ArrayController arrayController) {
        super(arrayController);
        this.name = "Quick Sort (Pivot Middle)";
        alternativeSize = arrayController.getLength();
    }

    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();
        sort(arrayController, 0, arrayController.getLength() - 1);
        arrayController.addRealTime(System.nanoTime() - startTime);
    }

    private void sort(ArrayController arrayController, int start, int end) {
        if (arrayController.getLength() == 0) {
            return;
        }

        if (start >= end) {
            return;
        }

        // pick the pivot
        int middle = start + (end - start) / 2;
        int pivot = arrayController.get(middle);

        // make left < pivot and right > pivot
        int i = start, j = end;
        while (i <= j && run) {
            while (arrayController.get(i) < pivot && run) {
                i++;
                arrayController.addComparisons(1);
            }
            arrayController.addComparisons(1);

            while (arrayController.get(j) > pivot && run) {
                j--;
                arrayController.addComparisons(1);
            }
            arrayController.addComparisons(1);

            if (i <= j) {
                arrayController.swap(i, j);
                arrayController.setMarker(i, Marker.SET);
                arrayController.setMarker(j, Marker.SET);
                i++;
                j--;
            }

            delay();

        }

        // recursively sort two sub parts
        if (start < j && run)
            sort(arrayController, start, j);

        if (end > i && run)
            sort(arrayController, i, end);
    }
}
