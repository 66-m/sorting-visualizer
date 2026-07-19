package io.github.compilerstuck.control.ui.settingsfx;

import com.badlogic.gdx.Gdx;
import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.config.MainControllerConfig;
import io.github.compilerstuck.control.ui.AppIcons;
import io.github.compilerstuck.control.ui.settingsfx.vm.AlgorithmViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.AppearanceViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.ArraySizeViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.DebugViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.DisplayViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.SoundViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.SpeedViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.VisualizationViewModel;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Owns the JavaFX Settings {@link Stage}, view-models, section binding, and action bar. Not an
 * {@link javafx.application.Application} subclass: classpath launcher (Phase 0).
 *
 * <p>Window close shuts down Settings and the canvas via {@link AppContext#shutdown()}.
 */
public final class SettingsFxController {

  private static final Logger LOGGER = Logger.getLogger(SettingsFxController.class.getName());
  private static final String CSS_PATH = "/css/settings-app.css";
  private static final Duration RAISE_PULSE = Duration.millis(300);

  private static volatile SettingsFxController instance;

  private Stage stage;
  private AppContext app;
  private ProgressBar progressBar;
  private Button runButton;
  private Button cancelButton;
  private boolean contentReady;

  private SoundViewModel soundVm;
  private SpeedViewModel speedVm;
  private DisplayViewModel displayVm;
  private DebugViewModel debugVm;
  private ArraySizeViewModel arraySizeVm;
  private AppearanceViewModel appearanceVm;
  private VisualizationViewModel visualizationVm;
  private AlgorithmViewModel algorithmVm;

  private boolean inputsEnabled = true;
  private volatile int lastAppliedProgress = Integer.MIN_VALUE;

  private SettingsFxController() {}

  /**
   * Opens the Settings window chrome immediately (placeholder content). Call early so the Stage
   * appears on the primary screen while libGDX / AppContext init finishes.
   */
  public static void prepare() {
    Platform.runLater(
        () -> {
          SettingsFxController ctrl = instance();
          ctrl.ensureChrome();
          ctrl.raiseAndShow();
          ctrl.centerOnPrimaryScreen();
          LOGGER.info("SettingsFx Stage prepared (placeholder)");
        });
  }

  /**
   * Replaces the placeholder with live {@link AppContext} wiring. Must be called after bootstrap.
   */
  public static void show(AppContext appContext) {
    Objects.requireNonNull(appContext, "appContext");
    Platform.runLater(
        () -> {
          SettingsFxController ctrl = instance();
          ctrl.ensureChrome();
          ctrl.ensureContent(appContext);
          ctrl.raiseAndShow();
          ctrl.centerOnPrimaryScreen();
          LOGGER.info("SettingsFx Stage shown");
        });
  }

  /** Fans running-state enablement out to every section view-model. */
  public static void setInputsEnabled(boolean enabled) {
    Platform.runLater(
        () -> {
          SettingsFxController ctrl = instance;
          if (ctrl == null || ctrl.app == null) {
            return;
          }
          ctrl.inputsEnabled = enabled;
          ctrl.soundVm.setInputsEnabled(enabled);
          ctrl.speedVm.setInputsEnabled(enabled);
          ctrl.displayVm.setInputsEnabled(enabled);
          ctrl.arraySizeVm.setInputsEnabled(enabled);
          ctrl.appearanceVm.setInputsEnabled(enabled);
          ctrl.visualizationVm.setInputsEnabled(enabled);
          ctrl.algorithmVm.setInputsEnabled(enabled);
          ctrl.updateRunEnabled();
        });
  }

  public static void setCancelEnabled(boolean enabled) {
    Platform.runLater(
        () -> {
          if (instance != null && instance.cancelButton != null) {
            instance.cancelButton.setDisable(!enabled);
          }
        });
  }

  public static void setProgress(int progress) {
    int clamped = Math.max(0, Math.min(100, progress));
    SettingsFxController ctrl = instance;
    if (ctrl != null && clamped == ctrl.lastAppliedProgress) {
      return;
    }
    Platform.runLater(
        () -> {
          if (instance == null || instance.progressBar == null) {
            return;
          }
          if (clamped == instance.lastAppliedProgress) {
            return;
          }
          instance.lastAppliedProgress = clamped;
          instance.progressBar.setProgress(clamped / 100.0);
        });
  }

  /** Refresh CSV export enablement after a session finishes. */
  public static void refreshExportEnabled() {
    Platform.runLater(
        () -> {
          if (instance != null && instance.displayVm != null) {
            instance.displayVm.refreshCanExport();
          }
        });
  }

  private static SettingsFxController instance() {
    if (instance == null) {
      synchronized (SettingsFxController.class) {
        if (instance == null) {
          instance = new SettingsFxController();
        }
      }
    }
    return instance;
  }

  /** Creates and sizes the Stage with a lightweight placeholder scene (no section graph). */
  private void ensureChrome() {
    if (stage != null) {
      return;
    }

    stage = new Stage();
    stage.setTitle(SettingsStrings.WINDOW_TITLE);
    stage.setScene(placeholderScene());
    URL logoIcon = AppIcons.class.getResource(AppIcons.LOGO_RESOURCE);
    if (logoIcon != null) {
      stage.getIcons().add(new Image(logoIcon.toExternalForm()));
    }
    stage.setMinWidth(MainControllerConfig.SETTINGS_MIN_WIDTH);
    stage.setMinHeight(MainControllerConfig.SETTINGS_MIN_HEIGHT);

    Rectangle2D visual = primaryVisualBounds();
    double width =
        Math.max(
            MainControllerConfig.SETTINGS_MIN_WIDTH,
            visual.getWidth() * MainControllerConfig.SETTINGS_SCREEN_FRACTION);
    double height =
        Math.max(
            MainControllerConfig.SETTINGS_MIN_HEIGHT,
            visual.getHeight() * MainControllerConfig.SETTINGS_SCREEN_FRACTION);
    stage.setWidth(width);
    stage.setHeight(height);
    centerOnPrimaryScreen();

    stage.setOnCloseRequest(
        e -> {
          e.consume();
          LOGGER.info("SettingsFx close request → shutdown");
          if (app != null) {
            app.shutdown();
          } else {
            // Placeholder still up; AppContext not wired yet.
            JavaFxBootstrap.shutdown();
            if (Gdx.app != null) {
              Gdx.app.exit();
            }
          }
        });
  }

  /**
   * Centers the Settings stage on the primary monitor using JavaFX visual bounds. AWT screen
   * rectangles do not match JavaFX coordinates on multi-monitor / HiDPI Linux.
   */
  private void centerOnPrimaryScreen() {
    if (stage == null) {
      return;
    }
    Rectangle2D visual = primaryVisualBounds();
    stage.setX(visual.getMinX() + Math.max(0, (visual.getWidth() - stage.getWidth()) / 2.0));
    stage.setY(visual.getMinY() + Math.max(0, (visual.getHeight() - stage.getHeight()) / 2.0));
  }

  private static Rectangle2D primaryVisualBounds() {
    return Screen.getPrimary().getVisualBounds();
  }

  private void ensureContent(AppContext appContext) {
    if (contentReady) {
      return;
    }
    this.app = appContext;
    createViewModels(appContext);

    ShellResult shell =
        SettingsShell.build(
            new SectionNodes(
                ArraySizeSection.build(arraySizeVm),
                SortingSection.build(algorithmVm),
                SpeedSection.build(speedVm),
                VisualizationSection.build(visualizationVm),
                AppearanceSection.build(appearanceVm),
                DisplaySection.build(displayVm),
                SoundSection.build(soundVm),
                DebugSection.build(debugVm)));

    progressBar = shell.progress();
    runButton = shell.run();
    cancelButton = shell.cancel();
    wireActionBar();
    updateRunEnabled();

    Scene scene = new Scene(shell.root());
    applyAppStylesheet(scene);
    stage.setScene(scene);
    contentReady = true;
    // Cold popup skins are ~0.5–1s with NEWT live; warm invisibly after content is on screen.
    PopupPrewarm.warmControlsAsync(shell.root());
  }

  private static Scene placeholderScene() {
    Label loading = new Label(SettingsStrings.LOADING);
    loading.getStyleClass().add("settings-loading-label");
    StackPane root = new StackPane(loading);
    root.setAlignment(Pos.CENTER);
    Scene scene = new Scene(root);
    applyAppStylesheet(scene);
    return scene;
  }

  private static void applyAppStylesheet(Scene scene) {
    URL css = SettingsFxController.class.getResource(CSS_PATH);
    if (css != null) {
      scene.getStylesheets().add(css.toExternalForm());
    } else {
      LOGGER.log(Level.WARNING, "Missing stylesheet: {0}", CSS_PATH);
    }
  }

  /**
   * Show + brief always-on-top pulse so a maximized NEWT canvas does not keep Settings buried
   * (Phase 0 §4.3).
   */
  private void raiseAndShow() {
    if (stage == null) {
      return;
    }
    stage.show();
    stage.setIconified(false);
    stage.setAlwaysOnTop(true);
    stage.toFront();
    stage.requestFocus();
    PauseTransition clear = new PauseTransition(RAISE_PULSE);
    clear.setOnFinished(e -> stage.setAlwaysOnTop(false));
    clear.play();
  }

  private void createViewModels(AppContext appContext) {
    visualizationVm = new VisualizationViewModel(appContext);
    arraySizeVm = new ArraySizeViewModel(appContext, visualizationVm::currentConstraints);
    visualizationVm.setSizeDisplaySync(arraySizeVm::syncDisplayedSize);
    algorithmVm = new AlgorithmViewModel(appContext);
    speedVm = new SpeedViewModel(appContext);
    appearanceVm = new AppearanceViewModel(appContext);
    displayVm = new DisplayViewModel(appContext);
    soundVm = new SoundViewModel(appContext);
    debugVm = new DebugViewModel(appContext);

    arraySizeVm.addPropertyChangeListener(
        evt -> {
          if (ArraySizeViewModel.PROP_CAN_RUN.equals(evt.getPropertyName())) {
            VmBindings.runFx(this::updateRunEnabled);
          }
        });
    algorithmVm.addPropertyChangeListener(
        evt -> {
          if (AlgorithmViewModel.PROP_CAN_START.equals(evt.getPropertyName())
              || AlgorithmViewModel.PROP_RUN_ALL.equals(evt.getPropertyName())
              || AlgorithmViewModel.PROP_ENTRIES.equals(evt.getPropertyName())) {
            VmBindings.runFx(this::updateRunEnabled);
          }
        });
  }

  private void wireActionBar() {
    runButton.setOnAction(
        e -> {
          // Apply typed size even if the user skipped the Apply button.
          arraySizeVm.applyText();
          algorithmVm.applySelectionToAppContext();
          app.setStart(true);
          cancelButton.setDisable(false);
        });
    cancelButton.setOnAction(
        e -> {
          app.cancelSorting();
          cancelButton.setDisable(true);
        });
  }

  private void updateRunEnabled() {
    if (runButton == null || arraySizeVm == null || algorithmVm == null) {
      return;
    }
    boolean enabled = inputsEnabled && arraySizeVm.canRun() && algorithmVm.canStart();
    runButton.setDisable(!enabled);
  }
}
