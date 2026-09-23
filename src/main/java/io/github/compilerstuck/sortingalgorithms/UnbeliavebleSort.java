package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;

public class UnbeliavebleSort extends SortingAlgorithm {

  public UnbeliavebleSort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "I Can't Believe It Can Sort";
    alternativeSize = arrayController.getLength();
    selected = false;
  }

  public UnbeliavebleSort(ArrayModel arrayController, int alternativeSize) {
    super(arrayController);
    this.name = "I Can't Believe It Can Sort";
    this.alternativeSize = alternativeSize;
  }

  @Override
  public void sort() {
    report(name);

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
