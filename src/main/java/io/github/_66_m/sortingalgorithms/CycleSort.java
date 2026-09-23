package io.github._66_m.sortingalgorithms;

import io.github._66_m.control.model.ArrayModel;

public class CycleSort extends SortingAlgorithm {

  public CycleSort(ArrayModel arrayController) {
    super(arrayController);
    alternativeSize = arrayController.getLength();
  }

  public CycleSort(ArrayModel arrayController, int alternativeSize) {
    super(arrayController);
    this.alternativeSize = alternativeSize;
  }

  @Override
  public void sort() {
    report(getName());

    int n = arrayController.getLength();

    for (int cycle_start = 0; cycle_start <= n - 2 && !isCancelled(); cycle_start++) {
      int item = arrayController.get(cycle_start);

      // Counting pass already reads every remaining element once; piggyback a suffix-sorted check
      // on it for free so a fully-resolved tail can be detected and stopped in one pass instead of
      // re-scanning it on every remaining cycle_start (each of which costs O(n - cycle_start)).
      int pos = cycle_start;
      boolean suffixSorted = true;
      int prev = item;
      for (int i = cycle_start + 1; i < n; i++) {
        int v = arrayController.get(i);
        if (v < item) pos++;
        if (v < prev) suffixSorted = false;
        prev = v;
        arrayController.addComparisons(1);
      }

      if (pos == cycle_start) {
        delay(new int[] {cycle_start});
        if (suffixSorted) break;
        continue;
      }

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

      delay(new int[] {pos});

      while (pos != cycle_start && !isCancelled()) {
        pos = cycle_start;

        for (int i = cycle_start + 1; i < n; i++) {
          if (arrayController.get(i) < item) pos += 1;
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

        delay(new int[] {pos});

        arrayController.addComparisons(1);
      }
    }
  }
}
