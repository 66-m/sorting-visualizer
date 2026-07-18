package io.github.compilerstuck.control.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * Drift guard: {@link SettingsDefaults} must keep the numeric values that were previously hardcoded
 * across UserPreferences / MainControllerConfig / AppContext.
 */
class SettingsDefaultsTest {

  @Test
  void defaultsMatchPreviouslyHardcodedValues() {
    assertEquals("quicksort-middle", SettingsDefaults.DEFAULT_ALGORITHM_ID);
    assertEquals("bars", SettingsDefaults.DEFAULT_VISUALIZATION_ID);
    assertEquals(1280, SettingsDefaults.DEFAULT_ARRAY_SIZE);
    assertEquals(3, SettingsDefaults.DEFAULT_SPEED_LEVEL);
    assertFalse(SettingsDefaults.DEFAULT_USE_STEP_ENGINE);
    assertFalse(SettingsDefaults.DEFAULT_MUTED);

    assertEquals(3, SettingsDefaults.ARRAY_SIZE_MIN);
    assertEquals(20_000, SettingsDefaults.ARRAY_SIZE_MAX);
    assertEquals(1, SettingsDefaults.SPEED_LEVEL_MIN);
    assertEquals(5, SettingsDefaults.SPEED_LEVEL_MAX);

    assertArrayEquals(new int[] {50, 10, 1, 1, 1}, SettingsDefaults.DELAY_TIME);
    assertArrayEquals(new double[] {1.0, 1.0, 1.0, 0.12, 0.02}, SettingsDefaults.DELAY_FACTOR);
    assertArrayEquals(new int[] {1, 5, 25, 200, 2000}, SettingsDefaults.STEPS_PER_FRAME);
  }

  @Test
  void clampsMatchPreviousRanges() {
    assertEquals(3, SettingsDefaults.clampArraySize(0));
    assertEquals(20_000, SettingsDefaults.clampArraySize(99_999));
    assertEquals(1280, SettingsDefaults.clampArraySize(1280));

    assertEquals(1, SettingsDefaults.clampSpeedLevel(0));
    assertEquals(5, SettingsDefaults.clampSpeedLevel(9));
    assertEquals(3, SettingsDefaults.clampSpeedLevel(3));
  }

  @Test
  void legacyFacadesDelegateToSettingsDefaults() {
    assertEquals(SettingsDefaults.DEFAULT_ARRAY_SIZE, MainControllerConfig.DEFAULT_ARRAY_SIZE);
    assertEquals(SettingsDefaults.DEFAULT_ARRAY_SIZE, UserPreferences.DEFAULT_ARRAY_SIZE);
    assertEquals(SettingsDefaults.DEFAULT_ALGORITHM_ID, UserPreferences.DEFAULT_ALGORITHM_ID);
    assertEquals(SettingsDefaults.DEFAULT_SPEED_LEVEL, UserPreferences.DEFAULT_SPEED_LEVEL);
  }
}
