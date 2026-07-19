package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.visual.Marker;

public class QuickSortMiddlePivot extends SortingAlgorithm {

  public QuickSortMiddlePivot(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Quick Sort (Middle Pivot)";
    alternativeSize = arrayController.getLength();
  }

  @Override
  public void sort() {
    report(name);
    sort(arrayController, 0, arrayController.getLength() - 1);
  }

  private void sort(ArrayModel arrayController, int start, int end) {
    if (arrayController.getLength() == 0) {
      return;
    }

    if (start >= end) {
      return;
    }

    // pick the pivot
    int middle = start + (end - start) / 2;
    int pivot = arrayController.get(middle);

    // make left < pivot and right > pivot
    int i = start, j = end;
    while (i <= j && !isCancelled()) {
      while (arrayController.get(i) < pivot && !isCancelled()) {
        i++;
        arrayController.addComparisons(1);
      }
      arrayController.addComparisons(1);

      while (arrayController.get(j) > pivot && !isCancelled()) {
        j--;
        arrayController.addComparisons(1);
      }
      arrayController.addComparisons(1);

      if (i <= j) {
        arrayController.swap(i, j);
        arrayController.setMarker(i, Marker.SET);
        arrayController.setMarker(j, Marker.SET);
        i++;
        j--;
      }

      delay();
    }

    // recursively sort two sub parts
    if (start < j && !isCancelled()) sort(arrayController, start, j);

    if (end > i && !isCancelled()) sort(arrayController, i, end);
  }
}
