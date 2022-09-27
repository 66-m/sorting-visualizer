package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;

import java.util.Arrays;

public class CountingSort extends SortingAlgorithm {

    long startTime;

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
            if (delay){
                arrayController.setMarker(i, Marker.SET);
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(1);
                startTime = System.nanoTime();
            }
        }
        int ndx = 0;
        for (int i = 0; i < counter.length && run; i++) {
            while (0 < counter[i]) {
                arrayController.addComparisons(1);
                if (delay){
                    arrayController.setMarker(ndx, Marker.SET);
                    arrayController.addRealTime(System.nanoTime() - startTime);
                    proc.delay(5);
                    startTime = System.nanoTime();
                }
                arrayController.set(ndx++,i);
                counter[i]--;
                arrayController.addWritesAux(1);
            }
        }


        arrayController.addRealTime(System.nanoTime() - startTime);

    }
}
