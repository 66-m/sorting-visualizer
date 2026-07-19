package io.github.compilerstuck.control.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class TimeEstimateFormatTest {

  @Test
  void formatsNanosecondsAsMilliseconds() {
    assertEquals("1,50", TimeEstimateFormat.format(1_500_000.0));
    assertEquals("0,00", TimeEstimateFormat.format(0));
    assertEquals("1000,00", TimeEstimateFormat.format(1_000_000_000.0));
  }

  @Test
  void doesNotUseScientificNotation() {
    String formatted = TimeEstimateFormat.format(76_589_211_840_000.0);
    assertFalse(formatted.toUpperCase().contains("E"));
    assertEquals("76589211,84", formatted);
  }
}
