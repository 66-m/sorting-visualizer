package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

public class CycleSort extends SortingAlgorithm {

    public CycleSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Cycle Sort";
        alternativeSize = arrayController.getLength();
        delayTime = 8;
    }

    public CycleSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Cycle Sort";
        this.alternativeSize = alternativeSize;
        delayTime = 8;
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        for (int cycle_start = 0; cycle_start <= arrayController.getLength() - 2 && run; cycle_start++) {
            int item = arrayController.get(cycle_start);

            int pos = cycle_start;
            for (int i = cycle_start + 1; i < arrayController.getLength(); i++) {
                if (arrayController.get(i) < item)
                    pos++;
                arrayController.addComparisons(1);
            }

            if (pos == cycle_start)
                continue;

            while (item == arrayController.get(pos)) {
                pos += 1;
                arrayController.addComparisons(1);
            }
            arrayController.addComparisons(1);


            if (pos != cycle_start) {
                int temp = item;
                item = arrayController.get(pos);
                arrayController.set(pos, temp);
            }

            delay(new int[]{pos});

            while (pos != cycle_start && run) {
                pos = cycle_start;

                for (int i = cycle_start + 1; i < arrayController.getLength(); i++) {
                    if (arrayController.get(i) < item)
                        pos += 1;
                    arrayController.addComparisons(1);
                }

                while (item == arrayController.get(pos)) {
                    pos += 1;
                    arrayController.addComparisons(1);
                }

                if (item != arrayController.get(pos)) {
                    int temp = item;
                    item = arrayController.get(pos);
                    arrayController.set(pos, temp);
                }
                
                delay(new int[]{pos});

                arrayController.addComparisons(1);
            }
        }


        arrayController.addRealTime(System.nanoTime() - startTime);
    }
}
