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
}
