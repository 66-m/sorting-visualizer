package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

public class MergeSort extends SortingAlgorithm {

    long startTime;

    public MergeSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Merge Sort";
        alternativeSize = arrayController.getLength();
    }

    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        sort(arrayController, 0, arrayController.getLength() - 1);

        arrayController.addRealTime(System.nanoTime() - startTime);

    }


    private void sort(ArrayController arrayController, int l, int r) {
        if (l < r && run) {
            int m = (l + r) / 2;

            sort(arrayController, l, m);
            sort(arrayController, m + 1, r);

            merge(arrayController, l, m, r);
        }
    }

    private void merge(ArrayController arrayController, int l, int m, int r) {

        int n1 = m - l + 1;
        int n2 = r - m;

        int[] L = new int[n1];
        int[] R = new int[n2];

        for (int i = 0; i < n1; ++i) {
            L[i] = arrayController.get(l + i);
        }
        arrayController.addWritesAux(n1);
        for (int j = 0; j < n2; ++j) {
            R[j] = arrayController.get(m + 1 + j);
        }
        arrayController.addWritesAux(n2);


        int i = 0, j = 0;

        int k = l;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arrayController.set(k, L[i]);
                i++;
            } else {
                arrayController.set(k, R[j]);
                j++;
            }
            k++;

            arrayController.setMarker(k, Marker.SET);
            arrayController.addComparisons(1);
            if (delay) {
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(1);
                startTime = System.nanoTime();
            }
        }

        k = copyRemainingElements(arrayController, n1, L, i, k);

        copyRemainingElements(arrayController, n2, R, j, k);
    }

    private int copyRemainingElements(ArrayController arrayController, int n1, int[] l, int i, int k) {
        while (i < n1) {
            arrayController.set(k, l[i]);

            arrayController.setMarker(k, Marker.SET);
            arrayController.addWritesAux(1);
            if (delay) {
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(1);
                startTime = System.nanoTime();
            }


            i++;
            k++;
        }
        return k;
    }

}
