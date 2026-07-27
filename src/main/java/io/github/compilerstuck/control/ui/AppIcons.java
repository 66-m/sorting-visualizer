package io.github.compilerstuck.control.ui;

import java.awt.Image;
import java.awt.Taskbar;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Loads {@code logo.png} for the desktop taskbar. libGDX and Settings window icons both use the
 * same logo via {@code Lwjgl3ApplicationConfiguration.setWindowIcon} and JavaFX {@code
 * Stage.getIcons()}.
 */
public final class AppIcons {
  private static final Logger LOGGER = Logger.getLogger(AppIcons.class.getName());

  /** Canonical app logo on the classpath (same asset as {@code images/logo.png}). */
  public static final String LOGO_RESOURCE = "/logo.png";

  private static Image logoImage;

  private AppIcons() {}

  /**
   * Installs the desktop taskbar icon when supported. Call before the visualization window is
   * created.
   */
  public static void installApplicationIcons() {
    Image logo = logo();
    if (logo == null) {
      return;
    }
    try {
      if (Taskbar.isTaskbarSupported()) {
        Taskbar taskbar = Taskbar.getTaskbar();
        if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
          taskbar.setIconImage(logo);
        }
      }
    } catch (UnsupportedOperationException | SecurityException e) {
      LOGGER.log(Level.FINE, "Taskbar icon not available", e);
    }
  }

  public static Image logo() {
    if (logoImage == null) {
      logoImage = load(LOGO_RESOURCE);
    }
    return logoImage;
  }

  private static Image load(String resourcePath) {
    try (InputStream in = AppIcons.class.getResourceAsStream(resourcePath)) {
      if (in == null) {
        LOGGER.warning(() -> "Missing icon resource: " + resourcePath);
        return null;
      }
      return ImageIO.read(in);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to load icon resource: " + resourcePath, e);
      return null;
    }
  }
}
