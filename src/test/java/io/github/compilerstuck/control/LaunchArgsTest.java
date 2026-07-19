package io.github.compilerstuck.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LaunchArgsTest {

  @Test
  void emptyArgsDefaultToWindowedPrimary() {
    LaunchArgs.parse(new String[0]);
    assertFalse(LaunchArgs.fullscreen());
    assertFalse(LaunchArgs.portrait());
    assertEquals(0, LaunchArgs.display());
  }

  @Test
  void fullscreenFlag() {
    LaunchArgs.parse(new String[] {"fullscreen"});
    assertTrue(LaunchArgs.fullscreen());
    assertFalse(LaunchArgs.portrait());
  }

  @Test
  void portraitFlag() {
    LaunchArgs.parse(new String[] {"portrait"});
    assertFalse(LaunchArgs.fullscreen());
    assertTrue(LaunchArgs.portrait());
  }

  @Test
  void fullscreenWinsOverPortrait() {
    LaunchArgs.parse(new String[] {"portrait", "fullscreen"});
    assertTrue(LaunchArgs.fullscreen());
    assertFalse(LaunchArgs.portrait());
  }

  @Test
  void displayIndex() {
    LaunchArgs.parse(new String[] {"fullscreen", "--display=2"});
    assertTrue(LaunchArgs.fullscreen());
    assertEquals(2, LaunchArgs.display());
  }

  @Test
  void invalidDisplayFallsBackToPrimary() {
    LaunchArgs.parse(new String[] {"--display=nope"});
    assertEquals(0, LaunchArgs.display());
  }

  @Test
  void unknownArgsIgnored() {
    LaunchArgs.parse(new String[] {"nope"});
    assertFalse(LaunchArgs.fullscreen());
    assertFalse(LaunchArgs.portrait());
    assertEquals(0, LaunchArgs.display());
    assertFalse(LaunchArgs.perfStats());
  }

  @Test
  void perfStatsFlag() {
    LaunchArgs.parse(new String[] {"--perf-stats"});
    assertTrue(LaunchArgs.perfStats());
    assertFalse(LaunchArgs.fullscreen());
  }

  @Test
  void perfStatsDefaultsOff() {
    LaunchArgs.parse(new String[0]);
    assertFalse(LaunchArgs.perfStats());
    assertFalse(LaunchArgs.legacy3d());
    assertFalse(LaunchArgs.legacy2d());
  }

  @Test
  void legacy3dFlag() {
    LaunchArgs.parse(new String[] {"--legacy-3d"});
    assertTrue(LaunchArgs.legacy3d());
    assertFalse(LaunchArgs.perfStats());
  }

  @Test
  void legacy2dFlag() {
    LaunchArgs.parse(new String[] {"--legacy-2d"});
    assertTrue(LaunchArgs.legacy2d());
    assertFalse(LaunchArgs.legacy3d());
  }
}
