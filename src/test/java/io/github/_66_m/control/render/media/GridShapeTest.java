package io.github._66_m.control.render.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GridShapeTest {

  @Test
  void coversEveryTileWithRoughlySquareCells() {
    for (int n : new int[] {1, 2, 7, 100, 600, 1000, 99_999}) {
      GridShape g = GridShape.forCount(n, 16 / 9.0);
      assertTrue(g.cols() * g.rows() >= n, "n=" + n);
      assertTrue(g.cols() * (g.rows() - 1) < n, "no empty row, n=" + n);
    }
  }

  @Test
  void wideFramesGetMoreColumnsThanRows() {
    GridShape g = GridShape.forCount(900, 16 / 9.0);
    assertTrue(g.cols() > g.rows());
    assertEquals(new GridShape(1, 1), GridShape.forCount(0, 1.0));
  }
}
