package io.github.compilerstuck.control.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Drift guard: {@link SettingsDefaults} must keep the numeric values that were previously hardcoded
 * across UserPreferences / AppConfig / AppContext.
 */
class SettingsDefaultsTest {

  @Test
  void defaultsMatchPreviouslyHardcodedValues() {
    assertEquals("quicksort-middle", SettingsDefaults.DEFAULT_ALGORITHM_ID);
    assertEquals("bars", SettingsDefaults.DEFAULT_VISUALIZATION_ID);
    assertEquals(1280, SettingsDefaults.DEFAULT_ARRAY_SIZE);
    assertEquals(5, SettingsDefaults.DEFAULT_SPEED_LEVEL);
    assertFalse(SettingsDefaults.DEFAULT_MUTED);
    assertFalse(SettingsDefaults.DEFAULT_PERF_STATS);
    assertFalse(SettingsDefaults.DEFAULT_FIVE_SECOND_START_DELAY);
    assertEquals("{}", SettingsDefaults.DEFAULT_VISUAL_SETTINGS_BY_ID);

    assertEquals(3, SettingsDefaults.ARRAY_SIZE_MIN);
    assertEquals(100_000, SettingsDefaults.ARRAY_SIZE_MAX);
    assertEquals(24, SettingsDefaults.ARRAY_SIZE_FPS_WARNING_THRESHOLD);
    assertEquals(20_000, SettingsDefaults.ARRAY_SIZE_HIGH_WARNING_THRESHOLD);
    assertEquals(1, SettingsDefaults.SPEED_LEVEL_MIN);
    assertEquals(10, SettingsDefaults.SPEED_LEVEL_MAX);

    assertEquals(1, SettingsDefaults.stepsPerFrame(1));
    assertEquals(2, SettingsDefaults.stepsPerFrame(2));
    assertEquals(5, SettingsDefaults.stepsPerFrame(3));
    assertEquals(12, SettingsDefaults.stepsPerFrame(4));
    assertEquals(25, SettingsDefaults.stepsPerFrame(5));
    assertEquals(50, SettingsDefaults.stepsPerFrame(6));
    assertEquals(100, SettingsDefaults.stepsPerFrame(7));
    assertEquals(250, SettingsDefaults.stepsPerFrame(8));
    assertEquals(750, SettingsDefaults.stepsPerFrame(9));
    assertEquals(2000, SettingsDefaults.stepsPerFrame(10));

    assertTrue(SettingsDefaults.DEFAULT_EQUALIZE_SORT_DURATION);
    assertEquals(CanvasBackground.DARK, SettingsDefaults.DEFAULT_CANVAS_BACKGROUND);
    assertEquals(30f, SettingsDefaults.equalizedDurationSec(1));
    assertEquals(10f, SettingsDefaults.equalizedDurationSec(5));
    assertEquals(2f, SettingsDefaults.equalizedDurationSec(10));

    assertEquals("1", SettingsDefaults.speedTickLabel(1, false));
    assertEquals("25", SettingsDefaults.speedTickLabel(5, false));
    assertEquals("2k", SettingsDefaults.speedTickLabel(10, false));
    assertEquals("30s", SettingsDefaults.speedTickLabel(1, true));
    assertEquals("10s", SettingsDefaults.speedTickLabel(5, true));
    assertEquals("2s", SettingsDefaults.speedTickLabel(10, true));
  }

  @Test
  void clampsMatchPreviousRanges() {
    assertEquals(3, SettingsDefaults.clampArraySize(0));
    assertEquals(100_000, SettingsDefaults.clampArraySize(999_999));
    assertEquals(1280, SettingsDefaults.clampArraySize(1280));

    assertEquals(1, SettingsDefaults.clampSpeedLevel(0));
    assertEquals(10, SettingsDefaults.clampSpeedLevel(99));
    assertEquals(5, SettingsDefaults.clampSpeedLevel(5));
  }

  @Test
  void legacyFacadesDelegateToSettingsDefaults() {
    assertEquals(SettingsDefaults.DEFAULT_ALGORITHM_ID, UserPreferences.DEFAULT_ALGORITHM_ID);
    assertEquals(SettingsDefaults.DEFAULT_SPEED_LEVEL, UserPreferences.DEFAULT_SPEED_LEVEL);
  }
}
