package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;

public class InsertionSort extends SortingAlgorithm {

  public InsertionSort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Insertion Sort";
    alternativeSize = arrayController.getLength();
  }

  public InsertionSort(ArrayModel arrayController, int alternativeSize) {
    super(arrayController);
    this.name = "Insertion Sort";
    this.alternativeSize = alternativeSize;
  }

  @Override
  public void sort() {
    report(name);

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
