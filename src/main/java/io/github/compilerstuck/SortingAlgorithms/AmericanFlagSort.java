package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;

public class AmericanFlagSort extends SortingAlgorithm {

    public AmericanFlagSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "American Flag Sort";
        alternativeSize = arrayController.getLength();
        this.delayTime = 10;
    }

    public AmericanFlagSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "American Flag Sort";
        this.alternativeSize = alternativeSize;
        this.delayTime = 10;
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        final int M = arrayController.getLength();

        int[] count = new int[M];
        for (int num : arrayController.getArray()) {
            count[num % M]++;
            arrayController.addWritesAux(1);
        }
        int[] start = new int[M];
        for (int i = 1; i < M && run; i++) {
            start[i] = start[i - 1] + count[i - 1];
            arrayController.addWritesAux(1);
        }
        for (int b = 0; b < M && run; b++) {
            while (count[b] > 0) {
                int origin = start[b];
                int from = origin;
                int num = arrayController.get(from);
                arrayController.set(from, 0);
                do {
                    int to = start[num % M]++;
                    count[num % M]--;
                    int temp = arrayController.get(to);
                    arrayController.set(to, num);

                    num = temp;
                    from = to;
                    
                    delay(new int[]{to});

                } while (from != origin && run);
            }

        }


        arrayController.addRealTime(System.nanoTime() - startTime);
    }
}
