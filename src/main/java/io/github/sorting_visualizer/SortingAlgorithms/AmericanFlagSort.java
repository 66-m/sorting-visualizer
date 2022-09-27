package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;

public class AmericanFlagSort extends SortingAlgorithm {

    public AmericanFlagSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "American Flag Sort";
        alternativeSize = arrayController.getLength();
    }

    public AmericanFlagSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "American Flag Sort";
        this.alternativeSize = alternativeSize;
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        long startTime = System.nanoTime();

        final int M = arrayController.getLength();

        int[] count = new int[M];
        for (int num : arrayController.getArray()) {
            count[num % M]++;
            arrayController.addWritesAux(1);
        }
        int[] start = new int[M];
        for (int i = 1; i < M && run; i++) {
            start[i] = start[i - 1] + count[i - 1];
            arrayController.addWritesAux(1);
        }
        for (int b = 0; b < M && run; b++) {
            while (count[b] > 0) {
                int origin = start[b];
                int from = origin;
                int num = arrayController.get(from);
                arrayController.set(from, 0);
                do {
                    int to = start[num % M]++;
                    count[num % M]--;
                    int temp = arrayController.get(to);
                    arrayController.set(to, num);

                    num = temp;
                    from = to;
                    if (delay) {
                        arrayController.setMarker(to, Marker.SET);
                        arrayController.addRealTime(System.nanoTime() - startTime);
                        proc.delay(10);
                        startTime = System.nanoTime();
                    }
                } while (from != origin && run);
            }

        }


        arrayController.addRealTime(System.nanoTime() - startTime);
    }
}
