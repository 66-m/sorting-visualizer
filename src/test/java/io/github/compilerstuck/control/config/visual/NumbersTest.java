package io.github.compilerstuck.control.config.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NumbersTest {

  @Test
  void clampBoundsValue() {
    assertEquals(0.5, Numbers.clamp(0.5, 0, 1));
    assertEquals(0.0, Numbers.clamp(-1, 0, 1));
    assertEquals(1.0, Numbers.clamp(2, 0, 1));
  }

  @Test
  void clampIntBoundsValue() {
    assertEquals(5, Numbers.clampInt(5, 0, 10));
    assertEquals(0, Numbers.clampInt(-3, 0, 10));
    assertEquals(10, Numbers.clampInt(99, 0, 10));
  }

  @Test
  void clampRoundedIntRoundsThenClamps() {
    assertEquals(1, Numbers.clampRoundedInt(0.6, 0, 2));
    assertEquals(0, Numbers.clampRoundedInt(-0.4, 0, 2));
    assertEquals(2, Numbers.clampRoundedInt(9.9, 0, 2));
  }
}
