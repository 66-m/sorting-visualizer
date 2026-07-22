package io.github.compilerstuck.control;

import java.util.logging.Logger;

/**
 * Parses CLI launch tokens shared by {@link DesktopLauncher}. Package-visible accessors support
 * unit tests.
 */
public final class LaunchArgs {
  private static final Logger LOGGER = Logger.getLogger(LaunchArgs.class.getName());

  private static boolean launchFullscreen;
  private static boolean launchPortrait;
  private static int launchDisplay;
  private static boolean launchPerfStats;
  private static boolean launchLegacy3d;
  private static boolean launchLegacy2d;

  private LaunchArgs() {}

  /**
   * Parses known launch tokens. {@code fullscreen} wins over {@code portrait} when both are
   * present. Unknown args are ignored.
   */
  public static void parse(String[] passedArgs) {
    launchFullscreen = false;
    launchPortrait = false;
    launchDisplay = 0;
    launchPerfStats = false;
    launchLegacy3d = false;
    launchLegacy2d = false;
    if (passedArgs == null) {
      return;
    }
    for (String arg : passedArgs) {
      if (arg == null) {
        continue;
      }
      if ("fullscreen".equalsIgnoreCase(arg)) {
        launchFullscreen = true;
      } else if ("portrait".equalsIgnoreCase(arg)) {
        launchPortrait = true;
      } else if ("--perf-stats".equalsIgnoreCase(arg)) {
        launchPerfStats = true;
      } else if ("--legacy-3d".equalsIgnoreCase(arg)) {
        launchLegacy3d = true;
      } else if ("--legacy-2d".equalsIgnoreCase(arg)) {
        launchLegacy2d = true;
      } else if (arg.regionMatches(true, 0, "--display=", 0, "--display=".length())) {
        launchDisplay = parseDisplayIndex(arg.substring("--display=".length()));
      }
    }
    if (launchFullscreen) {
      launchPortrait = false;
    }
  }

  private static int parseDisplayIndex(String value) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      LOGGER.warning("Invalid --display value: " + value);
      return 0;
    }
  }

  static boolean isFullscreen() {
    return launchFullscreen;
  }

  static boolean isPortrait() {
    return launchPortrait;
  }

  static int getDisplay() {
    return launchDisplay;
  }

  public static boolean fullscreen() {
    return launchFullscreen;
  }

  public static boolean portrait() {
    return launchPortrait;
  }

  public static int display() {
    return launchDisplay;
  }

  /** When true, show FrameStats overlay and log a summary every 60 frames. */
  public static boolean perfStats() {
    return launchPerfStats;
  }

  /**
   * When true, force the pre-instancing ModelInstance 3D path (A/B with {@code --perf-stats}).
   * Supported fallback when GL30/instancing is unavailable.
   */
  public static boolean legacy3d() {
    return launchLegacy3d;
  }

  /** When true, force ShapeRenderer for World2D circles/ellipses (A/B with GeometryBatch2D). */
  public static boolean legacy2d() {
    return launchLegacy2d;
  }
}
