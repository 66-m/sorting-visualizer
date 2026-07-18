package io.github.compilerstuck.control.catalog;

import java.util.OptionalInt;

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

  /**
   * Proposes a valid array size for the given current size, or empty if the current size already
   * satisfies (or has no) constraints.
   */
  public OptionalInt proposeSize(int current) {
    if (requiresPerfectCube && !isPerfectCube(current)) {
      return OptionalInt.of(nextPerfectCube(current));
    }
    if (requiresPerfectSquare && !isPerfectSquare(current)) {
      return OptionalInt.of(nextPerfectSquare(current));
    }
    return OptionalInt.empty();
  }
}
