package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

public class CycleSort extends SortingAlgorithm {

    public CycleSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Cycle Sort";
        alternativeSize = arrayController.getLength();
    }

    public CycleSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Cycle Sort";
        this.alternativeSize = alternativeSize;
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        long startTime = System.nanoTime();

        // traverse array elements and put it to on
        // the right place
        for (int cycle_start = 0; cycle_start <= arrayController.getLength() - 2 && run; cycle_start++) {
            // initialize item as starting point
            int item = arrayController.get(cycle_start);

            // Find position where we put the item. We basically
            // count all smaller elements on right side of item.
            int pos = cycle_start;
            for (int i = cycle_start + 1; i < arrayController.getLength(); i++) {
                if (arrayController.get(i) < item)
                    pos++;
                arrayController.addComparisons(1);
            }

            // If item is already in correct position
            if (pos == cycle_start)
                continue;

            // ignore all duplicate elements
            while (item == arrayController.get(pos)) {
                pos += 1;
                arrayController.addComparisons(1);
            }
            arrayController.addComparisons(1);


            // put the item to it's right position
            if (pos != cycle_start) {
                int temp = item;
                item = arrayController.get(pos);
                arrayController.set(pos, temp);
            }
            if (delay) {
                arrayController.setMarker(pos, Marker.SET);
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(8);
                startTime = System.nanoTime();
            }

            // Rotate rest of the cycle
            while (pos != cycle_start && run) {
                pos = cycle_start;

                // Find position where we put the element
                for (int i = cycle_start + 1; i < arrayController.getLength(); i++) {
                    if (arrayController.get(i) < item)
                        pos += 1;
                    arrayController.addComparisons(1);
                }

                // ignore all duplicate elements
                while (item == arrayController.get(pos)) {
                    pos += 1;
                    arrayController.addComparisons(1);
                }

                // put the item to it's right position
                if (item != arrayController.get(pos)) {
                    int temp = item;
                    item = arrayController.get(pos);
                    arrayController.set(pos, temp);
                }
                if (delay) {
                    arrayController.setMarker(pos, Marker.SET);
                    arrayController.addRealTime(System.nanoTime() - startTime);
                    proc.delay(8);
                    startTime = System.nanoTime();
                }
                arrayController.addComparisons(1);
            }
        }


        arrayController.addRealTime(System.nanoTime() - startTime);
    }
}
