package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;

import java.util.Arrays;

public class PigeonholeSort extends SortingAlgorithm {

    public PigeonholeSort(ArrayModel arrayController) {
        super(arrayController);
        this.name = "Pigeonhole Sort";
        alternativeSize = arrayController.getLength();
    }

    public PigeonholeSort(ArrayModel arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Pigeonhole Sort";
        this.alternativeSize = alternativeSize;
    }

    public void sort() {
        report(name);
        startTime = System.nanoTime();

        int min = arrayController.get(0);
        int max = arrayController.get(0);
        int range, i, index;

        for (int a = 0; a < arrayController.getLength() && !isCancelled(); a++) {
            if (arrayController.get(a) > max)
                max = arrayController.get(a);
            if (arrayController.get(a) < min)
                min = arrayController.get(a);
            arrayController.addComparisons(2);

            delay(new int[]{a});
        }

        range = max - min + 1;
        int[] phole = new int[range];
        Arrays.fill(phole, 0);
        arrayController.addWritesAux(range);

        for(i = 0; i < arrayController.getLength() && !isCancelled(); i++) {
            phole[arrayController.get(i) - min]++;
            arrayController.addWritesAux(range);
        }

        index = 0;

        for (i = 0; i < range && !isCancelled(); i++) {
            while (phole[i] --> 0) {
                arrayController.set(index++, i + min);
                
                delay(new int[]{index - 1});
            }
        }

        arrayController.addRealTime(System.nanoTime() - startTime);
    }
}
