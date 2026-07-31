package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;

public class QuickSortDualPivot extends SortingAlgorithm {

  public QuickSortDualPivot(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Quick Sort (Dual Pivot)";
    alternativeSize = arrayController.getLength();
  }

  public QuickSortDualPivot(ArrayModel arrayController, int alternativeSize) {
    super(arrayController);
    this.name = "Quick Sort (Dual Pivot)";
    this.alternativeSize = alternativeSize;
  }

  @Override
  public void sort() {
    report(name);

    sort(arrayController, 0, arrayController.getLength() - 1);
  }

  /**
   * Dual-pivot quicksort. Recurses on the two smaller partitions and iterates on the largest so
   * stack depth stays O(log n) even when outermost pivots degenerate (sorted / reverse input).
   */
  private void sort(ArrayModel arrayController, int left, int right) {
    while (right > left && !isCancelled()) {
      // Choose outermost elements as pivots
      if (arrayController.get(left) > arrayController.get(right)) {
        arrayController.swap(left, right);

        delay(new int[] {left, right});
      }
      arrayController.addComparisons(1);
      int p = arrayController.get(left), q = arrayController.get(right);

      // Partition A according to invariant below
      int l = left + 1, g = right - 1, k = l;
      while (k <= g && !isCancelled()) {
        if (arrayController.get(k) < p) {
          arrayController.swap(k, l);

          delay(new int[] {k, l});

          ++l;
        } else if (arrayController.get(k) >= q) {
          while (arrayController.get(g) > q && k < g && !isCancelled()) {
            --g;
            arrayController.addComparisons(1);
          }
          arrayController.addComparisons(1);
          arrayController.swap(k, g);

          delay(new int[] {k, g});

          --g;
          if (arrayController.get(k) < p) {
            arrayController.swap(k, l);

            delay(new int[] {k, l});

            ++l;
          }
          arrayController.addComparisons(1);
        }
        arrayController.addComparisons(1);
        ++k;
      }
      --l;
      ++g;

      // Swap pivots to final place
      arrayController.swap(left, l);
      arrayController.swap(right, g);

      delay(new int[] {left, right, l, g});

      int leftLo = left;
      int leftHi = l - 1;
      int midLo = l + 1;
      int midHi = g - 1;
      int rightLo = g + 1;
      int rightHi = right;

      int leftLen = Math.max(0, leftHi - leftLo + 1);
      int midLen = Math.max(0, midHi - midLo + 1);
      int rightLen = Math.max(0, rightHi - rightLo + 1);

      // Recurse into the two smaller segments; continue the loop on the largest.
      if (leftLen >= midLen && leftLen >= rightLen) {
        if (midLen > 0) {
          sort(arrayController, midLo, midHi);
        }
        if (rightLen > 0) {
          sort(arrayController, rightLo, rightHi);
        }
        right = leftHi;
      } else if (midLen >= rightLen) {
        if (leftLen > 0) {
          sort(arrayController, leftLo, leftHi);
        }
        if (rightLen > 0) {
          sort(arrayController, rightLo, rightHi);
        }
        left = midLo;
        right = midHi;
      } else {
        if (leftLen > 0) {
          sort(arrayController, leftLo, leftHi);
        }
        if (midLen > 0) {
          sort(arrayController, midLo, midHi);
        }
        left = rightLo;
      }
    }
  }
}
