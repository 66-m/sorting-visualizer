package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;

import java.util.Arrays;

public class CountingSort extends SortingAlgorithm {

    public CountingSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Counting Sort";
        alternativeSize = arrayController.getLength();
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();
        int max = (Arrays.stream(arrayController.getArray()).max().getAsInt());
        int[] counter = new int[max + 1];
        for (int i : arrayController.getArray()) {
            counter[i]++;
            arrayController.addWritesAux(1);
            
            delay(new int[]{i});
        }

        delayTime = 5;

        int ndx = 0;
        for (int i = 0; i < counter.length && run; i++) {
            while (0 < counter[i]) {
                arrayController.addComparisons(1);
                
                delay(new int[]{ndx});

                arrayController.set(ndx++, i);
                counter[i]--;
                arrayController.addWritesAux(1);
            }
        }


        arrayController.addRealTime(System.nanoTime() - startTime);

    }
}
