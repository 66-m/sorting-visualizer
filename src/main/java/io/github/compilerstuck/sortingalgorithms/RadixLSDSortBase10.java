package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;
import java.util.ArrayList;
import java.util.List;

public class RadixLSDSortBase10 extends SortingAlgorithm {

  int RADIX = 10;

  public RadixLSDSortBase10(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Radix Sort (LSD) (Base " + RADIX + ")";
    alternativeSize = arrayController.getLength();
  }

  public RadixLSDSortBase10(ArrayModel arrayController, int radix_base) {
    super(arrayController);
    RADIX = radix_base;
    this.name = "Radix Sort (LSD) (Base " + RADIX + ")";
  }

  public void sort() {
    report(name);
    startTime = System.nanoTime();

    @SuppressWarnings("unchecked")
    List<Integer>[] bucket = new List[RADIX];
    for (int i = 0; i < bucket.length && !isCancelled(); i++) {
      bucket[i] = new ArrayList<>();
    }
    boolean maxLength = false;
    int tmp, placement = 1;
    while (!maxLength && !isCancelled()) {
      maxLength = true;
      for (Integer i : arrayController.getArray()) {
        tmp = i / placement;
        bucket[tmp % RADIX].add(i);
        if (maxLength && tmp > 0) {
          maxLength = false;
        }
      }

      int[] buckA = new int[RADIX];
      for (int i = 0; i < RADIX && !isCancelled(); i++) {
        for (int j = i - 1; j >= 0 && !isCancelled(); j--) {
          buckA[i] += bucket[j].size();
        }
      }

      for (int i = 0; i < arrayController.getLength() && !isCancelled(); i++) {
        for (int j = 0; j < RADIX && !isCancelled(); j++) {
          if (bucket[j].size() <= i) {
            bucket[j].clear();
            continue;
          } else {
            arrayController.set(buckA[j]++, bucket[j].get(i));
          }

          delay(new int[] {buckA[j] - 1});
        }
      }

      placement *= RADIX;
    }

    arrayController.addRealTime(System.nanoTime() - startTime);
  }
}
