package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;

public class AmericanFlagSort extends SortingAlgorithm {

  public AmericanFlagSort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "American Flag Sort";
    alternativeSize = arrayController.getLength();
  }

  public AmericanFlagSort(ArrayModel arrayController, int alternativeSize) {
    super(arrayController);
    this.name = "American Flag Sort";
    this.alternativeSize = alternativeSize;
  }

  public void sort() {
    report(name);

    final int M = arrayController.getLength();

    int[] count = new int[M];
    for (int num : arrayController.getArray()) {
      count[num % M]++;
      arrayController.addWritesAux(1);
    }
    int[] start = new int[M];
    for (int i = 1; i < M && !isCancelled(); i++) {
      start[i] = start[i - 1] + count[i - 1];
      arrayController.addWritesAux(1);
    }
    for (int b = 0; b < M && !isCancelled(); b++) {
      while (count[b] > 0) {
        int origin = start[b];
        int from = origin;
        int num = arrayController.get(from);
        arrayController.set(from, 0);
        do {
          int to = start[num % M]++;
          count[num % M]--;
          int temp = arrayController.get(to);
          arrayController.set(to, num);

          num = temp;
          from = to;

          delay(new int[] {to});

        } while (from != origin && !isCancelled());
      }
    }
  }
}
