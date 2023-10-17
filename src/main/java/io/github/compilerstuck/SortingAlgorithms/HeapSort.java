package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

public class HeapSort extends SortingAlgorithm {

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

        for (int i = n - 1; i >= 0 && run; i--) {
            int temp = arrayController.get(0);
            arrayController.set(0, arrayController.get(i));
            arrayController.set(i, temp);

            delay(new int[]{i, 0});

            heapify(arrayController, i, 0);
        }

        arrayController.addRealTime(System.nanoTime() - startTime);
    }

    private void heapify(ArrayController arrayController, int n, int i) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;

        if (l < n && arrayController.get(l) > arrayController.get(largest))
            largest = l;
        arrayController.addComparisons(1);

        if (r < n && arrayController.get(r) > arrayController.get(largest))
            largest = r;
        arrayController.addComparisons(1);

        if (largest != i && run) {
            arrayController.swap(i, largest);

            delay(new int[]{i, largest});

            heapify(arrayController, n, largest);
        }

    }

}
