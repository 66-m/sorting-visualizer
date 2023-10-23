package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;

public class QuickSortDualPivot extends SortingAlgorithm {

    public QuickSortDualPivot(ArrayController arrayController) {
        super(arrayController);
        this.name = "Quick Sort (Dual Pivot)";
        alternativeSize = arrayController.getLength();
        delayTime = 3;
    }

    public QuickSortDualPivot(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Quick Sort (Dual Pivot)";
        this.alternativeSize = alternativeSize;
        delayTime = 3;
    }

    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        sort(arrayController, 0, arrayController.getLength() - 1);

        arrayController.addRealTime(System.nanoTime() - startTime);
    }

    private void sort(ArrayController arrayController, int left, int right) {
        if (right > left && run) {
            // Choose outermost elements as pivots
            if (arrayController.get(left) > arrayController.get(right)) {
                arrayController.swap(left, right);

                delay(new int[]{left, right});
            }
            arrayController.addComparisons(1);
            int p = arrayController.get(left), q = arrayController.get(right);

            // Partition A according to invariant below
            int l = left + 1, g = right - 1, k = l;
            while (k <= g && run) {
                if (arrayController.get(k) < p) {
                    arrayController.swap(k, l);
                    
                    delay(new int[]{k, l});

                    ++l;
                } else if (arrayController.get(k) >= q) {
                    while (arrayController.get(g) > q && k < g && run) {
                        --g;
                        arrayController.addComparisons(1);
                    }
                    arrayController.addComparisons(1);
                    arrayController.swap(k, g);

                    delay(new int[]{k, g});

                    --g;
                    if (arrayController.get(k) < p) {
                        arrayController.swap(k, l);
                        
                        delay(new int[]{k, l});

                        ++l;
                    }
                    arrayController.addComparisons(1);
                }
                arrayController.addComparisons(1);
                ++k;
            }
            --l;
            ++g;

            // Swap pivots to final place
            arrayController.swap(left, l);
            arrayController.swap(right, g);
            
            delay(new int[]{left, right, l, g});


            // Recursively sort partitions
            sort(arrayController, left, l - 1);
            sort(arrayController, l + 1, g - 1);
            sort(arrayController, g + 1, right);
        }

    }
}
