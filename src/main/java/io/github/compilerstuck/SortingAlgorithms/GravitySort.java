package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

public class GravitySort extends SortingAlgorithm {

    long startTime;

    public GravitySort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Gravity (Bead) Sort";
        alternativeSize = arrayController.getLength();
    }

    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        int max = arrayController.get(0);
        for (int i = 1; i < arrayController.getLength() && run; i++)
            if (arrayController.get(i) > max) {
                max = arrayController.get(i);
                arrayController.addComparisons(1);
            }

        int[][] abacus = new int[arrayController.getLength()][max];
        for (int i = 0; i < arrayController.getLength() && run; i++) {
            for (int j = 0; j < arrayController.get(i); j++) {
                arrayController.addComparisons(1);
                abacus[i][abacus[0].length - j - 1] = 1;
                arrayController.addWritesAux(1);
            }
        }
        //apply gravity
        for (int i = 0; i < abacus[0].length && run; i++) {
            for (int j = 0; j < abacus.length && run; j++) {
                if (abacus[j][i] == 1) {
                    //Drop it
                    int droppos = j;
                    while (droppos + 1 < abacus.length && abacus[droppos][i] == 1)
                        droppos++;
                    if (abacus[droppos][i] == 0) {
                        abacus[j][i] = 0;
                        abacus[droppos][i] = 1;
                        arrayController.addWritesAux(2);
                    }
                }
            }

            int count;
            for (int x = 0; x < abacus.length && run; x++) {
                count = 0;
                for (int y = 0; y < abacus[0].length; y++)
                    count += abacus[x][y];
                arrayController.set(x, count);
            }
            if (delay) {
                arrayController.setMarker(arrayController.getLength() - i - 1, Marker.SET);
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(3);
                startTime = System.nanoTime();
            }
        }

    }

}
