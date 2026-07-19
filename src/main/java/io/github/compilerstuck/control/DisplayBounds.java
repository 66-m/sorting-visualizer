package io.github.compilerstuck.control;

import io.github.compilerstuck.control.config.MainControllerConfig;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.logging.Logger;

/**
 * Resolves monitor bounds and portrait window size without a graphics backend. Shared by the libGDX
 * launcher and Settings placement.
 */
public final class DisplayBounds {
  private static final Logger LOGGER = Logger.getLogger(DisplayBounds.class.getName());

  private DisplayBounds() {}

  /**
   * @param displayIndex 1-based display index, or {@code <= 0} for the primary
   */
  public static Rectangle resolveBounds(int displayIndex) {
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
   * Portrait window sized from the screen: ~90% of screen height, width from the design portrait
   * aspect ratio, clamped to fit.
   */
  public static Dimension portraitSize(Rectangle screen) {
    int h = Math.max(1, (int) Math.round(screen.height * 0.9));
    int w =
        Math.max(
            1,
            Math.round(
                h
                    * (MainControllerConfig.PORTRAIT_WIDTH
                        / (float) MainControllerConfig.PORTRAIT_HEIGHT)));
    if (w > screen.width) {
      w = screen.width;
      h =
          Math.max(
              1,
              Math.round(
                  w
                      * (MainControllerConfig.PORTRAIT_HEIGHT
                          / (float) MainControllerConfig.PORTRAIT_WIDTH)));
    }
    if (h > screen.height) {
      h = screen.height;
    }
    return new Dimension(w, h);
  }
}
