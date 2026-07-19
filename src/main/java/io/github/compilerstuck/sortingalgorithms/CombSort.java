package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;

public class CombSort extends SortingAlgorithm {

  public CombSort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Comb Sort";
    alternativeSize = arrayController.getLength();
  }

  public CombSort(ArrayModel arrayController, int alternativeSize) {
    super(arrayController);
    this.name = "Comb Sort";
    this.alternativeSize = alternativeSize;
  }

  public void sort() {
    report(name);

    int n = arrayController.getLength();

    int gap = n;

    boolean swapped = true;

    while (gap != 1 || swapped && !isCancelled()) {
      gap = getNextGap(gap);

      swapped = false;

      for (int i = 0; i < n - gap && !isCancelled(); i++) {
        if (arrayController.get(i) > arrayController.get(i + gap)) {
          arrayController.swap(i, i + gap);

          swapped = true;
        }

        delay(new int[] {i, i + gap});

        arrayController.addComparisons(1);
      }
    }
  }

  int getNextGap(int gap) {
    // Shrink gap by Shrink factor
    gap = (gap * 10) / 13;
    return Math.max(gap, 1);
  }
}
