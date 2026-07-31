package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.model.ArrayModel;

/**
 * Gravity (bead) sort. Beads are placed on an abacus and fall column-by-column.
 *
 * <p>Uses the classic {@code int[n][max]} abacus when it fits in a modest heap budget. Falls back
 * to an O(n + max) simulation when {@code n * max} would risk {@link OutOfMemoryError}.
 *
 * <p>Each settled column ends with {@link #delayFrame} so the snapshot shows every bar updating
 * together — the FrameGate steps-per-frame budget would otherwise skip many columns per draw.
 */
public class GravitySort extends SortingAlgorithm {

  /** Cap on {@code n * max} abacus cells (~64 MiB of ints) before using the sparse path. */
  private static final long ABACUS_CELL_LIMIT = 16_000_000L;

  public GravitySort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Gravity Sort";
    alternativeSize = arrayController.getLength();
  }

  @Override
  public void sort() {
    report(name);

    int n = arrayController.getLength();
    if (n == 0) {
      return;
    }

    int max = arrayController.get(0);
    for (int i = 1; i < n && !isCancelled(); i++) {
      if (arrayController.get(i) > max) {
        max = arrayController.get(i);
        arrayController.addComparisons(1);
      }
    }

    if (max <= 0) {
      return;
    }

    if ((long) n * (long) max <= ABACUS_CELL_LIMIT) {
      sortWithAbacus(n, max);
    } else {
      sortSparse(n, max);
    }
  }

  /** Original abacus implementation (same control flow as on {@code main}). */
  private void sortWithAbacus(int n, int max) {
    int[][] abacus = new int[n][max];
    for (int i = 0; i < n && !isCancelled(); i++) {
      for (int j = 0; j < arrayController.get(i); j++) {
        arrayController.addComparisons(1);
        abacus[i][abacus[0].length - j - 1] = 1;
        arrayController.addWritesAux(1);
      }
    }
    // apply gravity
    for (int i = 0; i < abacus[0].length && !isCancelled(); i++) {
      for (int j = 0; j < abacus.length && !isCancelled(); j++) {
        if (abacus[j][i] == 1) {
          // Drop it
          int droppos = j;
          while (droppos + 1 < abacus.length && abacus[droppos][i] == 1) {
            droppos++;
          }
          if (abacus[droppos][i] == 0) {
            abacus[j][i] = 0;
            abacus[droppos][i] = 1;
            arrayController.addWritesAux(2);
          }
        }
      }

      for (int x = 0; x < abacus.length && !isCancelled(); x++) {
        int count = 0;
        for (int y = 0; y < abacus[0].length; y++) {
          count += abacus[x][y];
        }
        arrayController.set(x, count);
      }

      delayFrame(new int[] {n - i - 1});
    }
  }

  /**
   * Memory-safe path: same per-column bar heights as the abacus recount, without allocating {@code
   * int[n][max]}.
   */
  private void sortSparse(int n, int max) {
    int[] original = new int[n];
    for (int i = 0; i < n; i++) {
      original[i] = arrayController.get(i);
      for (int j = 0; j < original[i]; j++) {
        arrayController.addComparisons(1);
        arrayController.addWritesAux(1);
      }
    }

    int[] colBeads = new int[max];
    for (int i = 0; i < n && !isCancelled(); i++) {
      int value = original[i];
      if (value > 0) {
        colBeads[max - value]++;
      }
    }
    for (int col = 1; col < max; col++) {
      colBeads[col] += colBeads[col - 1];
    }

    for (int col = 0; col < max && !isCancelled(); col++) {
      int unsettledCap = max - col - 1;
      for (int row = 0; row < n && !isCancelled(); row++) {
        int count = settledBeads(colBeads, col, n - row) + Math.min(original[row], unsettledCap);
        arrayController.set(row, count);
      }
      delayFrame(new int[] {n - col - 1});
    }
  }

  private static int settledBeads(int[] colBeads, int col, int need) {
    int lo = 0;
    int hi = col + 1;
    while (lo < hi) {
      int mid = (lo + hi) >>> 1;
      if (colBeads[mid] >= need) {
        hi = mid;
      } else {
        lo = mid + 1;
      }
    }
    return lo <= col ? col - lo + 1 : 0;
  }
}
