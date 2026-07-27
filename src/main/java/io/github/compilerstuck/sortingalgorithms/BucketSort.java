package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;
import java.util.Arrays;

public class BucketSort extends SortingAlgorithm {

  public BucketSort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Bucket Sort";
    alternativeSize = arrayController.getLength();
  }

  @Override
  public void sort() {
    report(name);

    int max = Arrays.stream(arrayController.getArray()).max().getAsInt();
    int[] bucket = new int[max + 1];
    for (int i = 0; i <= max && !isCancelled(); i++) {
      bucket[i] = 0;
      arrayController.addWritesAux(1);
    }

    for (int i = 0; i < arrayController.getLength() && !isCancelled(); i++) {
      bucket[arrayController.get(i)]++;
      arrayController.addWritesAux(1);

      delay(new int[] {i});
    }

    for (int i = 0, j = 0; i <= max && !isCancelled(); i++) {
      while (bucket[i] > 0) {
        arrayController.addComparisons(1);
        arrayController.set(j++, i);
        bucket[i]--;
        arrayController.addWritesAux(1);

        delay(new int[] {j - 1});
      }
    }
  }
}
