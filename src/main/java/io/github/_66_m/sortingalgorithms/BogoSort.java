package io.github._66_m.sortingalgorithms;

import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.visual.Marker;
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

  @Override
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
