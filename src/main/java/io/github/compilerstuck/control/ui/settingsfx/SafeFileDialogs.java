package io.github.compilerstuck.control.ui.settingsfx;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * File pickers that avoid JavaFX {@code FileChooser} on Linux.
 *
 * <p>JavaFX's GTK {@code GtkCommonDialogs} SIGSEGVs when an LWJGL/OpenGL window shares the same
 * process (common with libGDX). Swing's cross-platform chooser does not use that native path.
 */
public final class SafeFileDialogs {

  private static final Logger LOGGER = Logger.getLogger(SafeFileDialogs.class.getName());

  private SafeFileDialogs() {}

  /** Opens an image open-dialog; returns {@code null} if cancelled or on failure. */
  public static File chooseImageFile() {
    AtomicReference<File> selected = new AtomicReference<>();
    Runnable show =
        () -> {
          try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
          } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not set cross-platform L&F for file chooser", e);
          }
          JFileChooser chooser = new JFileChooser();
          chooser.setDialogTitle(SettingsStrings.BROWSE);
          chooser.setAcceptAllFileFilterUsed(true);
          chooser.setFileFilter(
              new FileNameExtensionFilter(
                  "Images", "png", "jpg", "jpeg", "gif", "bmp", "PNG", "JPG", "JPEG", "GIF",
                  "BMP"));
          int result = chooser.showOpenDialog(null);
          if (result == JFileChooser.APPROVE_OPTION) {
            selected.set(chooser.getSelectedFile());
          }
        };

    if (EventQueue.isDispatchThread()) {
      show.run();
    } else {
      try {
        EventQueue.invokeAndWait(show);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        LOGGER.log(Level.WARNING, "Image file chooser interrupted", e);
      } catch (InvocationTargetException e) {
        LOGGER.log(
            Level.WARNING, "Image file chooser failed", e.getCause() != null ? e.getCause() : e);
      }
    }
    return selected.get();
  }
}
