package io.github._66_m.sortingalgorithms;

import io.github._66_m.control.model.ArrayModel;

public class InsertionSort extends SortingAlgorithm {

  public InsertionSort(ArrayModel arrayController) {
    super(arrayController);
    alternativeSize = arrayController.getLength();
  }

  public InsertionSort(ArrayModel arrayController, int alternativeSize) {
    super(arrayController);
    this.alternativeSize = alternativeSize;
  }

  @Override
  public void sort() {
    report(getName());

    int n = arrayController.getLength();

    for (int i = 1; i < n && !isCancelled(); ++i) {

      int x = arrayController.get(i);
      int j = i - 1;

      while (j >= 0 && arrayController.get(j) > x && !isCancelled()) {

        arrayController.set(j + 1, arrayController.get(j));

        arrayController.addComparisons(1);

        j = j - 1;
      }

      arrayController.set(j + 1, x);

      delay(new int[] {j + 1});
    }
  }
}
