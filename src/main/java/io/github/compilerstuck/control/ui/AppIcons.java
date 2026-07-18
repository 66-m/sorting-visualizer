package io.github.compilerstuck.control.ui;

import com.jogamp.common.util.IOUtil;
import com.jogamp.newt.NewtFactory;
import java.awt.Image;
import java.awt.Taskbar;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Loads {@code logo.png} (and multi-size variants) for the taskbar and NEWT / Processing surfaces.
 * Settings window icons are applied separately via JavaFX ({@code settings.png}).
 */
public final class AppIcons {
  private static final Logger LOGGER = Logger.getLogger(AppIcons.class.getName());

  /** Canonical app logo on the classpath (same asset as {@code images/logo.png}). */
  public static final String LOGO_RESOURCE = "/logo.png";

  /** Settings window icon (PNG raster of {@code settings.svg}; ImageIO cannot load SVG). */
  public static final String SETTINGS_RESOURCE = "/settings.png";

  /**
   * NEWT wants several PNGs from small to large for window / taskbar icons. Generated from {@link
   * #LOGO_RESOURCE}.
   */
  private static final String[] NEWT_ICON_RESOURCES = {
    "/icons/icon-16.png",
    "/icons/icon-32.png",
    "/icons/icon-48.png",
    "/icons/icon-64.png",
    "/icons/icon-128.png",
    "/icons/icon-256.png",
    LOGO_RESOURCE
  };

  private static Image logoImage;
  private static List<Image> logoImages;

  private AppIcons() {}

  /**
   * Installs icons used by the whole process: NEWT/Processing window icons (must run before any
   * NEWT window is created) and the desktop taskbar icon when supported.
   */
  public static void installApplicationIcons() {
    try {
      NewtFactory.setWindowIcons(
          new IOUtil.ClassResources(
              NEWT_ICON_RESOURCES, AppIcons.class.getClassLoader(), AppIcons.class));
    } catch (RuntimeException e) {
      LOGGER.log(Level.WARNING, "Failed to set NEWT window icons", e);
    }

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

  public static List<Image> logoImages() {
    if (logoImages == null) {
      List<Image> images = new ArrayList<>(NEWT_ICON_RESOURCES.length);
      for (String path : NEWT_ICON_RESOURCES) {
        Image image = LOGO_RESOURCE.equals(path) ? logo() : load(path);
        if (image != null) {
          images.add(image);
        }
      }
      logoImages = List.copyOf(images);
    }
    return logoImages;
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
