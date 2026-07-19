package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;

public class CycleSort extends SortingAlgorithm {

  public CycleSort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Cycle Sort";
    alternativeSize = arrayController.getLength();
  }

  public CycleSort(ArrayModel arrayController, int alternativeSize) {
    super(arrayController);
    this.name = "Cycle Sort";
    this.alternativeSize = alternativeSize;
  }

  @Override
  public void sort() {
    report(name);

    for (int cycle_start = 0;
        cycle_start <= arrayController.getLength() - 2 && !isCancelled();
        cycle_start++) {
      int item = arrayController.get(cycle_start);

      int pos = cycle_start;
      for (int i = cycle_start + 1; i < arrayController.getLength(); i++) {
        if (arrayController.get(i) < item) pos++;
        arrayController.addComparisons(1);
      }

      if (pos == cycle_start) continue;

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

        for (int i = cycle_start + 1; i < arrayController.getLength(); i++) {
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
