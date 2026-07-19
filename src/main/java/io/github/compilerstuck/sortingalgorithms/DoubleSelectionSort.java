package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;

public class DoubleSelectionSort extends SortingAlgorithm {

  public DoubleSelectionSort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Double Selection Sort";
    alternativeSize = arrayController.getLength();
  }

  @Override
  public void sort() {
    report(name);

    for (int i = 0, j = arrayController.getLength() - 1; i < j && !isCancelled(); i++, j--) {
      int min = arrayController.get(i), max = arrayController.get(i);
      int min_i = i, max_i = i;
      for (int k = i; k <= j && !isCancelled(); k++) {
        if (arrayController.get(k) > max) {
          max = arrayController.get(k);
          max_i = k;
        } else if (arrayController.get(k) < min) {
          min = arrayController.get(k);
          min_i = k;
        }
        arrayController.addComparisons(1);
      }

      arrayController.swap(i, min_i);

      if (arrayController.get(min_i) == max) arrayController.swap(j, min_i);
      else arrayController.swap(j, max_i);
      arrayController.addComparisons(1);

      delay(new int[] {i, j, min_i, max_i});
    }
  }
}
