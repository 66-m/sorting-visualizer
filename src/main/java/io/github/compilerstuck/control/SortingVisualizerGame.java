package io.github.compilerstuck.control;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.compilerstuck.control.catalog.AlgorithmCatalog;
import io.github.compilerstuck.control.catalog.AlgorithmDescriptor;
import io.github.compilerstuck.control.catalog.VisualizationCatalog;
import io.github.compilerstuck.control.catalog.VisualizationDescriptor;
import io.github.compilerstuck.control.config.GradientPreferences;
import io.github.compilerstuck.control.config.UserPreferences;
import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.model.SnapshotPublisher;
import io.github.compilerstuck.control.model.SortingSessionManager;
import io.github.compilerstuck.control.model.SortingStateManager;
import io.github.compilerstuck.control.render.FrameGateDelayContext;
import io.github.compilerstuck.control.render.GdxRenderSystem;
import io.github.compilerstuck.control.render.asset.AppAssets;
import io.github.compilerstuck.control.render.asset.GdxImageRepository;
import io.github.compilerstuck.control.render.asset.ImageRepository;
import io.github.compilerstuck.control.screen.ResultsScreen;
import io.github.compilerstuck.control.screen.VisualizerScreen;
import io.github.compilerstuck.control.ui.settingsfx.JavaFxBootstrap;
import io.github.compilerstuck.control.ui.settingsfx.SettingsFxController;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import io.github.compilerstuck.sound.MidiSys;
import io.github.compilerstuck.sound.SilentSound;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.ConfigurableVisualization;
import io.github.compilerstuck.visual.Visualization;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiUnavailableException;

/**
 * LibGDX {@link Game} composition root: assets, services, {@link AppContext}, and screen
 * navigation. JavaFX Settings talks to {@link AppContext} only.
 */
public final class SortingVisualizerGame extends Game {
  private static final Logger LOGGER = Logger.getLogger(SortingVisualizerGame.class.getName());

  private ArrayController arrayController;
  private SnapshotPublisher snapshotPublisher;
  private List<SortingAlgorithm> algorithms;
  private Visualization visualization;
  private ColorGradient colorGradient;
  private SortingStateManager stateManager;
  private SortingSessionManager sessionManager;
  private AppContext appContext;
  private AppAssets assets;
  private ImageRepository imageRepository;
  private GdxRenderSystem renderSystem;
  private Sound sound;
  private VisualizerScreen visualizerScreen;
  private ResultsScreen resultsScreen;
  private boolean shuttingDown;

  /** Seconds remaining before session start; {@code < 0} means inactive. */
  private float setupDelayRemaining = -1f;

  private int lastPublishedProgress = -1;
  private float progressPublishAccum;

  @Override
  public void create() {
    AppAssets loadedAssets = new AppAssets();
    loadedAssets.loadDefaults();
    this.assets = loadedAssets;
    imageRepository = new GdxImageRepository();
    renderSystem = new GdxRenderSystem(assets);

    SettingsFxController.prepare();

    UserPreferences prefs = UserPreferences.load();
    int size = prefs.getArraySize();
    arrayController = new ArrayController(size);
    snapshotPublisher = new SnapshotPublisher();
    snapshotPublisher.publish(arrayController);
    ArrayModel published = snapshotPublisher.publishedView();

    try {
      sound = new MidiSys(published);
    } catch (MidiUnavailableException e) {
      LOGGER.log(Level.WARNING, "Sound system unavailable, running without audio", e);
      sound = new SilentSound(published);
    }
    sound.setIsMuted(prefs.isMuted());

    colorGradient =
        GradientPreferences.resolve(
            prefs.getGradientName(),
            prefs.getGradientColor1Rgb(),
            prefs.getGradientColor2Rgb(),
            size);

    stateManager = new SortingStateManager();
    stateManager.setPrintMeasurements(prefs.isPrintMeasurements());
    stateManager.setShowComparisonTable(prefs.isShowComparisonTable());
    sessionManager = new SortingSessionManager(arrayController, sound, stateManager);

    appContext = new AppContext(arrayController, stateManager, sessionManager, prefs);
    appContext.setSize(size);
    appContext.setSnapshotPublisher(snapshotPublisher);
    appContext.setShutdownHandler(this::shutdown);
    FrameGateDelayContext delayContext = new FrameGateDelayContext(appContext.getFrameGate());
    arrayController.setDelayContext(delayContext);
    appContext.setGraphics(renderSystem, delayContext);
    appContext.setImageRepository(imageRepository);
    renderSystem.setImageRepository(imageRepository);
    appContext.setSound(sound);
    appContext.setColorGradient(colorGradient);
    appContext.setSpeedLevel(prefs.getSpeedLevel());

    // Bootstrap the selected visualization only; VisualizationViewModel adopts it and lazy-creates
    // others on demand (SettingsFxController.show is async on the JavaFX thread).
    VisualizationDescriptor vizDesc = VisualizationCatalog.findById(prefs.getVisualizationId());
    visualization = vizDesc.factory().create(published, colorGradient, sound, renderSystem);
    var savedVizSettings = prefs.getVisualSettingsMap().get(vizDesc.id());
    if (savedVizSettings != null && visualization instanceof ConfigurableVisualization cfg) {
      cfg.applySettings(savedVizSettings);
    }
    appContext.setVisualization(visualization);

    AlgorithmDescriptor algDesc = AlgorithmCatalog.findById(prefs.getAlgorithmId());
    SortingAlgorithm algorithm = algDesc.factory().apply(arrayController, delayContext);
    algorithm.setOperationReporter(stateManager::setCurrentOperation);
    algorithms = new ArrayList<>();
    algorithms.add(algorithm);
    appContext.setAlgorithm(algorithm);

    arrayController.setShuffleType(prefs.getShuffleType());

    visualizerScreen = new VisualizerScreen(this);
    resultsScreen = new ResultsScreen(this);
    setScreen(visualizerScreen);

    SettingsFxController.show(appContext);
  }

  public AppContext appContext() {
    return appContext;
  }

  public GdxRenderSystem renderSystem() {
    return renderSystem;
  }

  public ArrayController arrayController() {
    return arrayController;
  }

  public SortingStateManager stateManager() {
    return stateManager;
  }

  public SortingSessionManager sessionManager() {
    return sessionManager;
  }

  public Sound sound() {
    return sound;
  }

  public boolean perfStatsEnabled() {
    return LaunchArgs.perfStats() || (appContext != null && appContext.isPerfStatsEnabled());
  }

  public float setupDelayRemaining() {
    return setupDelayRemaining;
  }

  public void setSetupDelayRemaining(float setupDelayRemaining) {
    this.setupDelayRemaining = setupDelayRemaining;
  }

  public int lastPublishedProgress() {
    return lastPublishedProgress;
  }

  public void setLastPublishedProgress(int lastPublishedProgress) {
    this.lastPublishedProgress = lastPublishedProgress;
  }

  public float progressPublishAccum() {
    return progressPublishAccum;
  }

  public void setProgressPublishAccum(float progressPublishAccum) {
    this.progressPublishAccum = progressPublishAccum;
  }

  public void showVisualizer() {
    if (getScreen() != visualizerScreen) {
      setScreen(visualizerScreen);
    }
  }

  public void showResults() {
    if (getScreen() != resultsScreen) {
      setScreen(resultsScreen);
    }
  }

  public void drawWorld(float delta) {
    renderSystem.clear(15 / 255f, 15 / 255f, 15 / 255f);
    currentVisualization().render(delta);
  }

  public void publishThenDraw(float delta) {
    if (appContext != null) {
      appContext.publishArraySnapshot();
    } else if (snapshotPublisher != null) {
      snapshotPublisher.publish(arrayController);
    }
    drawWorld(delta);
  }

  public void finishSession(boolean clearResults) {
    stateManager.setRunning(false);
    stateManager.setStartRequested(false);
    stateManager.setRestart(false);
    stateManager.setFrameGateSuspended(false);
    setupDelayRemaining = -1f;
    if (clearResults) {
      stateManager.setShowResults(false);
    }

    sound.cutNotes();

    sessionManager.printTimestampsToConsole(new ArrayList<>(currentAlgorithms()));
    arrayController.resetMeasurements();
    stateManager.setCurrentOperation("Waiting");

    stateManager.setContinueExecution(true);
    arrayController.resetArray();

    if (appContext != null) {
      SettingsBridge bridge = appContext.settingsBridge();
      bridge.setProgress(100);
      lastPublishedProgress = 100;
      progressPublishAccum = 0f;
      bridge.setInputsEnabled(true);
      bridge.setCancelEnabled(false);
    } else {
      lastPublishedProgress = 100;
      progressPublishAccum = 0f;
    }

    if (appContext != null) {
      appContext.getFrameGate().reset();
    }
  }

  public void startSortingSessionBody() {
    stateManager.setRunning(true);
    if (appContext != null) {
      appContext.settingsBridge().setInputsEnabled(false);
    }

    arrayController.resetArray();
    algorithms = new ArrayList<>(currentAlgorithms());
    visualization = currentVisualization();

    if (appContext != null) {
      appContext.getFrameGate().reset();
    }

    sessionManager.startSortingSession(algorithms);
  }

  public Visualization currentVisualization() {
    if (appContext != null) {
      Visualization fromApp = appContext.getVisualization();
      if (fromApp != null) {
        return fromApp;
      }
    }
    return visualization;
  }

  public List<SortingAlgorithm> currentAlgorithms() {
    if (appContext != null) {
      List<SortingAlgorithm> fromApp = appContext.getAlgorithms();
      if (!fromApp.isEmpty()) {
        return fromApp;
      }
    }
    return algorithms;
  }

  public void cancelSorting() {
    if (appContext != null) {
      appContext.cancelSorting();
      return;
    }
    if (sessionManager != null) {
      sessionManager.cancel();
    } else if (stateManager != null) {
      stateManager.setContinueExecution(false);
    }
  }

  public void shutdown() {
    if (shuttingDown) {
      return;
    }
    shuttingDown = true;
    cancelSorting();
    if (appContext != null) {
      appContext.persistPreferences();
    }
    if (sound != null) {
      sound.dispose();
    }
    JavaFxBootstrap.shutdown();
    if (Gdx.app != null) {
      Gdx.app.exit();
    }
  }

  @Override
  public void dispose() {
    if (visualizerScreen != null) {
      visualizerScreen.dispose();
      visualizerScreen = null;
    }
    if (resultsScreen != null) {
      resultsScreen.dispose();
      resultsScreen = null;
    }
    if (renderSystem != null) {
      renderSystem.dispose();
      renderSystem = null;
    }
    if (imageRepository != null) {
      imageRepository.dispose();
      imageRepository = null;
    }
    if (assets != null) {
      assets.dispose();
      assets = null;
    }
    if (!shuttingDown) {
      shuttingDown = true;
      cancelSorting();
      if (appContext != null) {
        appContext.persistPreferences();
      }
      if (sound != null) {
        sound.dispose();
      }
      JavaFxBootstrap.shutdown();
    }
  }
}
