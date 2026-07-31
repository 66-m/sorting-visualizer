package io.github.compilerstuck.control.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.config.AppConfig;
import org.junit.jupiter.api.Test;

class EqualizePacingTest {

  @Test
  void beginWithZeroStepsLeavesInactive() {
    EqualizePacing pacing = new EqualizePacing();
    pacing.begin(0, 0, 10f);
    assertFalse(pacing.isActive());
    assertEquals(-1, pacing.stepsForDelta(1f / 60f));
  }

  @Test
  void stepsForDeltaUsesRemainingBudget() {
    EqualizePacing pacing = new EqualizePacing();
    pacing.begin(600, 0, 10f);
    assertTrue(pacing.isActive());
    // 600 steps / 10s → 60 steps/s → ~1 step per 1/60s frame
    assertEquals(1, pacing.stepsForDelta(1f / 60f));
    for (int i = 0; i < 300; i++) {
      pacing.recordStep();
    }
    // Half consumed; still ~1/frame if on schedule
    assertEquals(1, pacing.stepsForDelta(1f / 60f));
  }

  @Test
  void shortTargetWithFrameBeatsEnablesBatching() {
    EqualizePacing pacing = new EqualizePacing();
    // 120 beats → floor 2s; slider 1s → must batch
    pacing.begin(120, 120, 1f, 120);
    assertTrue(pacing.batchBeats());
    assertEquals(1f, pacing.effectiveTargetSec(), 1e-4f);
    // Uncapped: 120 steps / 1s → 2 per 1/60s frame; n=120 allows up to 64
    assertEquals(2, pacing.stepsForDelta(1f / 60f));
  }

  @Test
  void largeArrayCapsBatchedBeatsPerFrame() {
    EqualizePacing pacing = new EqualizePacing();
    pacing.begin(100_000, 100_000, 2f, 100_000);
    assertTrue(pacing.batchBeats());
    assertEquals(2, pacing.maxStepsPerFrame()); // 250_000 / 100_000
    // Schedule wants huge grants; cap keeps frames responsive
    assertEquals(2, pacing.stepsForDelta(1f / 60f));
  }

  @Test
  void longTargetStretchesWithExtraFrameWaits() {
    EqualizePacing pacing = new EqualizePacing();
    // 60 beats → floor 1s; slider 2s → ~2 frames per beat on average
    pacing.begin(60, 60, 2f);
    assertFalse(pacing.batchBeats());
    int extras = 0;
    for (int i = 0; i < 60; i++) {
      extras += pacing.takeExtraFrameWaits();
    }
    // 2s * 60fps = 120 frames total → 60 beats + ~60 extras
    assertEquals(60, extras);
  }

  @Test
  void frameFloorHelperUnchanged() {
    assertEquals(2f, AppConfig.equalizeFrameFloorSec(120), 1e-4f);
    assertEquals(2f, AppConfig.effectiveEqualizeTargetSec(1f, 120), 1e-4f);
    assertEquals(2, AppConfig.equalizeMaxFrameBeatsPerFrame(100_000));
    assertEquals(64, AppConfig.equalizeMaxFrameBeatsPerFrame(1_000));
  }
}
