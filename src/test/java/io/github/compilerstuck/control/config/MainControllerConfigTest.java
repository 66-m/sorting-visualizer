package io.github.compilerstuck.control.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MainControllerConfigTest {

  @Test
  void shuffleStepsForDeltaTargetsOneSecondBudget() {
    assertEquals(1, MainControllerConfig.shuffleStepsForDelta(0f));
    assertEquals(1, MainControllerConfig.shuffleStepsForDelta(-1f));
    assertEquals(
        MainControllerConfig.SHUFFLE_VISUAL_STEPS,
        MainControllerConfig.shuffleStepsForDelta(MainControllerConfig.SHUFFLE_DURATION_SEC));
    assertEquals(
        MainControllerConfig.SHUFFLE_VISUAL_STEPS / 2,
        MainControllerConfig.shuffleStepsForDelta(MainControllerConfig.SHUFFLE_DURATION_SEC / 2f));
  }
}
