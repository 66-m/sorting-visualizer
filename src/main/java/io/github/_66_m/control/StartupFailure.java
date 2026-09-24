package io.github._66_m.control;

import java.awt.GraphicsEnvironment;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Reports an error that stops the app from starting. Double-clicking the JAR on Windows runs {@code
 * javaw}, which has no console, so without a dialog the app just seems to close; and the running
 * JavaFX toolkit would keep the process alive without any window. {@link DesktopLauncher} shows
 * {@link #report} and then exits.
 */
final class StartupFailure {
  private static final Logger LOGGER = Logger.getLogger(StartupFailure.class.getName());

  /** libGDX's message when GLFW cannot create the window with the requested OpenGL version. */
  static final String WINDOW_CREATION_FAILED = "Couldn't create window";

  private StartupFailure() {}

  /** Logs {@code error} and, when a display is available, shows it in a dialog. */
  static void report(Throwable error) {
    String message = describe(error);
    LOGGER.log(Level.SEVERE, message, error);
    if (GraphicsEnvironment.isHeadless()) {
      return;
    }
    try {
      SwingUtilities.invokeAndWait(
          () ->
              JOptionPane.showMessageDialog(
                  null,
                  message + "\n\nDetails:\n" + stackTraceHead(error, 12),
                  "Sorting Visualizer error",
                  JOptionPane.ERROR_MESSAGE));
    } catch (Exception | Error dialogError) {
      // The log above already has the error; nothing more to show it with.
      LOGGER.log(Level.WARNING, "Could not show the startup error dialog", dialogError);
    }
  }

  /** The text for the user: a known cause with a hint, or the error itself. */
  static String describe(Throwable error) {
    for (Throwable t = error; t != null; t = t.getCause()) {
      String text = t.getMessage();
      if (text != null && text.contains(WINDOW_CREATION_FAILED)) {
        return "The visualization window could not be created. Sorting Visualizer needs"
            + " OpenGL 3.3, which your graphics driver does not provide.\n\n"
            + "Install the latest driver for your graphics card from its vendor (Intel, AMD or"
            + " NVIDIA). Remote desktop sessions and virtual machines often lack OpenGL 3.3 too.";
      }
    }
    return "Sorting Visualizer stopped because of an error:\n" + error;
  }

  private static String stackTraceHead(Throwable error, int maxLines) {
    StringWriter out = new StringWriter();
    error.printStackTrace(new PrintWriter(out));
    String[] lines = out.toString().split("\\R");
    StringBuilder head = new StringBuilder();
    for (int i = 0; i < Math.min(lines.length, maxLines); i++) {
      head.append(lines[i]).append('\n');
    }
    if (lines.length > maxLines) {
      head.append("\t...");
    }
    return head.toString();
  }
}
