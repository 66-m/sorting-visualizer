package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

import java.util.Random;

public class OddEvenSort extends SortingAlgorithm {

    public OddEvenSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Odd Even Sort";
        alternativeSize = arrayController.getLength();
    }

    public OddEvenSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Odd Even Sort";
        this.alternativeSize = alternativeSize;
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        long startTime = System.nanoTime();

        boolean isSorted = false; // Initially array is unsorted

        while (!isSorted && run) {
            isSorted = true;
            int temp = 0;

            // Perform Bubble sort on odd indexed element
            for (int i = 1; i <= arrayController.getLength() - 2 && run; i = i + 2) {
                if (arrayController.get(i) > arrayController.get(i + 1)) {
                    arrayController.swap(i, i + 1);
                    isSorted = false;

                    if (delay && new Random().nextInt(45) == 1) {
                        arrayController.setMarker(i, Marker.SET);
                        arrayController.setMarker(i + 1, Marker.SET);
                        arrayController.addRealTime(System.nanoTime() - startTime);
                        proc.delay(1);
                        startTime = System.nanoTime();
                    }
                }
                arrayController.addComparisons(1);
            }

            // Perform Bubble sort on even indexed element
            for (int i = 0; i <= arrayController.getLength() - 2 && run; i = i + 2) {
                if (arrayController.get(i) > arrayController.get(i + 1)) {
                    arrayController.swap(i, i + 1);
                    isSorted = false;
                    if (delay && new Random().nextInt(45) == 1) {
                        arrayController.setMarker(i, Marker.SET);
                        arrayController.setMarker(i + 1, Marker.SET);
                        arrayController.addRealTime(System.nanoTime() - startTime);
                        proc.delay(1);
                        startTime = System.nanoTime();
                    }
                }

                arrayController.addComparisons(1);
            }
        }

        arrayController.addRealTime(System.nanoTime() - startTime);
    }
}
