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
      } else if ("--legacy-3d".equalsIgnoreCase(arg) || "--legacy-2d".equalsIgnoreCase(arg)) {
        LOGGER.warning(
            "Ignoring removed flag "
                + arg
                + " (legacy ShapeRenderer/ModelBatch paths were retired; GL30 is required)");
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
}
