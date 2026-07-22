package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.PrimerLight;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Bootstraps the JavaFX toolkit once for Settings. Call {@link #start()} from {@code
 * DesktopLauncher.main} before {@code Lwjgl3Application}, and {@link #shutdown()} from the real app
 * shutdown path.
 */
public final class JavaFxBootstrap {

  private static final Logger LOGGER = Logger.getLogger(JavaFxBootstrap.class.getName());
  private static final AtomicBoolean STARTED = new AtomicBoolean(false);

  private JavaFxBootstrap() {}

  /** Idempotent toolkit start. Safe to call once from {@code main()}. */
  public static void start() {
    if (!STARTED.compareAndSet(false, true)) {
      return;
    }
    Platform.startup(JavaFxBootstrap::onToolkitStarted);
    // Closing the last Stage must not terminate the JVM (canvas + Swing Settings keep running).
    Platform.setImplicitExit(false);
  }

  /**
   * Theme + first CSS/control classload. Runs on the FX thread while the libGDX window boots on the
   * caller thread so Settings' first real Scene is cheaper.
   */
  private static void onToolkitStarted() {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    prewarmControls();
    LOGGER.info("JavaFX toolkit started (Primer Light)");
  }

  private static void prewarmControls() {
    ComboBox<String> combo = new ComboBox<>();
    for (int i = 0; i < 30; i++) {
      combo.getItems().add("warm-" + i);
    }
    combo.getSelectionModel().select(0);
    ColorPicker colorPicker = new ColorPicker();
    VBox root =
        new VBox(
            new Label("warm"),
            new Button("Run"),
            combo,
            new Slider(),
            colorPicker,
            new ToggleSwitch());
    Scene scene = new Scene(root, 8, 8);
    URL css = SettingsStylesheets.cssUrl();
    if (css != null) {
      scene.getStylesheets().add(css.toExternalForm());
    }

    // applyCss alone does not build ComboBox/ColorPicker popup skins; those only load on show().
    Stage warm = new Stage(StageStyle.UTILITY);
    warm.setOpacity(0);
    warm.setX(-20_000);
    warm.setY(-20_000);
    warm.setScene(scene);
    warm.show();
    try {
      PopupPrewarm.warmControls(root);
    } finally {
      warm.close();
    }
  }

  /** Terminates the JavaFX toolkit. Call from the real app shutdown path. */
  public static void shutdown() {
    if (!STARTED.get()) {
      return;
    }
    LOGGER.info("JavaFX Platform.exit() requested");
    Platform.exit();
  }

  public static boolean isStarted() {
    return STARTED.get();
  }
}
