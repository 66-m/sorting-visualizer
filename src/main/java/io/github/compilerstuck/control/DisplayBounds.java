package io.github.compilerstuck.control;

import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.compilerstuck.control.config.AppConfig;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.logging.Logger;

/**
 * Resolves monitor bounds / GLFW monitors and portrait window size for the libGDX visualization
 * window. Settings placement uses JavaFX {@code Screen.getPrimary().getVisualBounds()} instead -
 * AWT and JavaFX coordinates can disagree on multi-monitor / HiDPI Linux.
 */
public final class DisplayBounds {
  private static final Logger LOGGER = Logger.getLogger(DisplayBounds.class.getName());

  private DisplayBounds() {}

  /**
   * Resolves the pixel bounds of a monitor for the visualization window.
   *
   * @param displayIndex 1-based display index, or {@code <= 0} for the default (display 2 when more
   *     than one monitor is available so the primary stays free for Settings, otherwise the
   *     primary)
   */
  public static Rectangle resolveBounds(int displayIndex) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] devices = ge.getScreenDevices();
    int listIndex = resolveListIndex(displayIndex, devices.length);
    GraphicsDevice device = listIndex >= 0 ? devices[listIndex] : ge.getDefaultScreenDevice();
    return device.getDefaultConfiguration().getBounds();
  }

  /**
   * Resolves the GLFW/libGDX {@link Monitor} for exclusive fullscreen, using the same {@code
   * --display=} selection rules as {@link #resolveBounds(int)}.
   */
  public static Monitor resolveMonitor(int displayIndex) {
    Monitor[] monitors = Lwjgl3ApplicationConfiguration.getMonitors();
    int listIndex = resolveListIndex(displayIndex, monitors.length);
    return listIndex >= 0
        ? monitors[listIndex]
        : Lwjgl3ApplicationConfiguration.getPrimaryMonitor();
  }

  /**
   * Maps a 1-based {@code --display=} index to a 0-based list index. {@code displayIndex <= 0}
   * prefers the secondary monitor when more than one is available. Returns {@code -1} to mean "use
   * the platform primary/default".
   */
  static int resolveListIndex(int displayIndex, int count) {
    int index = displayIndex;
    if (index <= 0) {
      // Prefer the secondary monitor when present so Settings can own the primary.
      index = count > 1 ? 2 : 0;
    }
    if (index > 0 && index <= count) {
      return index - 1;
    }
    if (index > 0) {
      final int requested = index;
      final int available = count;
      LOGGER.warning(
          () -> "Display " + requested + " not found (" + available + " available); using primary");
    }
    return -1;
  }

  /**
   * Portrait window sized from the screen: ~90% of screen height, width from the design portrait
   * aspect ratio, clamped to fit.
   */
  public static Dimension portraitSize(Rectangle screen) {
    int h = Math.max(1, (int) Math.round(screen.height * 0.9));
    int w =
        Math.max(1, Math.round(h * (AppConfig.PORTRAIT_WIDTH / (float) AppConfig.PORTRAIT_HEIGHT)));
    if (w > screen.width) {
      w = screen.width;
      h =
          Math.max(
              1, Math.round(w * (AppConfig.PORTRAIT_HEIGHT / (float) AppConfig.PORTRAIT_WIDTH)));
    }
    if (h > screen.height) {
      h = screen.height;
    }
    return new Dimension(w, h);
  }
}
