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
 *
 * <p>Under equalize-sort-duration mode, {@link #setColumnStride(int)} may skip intermediate columns
 * so large arrays (e.g. 100k) can finish near the target time instead of doing O(n·max) bar
 * rewrites.
 */
public class GravitySort extends SortingAlgorithm {

  /** Cap on {@code n * max} abacus cells (~64 MiB of ints) before using the sparse path. */
  private static final long ABACUS_CELL_LIMIT = 16_000_000L;

  /** Visualize every Nth column (1 = every column). Set by equalize pacing for large n. */
  private int columnStride = 1;

  public GravitySort(ArrayModel arrayController) {
    super(arrayController);
    this.name = "Gravity Sort";
    alternativeSize = arrayController.getLength();
  }

  /**
   * How many {@link #delayFrame} beats this sort will fire for {@code model} at stride 1: one per
   * bead column ({@code max} value).
   */
  public static int estimateFrameBeats(ArrayModel model) {
    return Math.max(0, maxValue(model));
  }

  /** Number of visualized columns when sampling with {@code stride}. */
  public static int countVisualBeats(int maxColumns, int stride) {
    if (maxColumns <= 0) {
      return 0;
    }
    int s = Math.max(1, stride);
    int count = 0;
    int lastVis = -1;
    for (int col = s - 1; col < maxColumns; col += s) {
      count++;
      lastVis = col;
    }
    if (lastVis != maxColumns - 1) {
      count++;
    }
    return count;
  }

  /**
   * Stride for equalize mode: show every Nth column (and always the last). {@code 1} restores full
   * fidelity.
   */
  public void setColumnStride(int stride) {
    this.columnStride = Math.max(1, stride);
  }

  public int getColumnStride() {
    return columnStride;
  }

  private static int maxValue(ArrayModel model) {
    int n = model.getLength();
    if (n == 0) {
      return 0;
    }
    int max = model.get(0);
    for (int i = 1; i < n; i++) {
      int v = model.get(i);
      if (v > max) {
        max = v;
      }
    }
    return max;
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

      if (!shouldVisualizeColumn(i, max)) {
        continue;
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
   * int[n][max]}. Intermediate columns may be skipped when {@link #columnStride} &gt; 1.
   */
  private void sortSparse(int n, int max) {
    int[] original = new int[n];
    for (int i = 0; i < n; i++) {
      original[i] = arrayController.get(i);
      // O(1) instrumentation (avoid O(value) loops that are O(n²) on 0..n-1 data).
      if (original[i] > 0) {
        arrayController.addComparisons(original[i]);
        arrayController.addWritesAux(original[i]);
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

    int stride = columnStride;
    int lastVis = -1;
    for (int col = stride - 1; col < max && !isCancelled(); col += stride) {
      writeSparseColumn(n, max, original, colBeads, col);
      delayFrame(new int[] {n - col - 1});
      lastVis = col;
    }
    int last = max - 1;
    if (lastVis != last && !isCancelled()) {
      writeSparseColumn(n, max, original, colBeads, last);
      delayFrame(new int[] {n - last - 1});
    }
  }

  private void writeSparseColumn(int n, int max, int[] original, int[] colBeads, int col) {
    int unsettledCap = max - col - 1;
    for (int row = 0; row < n && !isCancelled(); row++) {
      int count = settledBeads(colBeads, col, n - row) + Math.min(original[row], unsettledCap);
      arrayController.set(row, count);
    }
  }

  private boolean shouldVisualizeColumn(int col, int maxColumns) {
    int stride = columnStride;
    if (stride <= 1) {
      return true;
    }
    if (col == maxColumns - 1) {
      return true;
    }
    return col % stride == stride - 1;
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
