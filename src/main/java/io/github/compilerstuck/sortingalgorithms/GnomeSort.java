package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;

public class GnomeSort extends SortingAlgorithm {

  public GnomeSort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Gnome Sort";
    alternativeSize = arrayController.getLength();
  }

  public GnomeSort(ArrayModel arrayController, int alternativeArrSize) {
    super(arrayController);
    this.name = "Gnome Sort";
    this.alternativeSize = alternativeArrSize;
  }

  public void sort() {
    report(name);

    int index = 0;
    while (index < arrayController.getLength() && !isCancelled()) {

      if (index == 0) index++;
      if (arrayController.get(index) >= arrayController.get(index - 1)) index++;
      else {
        arrayController.swap(index, index - 1);
        index--;

        delay(new int[] {index});
      }
      arrayController.addComparisons(1);
    }
  }
}
