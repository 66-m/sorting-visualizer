package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;

import java.util.Arrays;

public class PigeonholeSort extends SortingAlgorithm {

    public PigeonholeSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Pigeonhole Sort";
        alternativeSize = arrayController.getLength();
    }

    public PigeonholeSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Pigeonhole Sort";
        this.alternativeSize = alternativeSize;
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        long startTime = System.nanoTime();

        int min = arrayController.get(0);
        int max = arrayController.get(0);
        int range, i, j, index;

        for (int a = 0; a < arrayController.getLength() && run; a++) {
            if (arrayController.get(a) > max)
                max = arrayController.get(a);
            if (arrayController.get(a) < min)
                min = arrayController.get(a);
            arrayController.addComparisons(2);
        }

        range = max - min + 1;
        int[] phole = new int[range];
        Arrays.fill(phole, 0);
        arrayController.addWritesAux(range);

        for (i = 0; i < arrayController.getLength() && run; i++) {
            phole[arrayController.get(i) - min]++;
            index = 0;
            arrayController.addWritesAux(1);
            int[] cpy = phole.clone();
            for (j = 0; j < range; j++)
                while (cpy[j]-- > 0) {
                    arrayController.set(index++, j + min);
                    if (delay) {
                        arrayController.setMarker(index - 1, Marker.SET);
                        arrayController.addRealTime(System.nanoTime() - startTime);
                        proc.delay(1);
                        startTime = System.nanoTime();
                    }
                }
        }


        arrayController.addRealTime(System.nanoTime() - startTime);
    }
}
