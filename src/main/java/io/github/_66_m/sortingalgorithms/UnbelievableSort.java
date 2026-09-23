package io.github._66_m.sortingalgorithms;

import io.github._66_m.control.model.ArrayModel;

public class UnbelievableSort extends SortingAlgorithm {

  public UnbelievableSort(ArrayModel arrayController) {
    super(arrayController);
    alternativeSize = arrayController.getLength();
    selected = false;
  }

  public UnbelievableSort(ArrayModel arrayController, int alternativeSize) {
    super(arrayController);
    this.alternativeSize = alternativeSize;
  }

  @Override
  public void sort() {
    report(getName());

    int n = arrayController.getLength();
    for (int i = 0; i < n && !isCancelled(); i++) {
      for (int j = 0; j < n && !isCancelled(); j++) {
        if (arrayController.get(i) < arrayController.get(j)) {
          arrayController.swap(i, j);
          delay(new int[] {i, j});
        }
        arrayController.addComparisons(1);
      }
      arrayController.addComparisons(1);
    }
  }
}
