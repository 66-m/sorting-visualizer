package io.github.compilerstuck.control.ui.settingsfx;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Scene;

/** Shared lookup for the Settings application stylesheet. */
public final class SettingsStylesheets {

  private static final Logger LOGGER = Logger.getLogger(SettingsStylesheets.class.getName());
  public static final String CSS_PATH = "/css/settings-app.css";

  private SettingsStylesheets() {}

  public static URL cssUrl() {
    return SettingsStylesheets.class.getResource(CSS_PATH);
  }

  public static void applyTo(Scene scene) {
    URL css = cssUrl();
    if (css != null) {
      scene.getStylesheets().add(css.toExternalForm());
    } else {
      LOGGER.log(Level.WARNING, "Missing stylesheet: {0}", CSS_PATH);
    }
  }
}
