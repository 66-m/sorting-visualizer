package io.github.compilerstuck.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LodStrideTest {

  @Test
  @DisplayName("forLength returns 1 when length fits in maxPrimitives")
  void strideIsOneWhenLengthFits() {
    assertEquals(1, LodStride.forLength(100, 200));
    assertEquals(1, LodStride.forLength(50, 50));
    assertEquals(1, LodStride.forLength(1, 1));
  }

  @Test
  @DisplayName("forLength returns 1 for non-positive inputs")
  void strideIsOneForNonPositive() {
    assertEquals(1, LodStride.forLength(0, 10));
    assertEquals(1, LodStride.forLength(10, 0));
    assertEquals(1, LodStride.forLength(-5, 10));
    assertEquals(1, LodStride.forLength(10, -1));
  }

  @Test
  @DisplayName("forLength ceil-divides when length exceeds maxPrimitives")
  void strideCeilDivides() {
    assertEquals(2, LodStride.forLength(100, 50));
    assertEquals(3, LodStride.forLength(100, 40));
    assertEquals(5, LodStride.forLength(1000, 200));
    assertEquals(10, LodStride.forLength(100, 10));
  }
}
