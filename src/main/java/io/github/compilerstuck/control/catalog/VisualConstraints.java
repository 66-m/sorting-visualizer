package io.github.compilerstuck.control.catalog;

/** Describes the array-size constraints a visualization imposes, if any. */
public record VisualConstraints(
    boolean requiresPerfectSquare, boolean requiresPerfectCube, boolean requiresImage) {

  public static final VisualConstraints NONE = new VisualConstraints(false, false, false);
  public static final VisualConstraints SQUARE = new VisualConstraints(true, false, false);
  public static final VisualConstraints CUBE = new VisualConstraints(false, true, false);
  public static final VisualConstraints IMAGE = new VisualConstraints(false, false, true);

  public static boolean isPerfectSquare(int n) {
    if (n < 0) return false;
    int root = (int) Math.round(Math.sqrt(n));
    return root * root == n;
  }

  public static boolean isPerfectCube(int n) {
    if (n < 0) return false;
    int root = (int) Math.round(Math.cbrt(n));
    return root * root * root == n;
  }

  public static int nextPerfectSquare(int n) {
    int root = (int) Math.ceil(Math.sqrt(Math.max(n, 0)));
    if (root < 1) root = 1;
    while (root * root < n) {
      root++;
    }
    return root * root;
  }

  public static int nextPerfectCube(int n) {
    int root = (int) Math.ceil(Math.cbrt(Math.max(n, 0)));
    if (root < 1) root = 1;
    while (root * root * root < n) {
      root++;
    }
    return root * root * root;
  }

  public static int previousPerfectSquare(int n) {
    int root = (int) Math.floor(Math.sqrt(Math.max(n, 0)));
    if (root < 1) {
      return 1;
    }
    return root * root;
  }

  public static int previousPerfectCube(int n) {
    int root = (int) Math.floor(Math.cbrt(Math.max(n, 0)));
    if (root < 1) {
      return 1;
    }
    return root * root * root;
  }

  /**
   * Returns {@code current} if it already satisfies constraints; otherwise the nearest valid size
   * clamped to {@code [minSize, maxSize]}. Ties prefer the smaller (clipped) size. Image visuals
   * and unconstrained visuals only clamp to the range.
   */
  public int fitSize(int current, int minSize, int maxSize) {
    int lo = Math.min(minSize, maxSize);
    int hi = Math.max(minSize, maxSize);
    int clamped = Math.max(lo, Math.min(hi, current));

    if (requiresImage || (!requiresPerfectSquare && !requiresPerfectCube)) {
      return clamped;
    }

    if (requiresPerfectCube) {
      return nearestPerfectCube(clamped, lo, hi);
    }
    return nearestPerfectSquare(clamped, lo, hi);
  }

  private static int nearestPerfectSquare(int n, int minSize, int maxSize) {
    if (isPerfectSquare(n) && n >= minSize && n <= maxSize) {
      return n;
    }
    int lower = previousPerfectSquare(n);
    int upper = nextPerfectSquare(n);
    return pickNearestInRange(n, lower, upper, minSize, maxSize, true);
  }

  private static int nearestPerfectCube(int n, int minSize, int maxSize) {
    if (isPerfectCube(n) && n >= minSize && n <= maxSize) {
      return n;
    }
    int lower = previousPerfectCube(n);
    int upper = nextPerfectCube(n);
    return pickNearestInRange(n, lower, upper, minSize, maxSize, false);
  }

  private static int pickNearestInRange(
      int target, int lower, int upper, int minSize, int maxSize, boolean square) {
    Integer bestLower = null;
    Integer bestUpper = null;

    for (int candidate = lower; candidate >= 1; ) {
      if (candidate <= maxSize && candidate >= minSize) {
        bestLower = candidate;
        break;
      }
      if (candidate < minSize) {
        break;
      }
      candidate =
          square ? previousPerfectSquare(candidate - 1) : previousPerfectCube(candidate - 1);
    }

    for (int candidate = upper; ; ) {
      if (candidate <= maxSize && candidate >= minSize) {
        bestUpper = candidate;
        break;
      }
      if (candidate > maxSize) {
        candidate = square ? previousPerfectSquare(maxSize) : previousPerfectCube(maxSize);
        if (candidate >= minSize && candidate <= maxSize) {
          bestUpper = candidate;
        }
        break;
      }
      candidate = square ? nextPerfectSquare(candidate + 1) : nextPerfectCube(candidate + 1);
    }

    if (bestLower == null && bestUpper == null) {
      // Fallback: smallest valid size in range
      return square ? nextPerfectSquare(minSize) : nextPerfectCube(minSize);
    }
    if (bestLower == null) {
      return bestUpper;
    }
    if (bestUpper == null) {
      return bestLower;
    }
    int dLower = Math.abs(target - bestLower);
    int dUpper = Math.abs(target - bestUpper);
    // Prefer smaller on ties ("clip").
    return dUpper < dLower ? bestUpper : bestLower;
  }
}
