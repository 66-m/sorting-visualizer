package io.github.compilerstuck.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DisplayBoundsTest {

  @Test
  void defaultOnSingleMonitorUsesPrimary() {
    assertEquals(-1, DisplayBounds.resolveListIndex(0, 1));
  }

  @Test
  void defaultOnMultiMonitorPrefersSecondary() {
    assertEquals(1, DisplayBounds.resolveListIndex(0, 2));
  }

  @Test
  void explicitOneBasedIndex() {
    assertEquals(0, DisplayBounds.resolveListIndex(1, 2));
    assertEquals(1, DisplayBounds.resolveListIndex(2, 2));
  }

  @Test
  void outOfRangeFallsBackToPrimary() {
    assertEquals(-1, DisplayBounds.resolveListIndex(3, 2));
  }
}
