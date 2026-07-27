package io.github.compilerstuck.control.catalog;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VisualConstraintsTest {

  @Test
  @DisplayName("fitSize leaves unconstrained sizes clamped only")
  void unconstrainedOnlyClamps() {
    assertEquals(100, VisualConstraints.NONE.fitSize(100, 3, 20000));
    assertEquals(3, VisualConstraints.NONE.fitSize(1, 3, 20000));
    assertEquals(20000, VisualConstraints.NONE.fitSize(50000, 3, 20000));
  }

  @Test
  @DisplayName("fitSize keeps valid perfect squares")
  void keepsPerfectSquare() {
    assertEquals(1024, VisualConstraints.SQUARE.fitSize(1024, 3, 20000));
  }

  @Test
  @DisplayName("fitSize picks nearest perfect square and clips ties downward")
  void nearestSquarePrefersClipOnTie() {
    // 12^2=144 and 13^2=169: |156-144|=12, |156-169|=13 → 144 (clip on near-ties)
    assertEquals(144, VisualConstraints.SQUARE.fitSize(156, 3, 20000));
    // closer to upper
    assertEquals(169, VisualConstraints.SQUARE.fitSize(160, 3, 20000));
  }

  @Test
  @DisplayName("fitSize picks nearest perfect cube within max")
  void nearestCubeRespectsMax() {
    assertEquals(8000, VisualConstraints.CUBE.fitSize(7500, 3, 20000));
    // 28^3 = 21952 exceeds max → 27^3 = 19683
    assertEquals(19683, VisualConstraints.CUBE.fitSize(20000, 3, 20000));
  }

  @Test
  @DisplayName("fitSize for image visuals only clamps")
  void imageOnlyClamps() {
    assertEquals(999, VisualConstraints.IMAGE.fitSize(999, 3, 20000));
  }
}
