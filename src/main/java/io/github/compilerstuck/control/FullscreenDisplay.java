package io.github.compilerstuck.control;

import com.jogamp.newt.MonitorDevice;
import com.jogamp.newt.opengl.GLWindow;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PSurface;

/**
 * Resolves a target monitor and enters per-display fullscreen via JOGL.
 *
 * <p>Processing's {@code fullScreen(P3D, display)} is unreliable on multi-monitor setups
 * (especially vertically stacked displays with different resolutions). We size the sketch to the
 * chosen display's bounds, then apply native fullscreen from a background thread (JOGL forbids it
 * on the animation thread).
 */
final class FullscreenDisplay {
  private static final Logger LOGGER = Logger.getLogger(FullscreenDisplay.class.getName());
  private static final long APPLY_DELAY_MS = 250L;

  private FullscreenDisplay() {}

  /**
   * @param displayIndex 1-based Processing display index, or {@code <= 0} for the primary
   */
  static Rectangle resolveBounds(int displayIndex) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] devices = ge.getScreenDevices();
    GraphicsDevice device = ge.getDefaultScreenDevice();
    if (displayIndex > 0 && displayIndex <= devices.length) {
      device = devices[displayIndex - 1];
    } else if (displayIndex > 0) {
      LOGGER.warning(
          () ->
              "Display "
                  + displayIndex
                  + " not found ("
                  + devices.length
                  + " available); using primary");
    }
    return device.getDefaultConfiguration().getBounds();
  }

  /**
   * Positions the surface immediately, then applies borderless/fullscreen after the Processing
   * animation thread has settled.
   */
  static void applyAsync(PSurface surface, Rectangle bounds) {
    if (surface == null || bounds == null) {
      return;
    }
    surface.setLocation(bounds.x, bounds.y);

    Thread applier =
        new Thread(
            () -> {
              try {
                Thread.sleep(APPLY_DELAY_MS);
                applyOnNativeWindow(surface, bounds);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to apply fullscreen on " + bounds, e);
              }
            },
            "fullscreen-display-applier");
    applier.setDaemon(true);
    applier.start();
  }

  private static void applyOnNativeWindow(PSurface surface, Rectangle bounds) {
    Object nativeSurface = surface.getNative();
    if (!(nativeSurface instanceof GLWindow glWindow)) {
      LOGGER.log(Level.INFO, "Native surface is not a GLWindow; keeping positioned window");
      return;
    }

    MonitorDevice monitor = findMonitor(glWindow, bounds);
    Thread previous = glWindow.setExclusiveContextThread(null);
    try {
      if (monitor != null && glWindow.setFullscreen(Collections.singletonList(monitor))) {
        return;
      }
      LOGGER.warning(
          "JOGL per-monitor fullscreen unavailable; using undecorated window at " + bounds);
      placeUndecorated(glWindow, bounds);
    } finally {
      glWindow.setExclusiveContextThread(previous);
    }
  }

  private static void placeUndecorated(GLWindow glWindow, Rectangle bounds) {
    if (glWindow.isFullscreen()) {
      glWindow.setFullscreen(false);
    }
    boolean visible = glWindow.isVisible();
    if (visible) {
      glWindow.setVisible(false);
    }
    glWindow.setUndecorated(true);
    glWindow.setPosition(bounds.x, bounds.y);
    glWindow.setSize(bounds.width, bounds.height);
    if (visible) {
      glWindow.setVisible(true);
    }
  }

  private static MonitorDevice findMonitor(GLWindow glWindow, Rectangle bounds) {
    List<MonitorDevice> monitors = glWindow.getScreen().getMonitorDevices();
    if (monitors == null || monitors.isEmpty()) {
      return null;
    }
    MonitorDevice best = null;
    int bestScore = Integer.MIN_VALUE;
    for (MonitorDevice monitor : monitors) {
      var viewport = monitor.getViewport();
      int score =
          scoreMatch(
              bounds, viewport.getX(), viewport.getY(), viewport.getWidth(), viewport.getHeight());
      if (score > bestScore) {
        bestScore = score;
        best = monitor;
      }
    }
    return bestScore >= 1_000_000 ? best : null;
  }

  /** Higher is better. Exact size match is required for a positive result. */
  static int scoreMatch(Rectangle bounds, int x, int y, int w, int h) {
    if (bounds.width != w || bounds.height != h) {
      return -1;
    }
    int dx = Math.abs(bounds.x - x);
    int dy = Math.abs(bounds.y - y);
    return 1_000_000 - dx - dy;
  }
}
