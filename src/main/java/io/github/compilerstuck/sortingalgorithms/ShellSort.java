package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.visual.Marker;

public class ShellSort extends SortingAlgorithm {

  public ShellSort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Shell Sort";
    alternativeSize = arrayController.getLength();
  }

  public void sort() {
    report(name);

    int n = arrayController.getLength();

    for (int gap = n / 2; gap > 0 && !isCancelled(); gap /= 2) {

      for (int i = gap; i < n && !isCancelled(); i += 1) {
        int temp = arrayController.get(i);

        int j;
        for (j = i; j >= gap && arrayController.get(j - gap) > temp && !isCancelled(); j -= gap) {
          arrayController.set(j, arrayController.get(j - gap));
          arrayController.setMarker(j, Marker.SET);
          arrayController.addComparisons(1);
        }
        arrayController.addComparisons(1);
        arrayController.set(j, temp);

        delay(new int[] {i});
      }

      arrayController.setMarker(gap, Marker.SET);
    }
  }
}
