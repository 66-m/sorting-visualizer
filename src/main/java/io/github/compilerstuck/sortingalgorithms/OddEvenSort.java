package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;

public class OddEvenSort extends SortingAlgorithm {

  public OddEvenSort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Odd Even Sort";
    alternativeSize = arrayController.getLength();
  }

  public OddEvenSort(ArrayModel arrayController, int alternativeSize) {
    super(arrayController);
    this.name = "Odd Even Sort";
    this.alternativeSize = alternativeSize;
  }

  public void sort() {
    report(name);

    boolean isSorted = false; // Initially array is unsorted

    while (!isSorted && !isCancelled()) {
      isSorted = true;
      // int temp = 0;

      // Perform Bubble sort on odd indexed element
      for (int i = 1; i <= arrayController.getLength() - 2 && !isCancelled(); i = i + 2) {
        if (arrayController.get(i) > arrayController.get(i + 1)) {
          arrayController.swap(i, i + 1);
          isSorted = false;

          delay(new int[] {i, i + 1});
        }
        arrayController.addComparisons(1);
      }

      // Perform Bubble sort on even indexed element
      for (int i = 0; i <= arrayController.getLength() - 2 && !isCancelled(); i = i + 2) {
        if (arrayController.get(i) > arrayController.get(i + 1)) {
          arrayController.swap(i, i + 1);
          isSorted = false;

          delay(new int[] {i, i + 1});
        }

        arrayController.addComparisons(1);
      }
    }
  }
}
