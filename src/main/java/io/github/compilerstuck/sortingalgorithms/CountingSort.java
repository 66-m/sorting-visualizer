package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;
import java.util.Arrays;

public class CountingSort extends SortingAlgorithm {

  public CountingSort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Counting Sort";
    alternativeSize = arrayController.getLength();
  }

  @Override
  public void sort() {
    report(name);
    int max = Arrays.stream(arrayController.getArray()).max().getAsInt();
    int[] counter = new int[max + 1];
    for (int i : arrayController.getArray()) {
      counter[i]++;
      arrayController.addWritesAux(1);

      delay(new int[] {i});
    }

    int ndx = 0;
    for (int i = 0; i < counter.length && !isCancelled(); i++) {
      while (0 < counter[i]) {
        arrayController.addComparisons(1);

        delay(new int[] {ndx});

        arrayController.set(ndx++, i);
        counter[i]--;
        arrayController.addWritesAux(1);
      }
    }
  }
}
