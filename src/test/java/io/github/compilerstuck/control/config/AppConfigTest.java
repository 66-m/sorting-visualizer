package io.github.compilerstuck.control.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppConfigTest {

  @Test
  void shuffleStepsForDeltaTargetsOneSecondBudget() {
    assertEquals(1, AppConfig.shuffleStepsForDelta(0f));
    assertEquals(1, AppConfig.shuffleStepsForDelta(-1f));
    assertEquals(
        AppConfig.SHUFFLE_VISUAL_STEPS,
        AppConfig.shuffleStepsForDelta(AppConfig.SHUFFLE_DURATION_SEC));
    assertEquals(
        AppConfig.SHUFFLE_VISUAL_STEPS / 2,
        AppConfig.shuffleStepsForDelta(AppConfig.SHUFFLE_DURATION_SEC / 2f));
  }

  @Test
  void equalizedSortStepsForDeltaMatchesBudget() {
    assertEquals(1, AppConfig.equalizedSortStepsForDelta(0f, 1000, 10f));
    assertEquals(1, AppConfig.equalizedSortStepsForDelta(1f, 0, 10f));
    assertEquals(100, AppConfig.equalizedSortStepsForDelta(1f, 1000, 10f));
    assertEquals(50, AppConfig.equalizedSortStepsForDelta(0.5f, 1000, 10f));
  }

  @Test
  void effectiveEqualizeTargetRespectsFrameFloor() {
    assertEquals(0f, AppConfig.equalizeFrameFloorSec(0));
    assertEquals(2f, AppConfig.equalizeFrameFloorSec(120), 1e-4f);
    assertEquals(10f, AppConfig.effectiveEqualizeTargetSec(10f, 0));
    assertEquals(10f, AppConfig.effectiveEqualizeTargetSec(10f, 120));
    assertEquals(21.3333f, AppConfig.effectiveEqualizeTargetSec(10f, 1280), 1e-3f);
  }

  @Test
  void equalizeMaxFrameBeatsScalesWithArrayLength() {
    assertEquals(2, AppConfig.equalizeMaxFrameBeatsPerFrame(100_000));
    assertEquals(64, AppConfig.equalizeMaxFrameBeatsPerFrame(1_000));
    assertEquals(1, AppConfig.equalizeMaxFrameBeatsPerFrame(1_000_000));
  }
}
