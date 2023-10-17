package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

import java.util.Arrays;

public class BucketSort extends SortingAlgorithm {

    public BucketSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Bucket Sort";
        alternativeSize = arrayController.getLength();
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        int max = (Arrays.stream(arrayController.getArray()).max().getAsInt());
        int[] bucket = new int[max + 1];
        for (int i = 0; i <= max && run; i++) {
            bucket[i] = 0;
            arrayController.addWritesAux(1);

        }

        for (int i = 0; i < arrayController.getLength() && run; i++) {
            bucket[arrayController.get(i)]++;
            arrayController.addWritesAux(1);

            delay(new int[]{i});
        }

        for (int i = 0, j = 0; i <= max && run; i++) {
            while (bucket[i] > 0) {
                arrayController.addComparisons(1);
                arrayController.set(j++, i);
                bucket[i]--;
                arrayController.addWritesAux(1);
                
                delay(new int[]{j - 1});
            }
        }

        arrayController.addRealTime(System.nanoTime() - startTime);
    }
}
