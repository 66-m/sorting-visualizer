package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;

import java.util.Random;

public class TimSort extends SortingAlgorithm {

    public TimSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Tim Sort";
        alternativeSize = arrayController.getLength();
    }

    public TimSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Tim Sort";
        this.alternativeSize = alternativeSize;
    }

    int RUN = 32;

    public void insertionSort(int left, int right) {
        for (int i = 1 + left; i <= right; i++) {
            int temp = arrayController.get(i);
            int j = i - 1;
            while (j >= left && arrayController.get(j) > temp && run) {
                arrayController.addComparisons(1);
                arrayController.set(j + 1, arrayController.get(j));

                if (new Random().nextInt(3) == 1) {
                    delay(new int[]{j + 1});
                }

                j--;
            }
            arrayController.addComparisons(1);
            arrayController.set(j + 1, temp);
            
                if (new Random().nextInt(3) == 1) {
                    delay(new int[]{j + 1});
                }
        }
    }

    public void merge(int l, int m, int r) {
        int len1 = m - l + 1, len2 = r - m;
        int[] left = new int[len1];
        int[] right = new int[len2];
        for (int x = 0; x < len1; x++) {
            left[x] = arrayController.get(l + x);
            arrayController.addWritesAux(1);
        }
        for (int x = 0; x < len2; x++) {
            right[x] = arrayController.get(m + 1 + x);
            arrayController.addWritesAux(1);
        }

        int i = 0;
        int j = 0;
        int k = l;

        while (i < len1 && j < len2 && run) {
            if (left[i] <= right[j]) {
                arrayController.set(k, left[i]);
                
                delay(new int[]{k});

                i++;
            } else {
                arrayController.set(k, right[j]);
                
                delay(new int[]{k});

                j++;
            }
            arrayController.addComparisons(1);
            k++;
        }

        while (i < len1 && run) {
            arrayController.set(k, left[i]);
            
            delay(new int[]{k});

            k++;
            i++;
        }
        while (j < len2 && run) {
            arrayController.set(k, right[j]);
            
            delay(new int[]{k});

            k++;
            j++;
        }

    }

    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        for (int i = 0; i < arrayController.getLength() && run; i += RUN) {
            insertionSort(i, Math.min((i + 31), (arrayController.getLength() - 1)));
        }

        for (int size = RUN; size < arrayController.getLength() && run; size = 2 * size) {

            for (int left = 0; left < arrayController.getLength() && run; left += 2 * size) {
                int right = Math.min((left + 2 * size - 1), (arrayController.getLength() - 1));
                int mid = Math.min(left + size - 1, right);

                merge(left, mid, right);
            }
        }
        arrayController.addRealTime(System.nanoTime() - startTime);
    }


}
