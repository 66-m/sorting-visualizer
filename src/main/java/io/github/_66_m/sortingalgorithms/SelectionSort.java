package io.github._66_m.sortingalgorithms;

import io.github._66_m.control.model.ArrayModel;

public class SelectionSort extends SortingAlgorithm {

  public SelectionSort(ArrayModel arrayController) {
    super(arrayController);
    alternativeSize = arrayController.getLength();
  }

  @Override
  public void sort() {
    report(getName());

    int n = arrayController.getLength();

    for (int i = 0; i < n && !isCancelled(); i++) {

      int min_index = i;
      for (int j = i + 1; j < n && !isCancelled(); j++) {
        if (arrayController.get(j) < arrayController.get(min_index)) {
          min_index = j;
        }
        arrayController.addComparisons(1);
      }

      arrayController.swap(min_index, i);

      delay(new int[] {i, min_index});
    }
  }
}
