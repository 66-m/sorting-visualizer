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
import java.awt.Rectangle;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Owns the JavaFX Settings {@link Stage}, view-models, section binding, and action bar. Not an
 * {@link javafx.application.Application} subclass — classpath launcher (Phase 0).
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

  /** Cap for packed stage height (typically ~90% of launch screen). */
  private int maxStageHeight = MainControllerConfig.SETTINGS_DEFAULT_HEIGHT;

  private Rectangle launchBounds;

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
   * Opens the Settings window chrome immediately (placeholder content). Call as soon as launch
   * bounds are known so the Stage appears while libGDX / AppContext init finishes.
   */
  public static void prepare(Rectangle launchScreenBounds) {
    Platform.runLater(
        () -> {
          SettingsFxController ctrl = instance();
          ctrl.ensureChrome(launchScreenBounds);
          ctrl.raiseAndShow();
          LOGGER.info("SettingsFx Stage prepared (placeholder)");
        });
  }

  /**
   * Replaces the placeholder with live {@link AppContext} wiring. Must be called after bootstrap.
   */
  public static void show(Rectangle launchScreenBounds, AppContext appContext) {
    Objects.requireNonNull(appContext, "appContext");
    Platform.runLater(
        () -> {
          SettingsFxController ctrl = instance();
          ctrl.ensureChrome(launchScreenBounds);
          ctrl.ensureContent(appContext);
          ctrl.raiseAndShow();
          ctrl.packStageToContent();
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
  private void ensureChrome(Rectangle launchScreenBounds) {
    if (stage != null) {
      return;
    }

    launchBounds = launchScreenBounds;
    stage = new Stage();
    stage.setTitle(SettingsStrings.WINDOW_TITLE);
    stage.setScene(placeholderScene());
    URL logoIcon = AppIcons.class.getResource(AppIcons.LOGO_RESOURCE);
    if (logoIcon != null) {
      stage.getIcons().add(new Image(logoIcon.toExternalForm()));
    }
    stage.setMinWidth(MainControllerConfig.SETTINGS_MIN_WIDTH);
    stage.setMinHeight(MainControllerConfig.SETTINGS_MIN_HEIGHT);

    int width =
        launchScreenBounds != null
            ? Math.max(MainControllerConfig.SETTINGS_MIN_WIDTH, launchScreenBounds.width / 2)
            : MainControllerConfig.SETTINGS_DEFAULT_WIDTH;
    maxStageHeight =
        launchScreenBounds != null
            ? Math.max(
                MainControllerConfig.SETTINGS_MIN_HEIGHT,
                (int) Math.round(launchScreenBounds.height * 0.9))
            : MainControllerConfig.SETTINGS_DEFAULT_HEIGHT;
    int height = Math.min(MainControllerConfig.SETTINGS_DEFAULT_HEIGHT, maxStageHeight);
    if (launchScreenBounds != null) {
      stage.setX(launchScreenBounds.x + Math.max(0, (launchScreenBounds.width - width) / 2.0));
      stage.setY(launchScreenBounds.y + Math.max(0, (launchScreenBounds.height - height) / 2.0));
    }
    stage.setWidth(width);
    stage.setHeight(height);

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
   * Shrinks (or grows) the stage to the form's preferred height so the one-pager has no large empty
   * band under the sections. Width stays half-screen; height is clamped to {@link #maxStageHeight}.
   */
  private void packStageToContent() {
    if (stage == null || stage.getScene() == null || !contentReady) {
      return;
    }
    Parent root = stage.getScene().getRoot();
    double width = stage.getWidth();
    root.applyCss();
    root.autosize();
    root.resize(width, root.prefHeight(width));
    root.layout();

    double prefH = root.prefHeight(width);
    double frameChrome = 0;
    if (stage.isShowing() && stage.getScene().getHeight() > 0) {
      frameChrome = stage.getHeight() - stage.getScene().getHeight();
    }
    // Small slack so sub-pixel layout does not force a scrollbar on a fresh open.
    int height = (int) Math.ceil(prefH + frameChrome + 4);
    height = Math.max(MainControllerConfig.SETTINGS_MIN_HEIGHT, Math.min(height, maxStageHeight));
    stage.setHeight(height);

    if (launchBounds != null) {
      stage.setY(launchBounds.y + Math.max(0, (launchBounds.height - height) / 2.0));
    }
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
