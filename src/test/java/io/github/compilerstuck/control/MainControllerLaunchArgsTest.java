package io.github.compilerstuck.control;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MainControllerLaunchArgsTest {

  @AfterEach
  void resetFlags() {
    MainController.parseLaunchArgs(new String[0]);
  }

  @Test
  @DisplayName("fullscreen arg sets launch fullscreen")
  void parsesFullscreen() {
    MainController.parseLaunchArgs(new String[] {"fullscreen"});
    assertTrue(MainController.isLaunchFullscreen());
    assertFalse(MainController.isLaunchPortrait());
  }

  @Test
  @DisplayName("portrait arg sets launch portrait")
  void parsesPortrait() {
    MainController.parseLaunchArgs(new String[] {"portrait"});
    assertFalse(MainController.isLaunchFullscreen());
    assertTrue(MainController.isLaunchPortrait());
  }

  @Test
  @DisplayName("fullscreen wins over portrait when both are passed")
  void fullscreenWinsOverPortrait() {
    MainController.parseLaunchArgs(new String[] {"portrait", "fullscreen"});
    assertTrue(MainController.isLaunchFullscreen());
    assertFalse(MainController.isLaunchPortrait());
  }

  @Test
  @DisplayName("--display=N is parsed as 1-based index")
  void parsesDisplayIndex() {
    MainController.parseLaunchArgs(new String[] {"fullscreen", "--display=2"});
    assertTrue(MainController.isLaunchFullscreen());
    assertEquals(2, MainController.getLaunchDisplay());
  }

  @Test
  @DisplayName("invalid --display falls back to primary (0)")
  void invalidDisplayFallsBack() {
    MainController.parseLaunchArgs(new String[] {"--display=nope"});
    assertEquals(0, MainController.getLaunchDisplay());
  }

  @Test
  @DisplayName("unknown args are ignored")
  void ignoresUnknown() {
    MainController.parseLaunchArgs(new String[] {"nope"});
    assertFalse(MainController.isLaunchFullscreen());
    assertFalse(MainController.isLaunchPortrait());
    assertEquals(0, MainController.getLaunchDisplay());
  }

  @Test
  @DisplayName("scoreMatch prefers exact size and closer origin")
  void scoreMatchPrefersCloserOrigin() {
    Rectangle bounds = new Rectangle(100, 200, 2560, 1440);
    assertTrue(
        FullscreenDisplay.scoreMatch(bounds, 100, 200, 2560, 1440)
            > FullscreenDisplay.scoreMatch(bounds, 0, 0, 2560, 1440));
    assertEquals(-1, FullscreenDisplay.scoreMatch(bounds, 100, 200, 1920, 1080));
  }

  @Test
  @DisplayName("settingsSize is half width and ~90% height (max bounds)")
  void settingsSizeIsHalfScreenMax() {
    Rectangle screen = new Rectangle(0, 0, 1920, 1080);
    var size = FullscreenDisplay.settingsSize(screen);
    assertEquals(960, size.width);
    assertEquals(972, size.height);
  }

  @Test
  @DisplayName("portraitSize fits within the screen at ~90% height")
  void portraitSizeFitsScreen() {
    Rectangle screen = new Rectangle(10, 20, 1920, 1080);
    var size = FullscreenDisplay.portraitSize(screen);
    assertTrue(size.width <= screen.width);
    assertTrue(size.height <= screen.height);
    assertTrue(size.height >= (int) (screen.height * 0.9) - 1);
  }
}
