package io.github._66_m.sortingalgorithms;

import io.github._66_m.control.model.ArrayModel;

public class ShakerSort extends SortingAlgorithm {

  public ShakerSort(ArrayModel arrayController) {
    super(arrayController);
    alternativeSize = arrayController.getLength();
  }

  public ShakerSort(ArrayModel arrayController, int alternativeSize) {
    super(arrayController);
    this.alternativeSize = alternativeSize;
  }

  @Override
  public void sort() {
    report(getName());

    boolean swapped = true;
    int start = 0;
    int end = arrayController.getLength();

    while (swapped && !isCancelled()) {
      swapped = false;

      for (int i = start; i < end - 1 && !isCancelled(); ++i) {
        if (arrayController.get(i) > arrayController.get(i + 1)) {
          arrayController.swap(i, i + 1);
          swapped = true;

          delay(new int[] {i, i + 1});
        }
        arrayController.addComparisons(1);
      }

      if (!swapped) break;

      swapped = false;

      end = end - 1;

      for (int i = end - 1; i >= start && !isCancelled(); i--) {
        if (arrayController.get(i) > arrayController.get(i + 1)) {
          arrayController.swap(i, i + 1);
          swapped = true;

          delay(new int[] {i, i + 1});
        }
        arrayController.addComparisons(1);
      }

      start = start + 1;
    }
  }
}
