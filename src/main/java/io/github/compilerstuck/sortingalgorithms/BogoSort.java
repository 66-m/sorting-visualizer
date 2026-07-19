package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.visual.Marker;
import java.util.Random;

public class BogoSort extends SortingAlgorithm {

  long trycnt = 0;

  public BogoSort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Bogo Sort";
    alternativeSize = arrayController.getLength();
    selected = false;
  }

  public BogoSort(ArrayModel arrayController, int alternativeSize) {
    super(arrayController);
    this.name = "Bogo Sort";
    this.alternativeSize = alternativeSize;
  }

  public void sort() {
    trycnt = 0;
    report(name);
    Random r = new Random();
    while (!arrayController.isSorted() && !isCancelled()) {

      int a = r.nextInt(arrayController.getLength());
      int b = r.nextInt(arrayController.getLength());

      arrayController.swap(a, b);

      arrayController.setMarker(a, Marker.SET);
      delay();

      trycnt++;
      report("Bogo Sort (Tries: " + trycnt + ")");
    }
  }
}
