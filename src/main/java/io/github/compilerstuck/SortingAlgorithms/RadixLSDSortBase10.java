package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;

import java.util.ArrayList;
import java.util.List;

public class RadixLSDSortBase10 extends SortingAlgorithm {

    int RADIX = 10;

    public RadixLSDSortBase10(ArrayController arrayController) {
        super(arrayController);
        this.name = "Radix (LSD) Sort (Base " + RADIX + ")";
        alternativeSize = arrayController.getLength();
    }

    public RadixLSDSortBase10(ArrayController arrayController, int radix_base) {
        super(arrayController);
        RADIX = radix_base;
        this.name = "Radix (LSD) Sort (Base " + RADIX + ")";
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();


        List<Integer>[] bucket = new ArrayList[RADIX];
        for (int i = 0; i < bucket.length && run; i++) {
            bucket[i] = new ArrayList<>();
        }
        boolean maxLength = false;
        int tmp, placement = 1;
        while (!maxLength && run) {
            maxLength = true;
            for (Integer i : arrayController.getArray()) {
                tmp = i / placement;
                bucket[tmp % RADIX].add(i);
                if (maxLength && tmp > 0) {
                    maxLength = false;
                }
            }

            int[] buckA = new int[RADIX];
            for (int i = 0; i < RADIX && run; i++) {
                for (int j = i - 1; j >= 0 && run; j--) {
                    buckA[i] += bucket[j].size();
                }
            }

            for (int i = 0; i < arrayController.getLength() && run; i++) {
                for (int j = 0; j < RADIX && run; j++) {
                    if (bucket[j].size() <= i) {
                        bucket[j].clear();
                        continue;
                    } else {
                        arrayController.set(buckA[j]++, bucket[j].get(i));
                    }

                    delay(new int[]{buckA[j] - 1});
                }
            }

            placement *= RADIX;
        }

        arrayController.addRealTime(System.nanoTime() - startTime);
    }

}
