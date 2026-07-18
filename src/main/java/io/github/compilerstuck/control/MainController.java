package io.github.compilerstuck.control;

import com.formdev.flatlaf.FlatLightLaf;
import io.github.compilerstuck.control.catalog.AlgorithmCatalog;
import io.github.compilerstuck.control.catalog.AlgorithmDescriptor;
import io.github.compilerstuck.control.catalog.VisualizationCatalog;
import io.github.compilerstuck.control.catalog.VisualizationDescriptor;
import io.github.compilerstuck.control.config.DelayStrategy;
import io.github.compilerstuck.control.config.MainControllerConfig;
import io.github.compilerstuck.control.config.UserPreferences;
import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.model.SortingSessionManager;
import io.github.compilerstuck.control.model.SortingStateManager;
import io.github.compilerstuck.control.render.ProcessingContext;
import io.github.compilerstuck.control.render.ProcessingLoadedImage;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.control.ui.ResultsTableRenderer;
import io.github.compilerstuck.control.ui.Settings;
import io.github.compilerstuck.control.ui.TimeEstimateFormat;
import io.github.compilerstuck.control.ui.UiTheme;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import io.github.compilerstuck.sound.MidiSys;
import io.github.compilerstuck.sound.SilentSound;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.Visualization;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import processing.core.PApplet;

/**
 * Main controller for the sorting algorithm visualizer.
 *
 * <p>Manages the Processing display loop, user interaction, and coordinates between visualization,
 * sound, algorithm execution, and UI settings.
 *
 * <p>Composition root: {@link #main} / {@link #setup} wires {@link AppContext}; Settings and UI
 * panels use {@link AppContext} only. Residual statics ({@link #processing}, {@link #sound}, {@link
 * #app}) exist for Processing bootstrap and shutdown — not as a Settings service locator.
 */
public class MainController extends PApplet implements RenderContext {
  private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

  /** Set in {@link #setup} to this instance (Processing bootstrap). */
  public static ProcessingContext processing;

  /** Non-null after {@link #initializeComponents()}; never assign null. */
  public static Sound sound = new SilentSound(null);

  /** Live {@link AppContext} after {@link #initializeState()}; preferred API for UI and tests. */
  public static AppContext app;

  // Launch flags parsed from CLI before the PApplet is constructed
  private static boolean launchFullscreen = false;
  private static boolean launchPortrait = false;

  /** 1-based display index from {@code --display=N}; {@code <= 0} means primary. */
  private static int launchDisplay = 0;

  // Instance fields (preferred pattern)
  private int size;
  private ArrayController arrayController;
  private List<SortingAlgorithm> algorithms;
  private Visualization visualization;
  private ColorGradient colorGradient;
  private Settings settings;

  private SortingStateManager stateManager;
  private SortingSessionManager sessionManager;
  private AppContext appContext;
  private final ResultsTableRenderer resultsTableRenderer = new ResultsTableRenderer();

  private boolean fullScreen = false;
  private boolean portrait = false;
  private Rectangle fullscreenBounds;

  /**
   * Entry point for the sorting visualizer application.
   *
   * @param passedArgs {@code fullscreen}, {@code portrait}, optional {@code --display=N} (1-based)
   */
  public static void main(String[] passedArgs) {
    parseLaunchArgs(passedArgs);
    setupUITheme();

    String[] appletArgs = new String[] {"io.github.compilerstuck.control.MainController"};
    PApplet.main(concat(appletArgs, passedArgs));
  }

  /**
   * Parses known launch tokens. {@code fullscreen} wins over {@code portrait} when both are
   * present. Unknown args are ignored (still forwarded to Processing).
   */
  static void parseLaunchArgs(String[] passedArgs) {
    launchFullscreen = false;
    launchPortrait = false;
    launchDisplay = 0;
    if (passedArgs == null) {
      return;
    }
    for (String arg : passedArgs) {
      if (arg == null) {
        continue;
      }
      if ("fullscreen".equalsIgnoreCase(arg)) {
        launchFullscreen = true;
      } else if ("portrait".equalsIgnoreCase(arg)) {
        launchPortrait = true;
      } else if (arg.regionMatches(true, 0, "--display=", 0, "--display=".length())) {
        launchDisplay = parseDisplayIndex(arg.substring("--display=".length()));
      }
    }
    if (launchFullscreen) {
      launchPortrait = false;
    }
  }

  private static int parseDisplayIndex(String value) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      LOGGER.warning("Invalid --display value: " + value);
      return 0;
    }
  }

  /** Package-visible for tests. */
  static boolean isLaunchFullscreen() {
    return launchFullscreen;
  }

  /** Package-visible for tests. */
  static boolean isLaunchPortrait() {
    return launchPortrait;
  }

  /** Package-visible for tests. */
  static int getLaunchDisplay() {
    return launchDisplay;
  }

  /**
   * Configures the application's UI theme using FlatLaf light theme aligned with {@link UiTheme}.
   */
  private static void setupUITheme() {
    FlatLightLaf.setup();
    try {
      UIManager.setLookAndFeel(new FlatLightLaf());
    } catch (UnsupportedLookAndFeelException e) {
      LOGGER.log(Level.WARNING, "Failed to set FlatLightLaf look and feel", e);
    }
    UiTheme.applyLookAndFeelDefaults();
  }

  @Override
  public void delay(int ms) {
    if (appContext != null && appContext.isUseStepEngine()) {
      try {
        appContext.getFrameGate().awaitStep();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      return;
    }
    super.delay(ms);
  }

  /** Processing settings hook. Configures window size and rendering mode. */
  @Override
  public void settings() {
    fullScreen = launchFullscreen;
    portrait = launchPortrait;
    if (fullScreen) {
      // Avoid Processing fullScreen(P3D): JOGL often picks the wrong size/monitor
      // when displays differ in resolution or are stacked vertically.
      fullscreenBounds = FullscreenDisplay.resolveBounds(launchDisplay);
      this.size(fullscreenBounds.width, fullscreenBounds.height, P3D);
    } else if (portrait) {
      this.size(MainControllerConfig.PORTRAIT_WIDTH, MainControllerConfig.PORTRAIT_HEIGHT, P3D);
    } else {
      this.size(MainControllerConfig.STANDARD_WIDTH, MainControllerConfig.STANDARD_HEIGHT, P3D);
    }
    noSmooth();
  }

  /**
   * Processing setup hook. Initializes all components including visualization, sound, algorithms,
   * and settings UI.
   */
  @Override
  public void setup() {
    configureWindow();

    processing = this;

    initializeComponents();
    initializeState();

    try {
      settings = new Settings(appContext);
    } catch (UnsupportedLookAndFeelException
        | ClassNotFoundException
        | InstantiationException
        | IllegalAccessException e) {
      LOGGER.log(Level.SEVERE, "Failed to initialize Settings UI", e);
    }
  }

  /** Configures window size, position, and title. */
  private void configureWindow() {
    if (fullScreen) {
      if (fullscreenBounds == null) {
        fullscreenBounds = FullscreenDisplay.resolveBounds(launchDisplay);
      }
      // Never call JOGL setFullscreen on the animation thread — it aborts the sketch.
      FullscreenDisplay.applyAsync(surface, fullscreenBounds);
    } else {
      centerWindowOnPrimaryScreen();
      surface.setResizable(false);
    }

    surface.setTitle("Sorting Algorithm Visualizer");
    frameRate(MainControllerConfig.TARGET_FRAME_RATE);
    textSize(MainControllerConfig.MAX_TEXT_SIZE); // Processing workaround
  }

  private void centerWindowOnPrimaryScreen() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    Rectangle screen = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
    int w = portrait ? MainControllerConfig.PORTRAIT_WIDTH : MainControllerConfig.STANDARD_WIDTH;
    int h = portrait ? MainControllerConfig.PORTRAIT_HEIGHT : MainControllerConfig.STANDARD_HEIGHT;
    int x = screen.x + Math.max(0, (screen.width - w) / 2);
    int y = screen.y + Math.max(0, (screen.height - h) / 2);
    surface.setLocation(x, y);
  }

  /** Initializes visualization, sound, color gradient, and algorithms. */
  private void initializeComponents() {
    UserPreferences prefs = UserPreferences.load();
    size = prefs.getArraySize();
    arrayController = new ArrayController(size);

    try {
      sound = new MidiSys(arrayController);
    } catch (MidiUnavailableException e) {
      LOGGER.log(Level.WARNING, "Sound system unavailable, running without audio", e);
      sound = new SilentSound(arrayController);
    }
    sound.setIsMuted(prefs.isMuted());

    colorGradient = new ColorGradient(Color.BLACK, Color.RED, Color.WHITE, "Black -> Red", size);

    VisualizationDescriptor vizDesc = VisualizationCatalog.findById(prefs.getVisualizationId());
    visualization = vizDesc.factory().create(arrayController, colorGradient, sound, this);

    AlgorithmDescriptor algDesc = AlgorithmCatalog.findById(prefs.getAlgorithmId());
    SortingAlgorithm algorithm = algDesc.factory().apply(arrayController, this);
    algorithms = new ArrayList<>();
    algorithms.add(algorithm);

    arrayController.setProcessingContext(this);
  }

  /** Initializes state managers for thread coordination. */
  private void initializeState() {
    UserPreferences prefs = UserPreferences.load();
    stateManager = new SortingStateManager();
    sessionManager = new SortingSessionManager(arrayController, sound, stateManager);

    appContext = new AppContext(arrayController, stateManager, sessionManager, prefs);
    appContext.setSize(size);
    appContext.setRenderContext(this);
    appContext.setSound(sound);
    appContext.setColorGradient(colorGradient);
    appContext.setVisualization(visualization);
    appContext.setAlgorithm(algorithms.get(0));
    algorithms.get(0).setOperationReporter(stateManager::setCurrentOperation);
    appContext.setSpeedLevel(prefs.getSpeedLevel());
    app = appContext;
  }

  /**
   * Processing draw hook. Called repeatedly to update and render the visualization. Handles state
   * transitions and coordinates between simulation and rendering.
   */
  @Override
  public void draw() {
    try {
      if (stateManager.shouldShowResults() && stateManager.shouldShowComparisonTable()) {
        handleResultsDisplay();
      } else if (stateManager.shouldRestart()) {
        handleRestart();
      } else if (stateManager.isRunning()) {
        handleActiveSort();
      } else {
        handleIdleState();
      }

      if (exitCalled()) {
        shutdown();
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error during draw loop", e);
    }
  }

  /**
   * Draws the comparison results table. Completes session teardown once so Settings inputs are
   * re-enabled while the table stays on screen.
   */
  private void handleResultsDisplay() {
    if (stateManager.shouldRestart()) {
      // Keep showResults so the table remains visible; still unlock the Settings UI.
      finishSession(false);
    }
    if (stateManager.shouldContinueExecution()) {
      printResults();
    }
  }

  /** Handles restart/reset state after algorithms complete (no comparison table). */
  private void handleRestart() {
    finishSession(true);
  }

  /**
   * Tears down an ended sorting session and re-enables Settings.
   *
   * @param clearResults when true, leave the results-table display mode
   */
  private void finishSession(boolean clearResults) {
    stateManager.setRunning(false);
    stateManager.setStartRequested(false);
    stateManager.setRestart(false);
    if (clearResults) {
      stateManager.setShowResults(false);
    }

    sound.mute(true);
    sound.mute(false);

    sessionManager.printTimestampsToConsole(new ArrayList<>(currentAlgorithms()));
    arrayController.resetMeasurements();
    stateManager.setCurrentOperation("Waiting");

    stateManager.setContinueExecution(true);
    arrayController.resetArray();

    if (settings != null) {
      settings.setProgressBar(100);
      settings.setEnableInputs(true);
      settings.setEnableCancelButton(false);
    }

    frameRate(MainControllerConfig.TARGET_FRAME_RATE);
    if (appContext != null) {
      appContext.getFrameGate().reset();
    }
  }

  /** Handles rendering and updates during active sorting. */
  private void handleActiveSort() {
    if (appContext != null && appContext.isUseStepEngine()) {
      appContext.getFrameGate().grant(appContext.getStepsPerFrame());
    }
    currentVisualization().update();
    arrayController.update();

    if (stateManager.shouldPrintMeasurements()) {
      printMeasurements();
    }

    if (settings != null) {
      settings.setProgressBar((int) (arrayController.getSortedPercentage() * 100));
    }
  }

  /** Handles idle state or starting new sort session. */
  private void handleIdleState() {
    if (stateManager.requestedStart()) {
      startSortingSession();
    } else {
      currentVisualization().update();
      if (stateManager.shouldPrintMeasurements()) {
        printMeasurements();
      }
    }
  }

  /** Initiates a new sorting session. */
  private void startSortingSession() {
    if (stateManager.shouldPrintMeasurements()) {
      printMeasurements();
    }

    stateManager.setShowResults(false);

    try {
      Thread.sleep(MainControllerConfig.SETUP_DELAY);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.log(Level.WARNING, "Thread interrupted during setup delay", e);
    }

    stateManager.setRunning(true);

    if (settings != null) {
      settings.setEnableInputs(false);
    }

    arrayController.resetArray();
    // Keep local mirrors aligned with AppContext (Settings writes there).
    algorithms = new ArrayList<>(currentAlgorithms());
    visualization = currentVisualization();

    if (appContext != null && appContext.isUseStepEngine()) {
      appContext.getFrameGate().reset();
      frameRate(MainControllerConfig.STEP_ENGINE_FRAME_RATE);
      for (SortingAlgorithm alg : algorithms) {
        alg.setDelayStrategy(DelayStrategy.ALWAYS);
      }
    } else {
      frameRate(MainControllerConfig.TARGET_FRAME_RATE);
    }

    sessionManager.startSortingSession(algorithms);
  }

  /** Settings updates {@link AppContext}; prefer that over the local mirror. */
  private Visualization currentVisualization() {
    if (appContext != null) {
      Visualization fromApp = appContext.getVisualization();
      if (fromApp != null) {
        return fromApp;
      }
    }
    return visualization;
  }

  /** Settings updates {@link AppContext}; prefer that over the local mirror. */
  private List<SortingAlgorithm> currentAlgorithms() {
    if (appContext != null) {
      List<SortingAlgorithm> fromApp = appContext.getAlgorithms();
      if (!fromApp.isEmpty()) {
        return fromApp;
      }
    }
    return algorithms;
  }

  /** Handles ESC key press to gracefully shutdown the application. */
  @Override
  public void keyPressed() {
    if (keyCode == ESC) {
      sound.mute(true);
      shutdown();
    }
  }

  /** Cancels the active sorting session via {@link AppContext} when available. */
  public static void cancelSorting() {
    if (app != null) {
      app.cancelSorting();
      return;
    }
    if (processing instanceof MainController controller) {
      if (controller.sessionManager != null) {
        controller.sessionManager.cancel();
      } else if (controller.stateManager != null) {
        controller.stateManager.setContinueExecution(false);
      }
    }
  }

  /**
   * Gracefully shuts down the application. Cancels any active sort and exits the Processing loop
   * and JVM.
   */
  public static void shutdown() {
    cancelSorting();

    if (app != null) {
      app.persistPreferences();
    }

    if (sound != null) {
      sound.dispose();
    }

    if (processing instanceof PApplet p) {
      p.noLoop();
      p.exit();
    }
  }

  /**
   * Displays a table of algorithm comparison results on screen. Shows metrics like comparisons,
   * swaps, writes, and execution time.
   */
  private void printResults() {
    resultsTableRenderer.render(
        this,
        currentAlgorithms(),
        sessionManager.getComparisons(),
        sessionManager.getRealTime(),
        sessionManager.getSwaps(),
        sessionManager.getWritesMain(),
        sessionManager.getWritesAux());
  }

  /** Prints live measurements during sorting (comparisons, swaps, time, etc). */
  private void printMeasurements() {
    stroke(255);
    fill(255);

    int textSize = MainControllerConfig.scaleToWidth(MainControllerConfig.TEXT_Y_OFFSET, width);
    int textXPosition =
        MainControllerConfig.scaleToWidth(MainControllerConfig.TEXT_X_OFFSET, width);
    int lineHeight =
        MainControllerConfig.scaleToWidth(MainControllerConfig.LINE_HEIGHT_OFFSET, width);
    textSize(textSize);

    String[] labels = {
      stateManager.getCurrentOperation(),
      (int) (arrayController.getSortedPercentage() * 100)
          + "% Sorted ("
          + arrayController.getSegments()
          + " Segments)",
      String.format("%,d", arrayController.getComparisons()) + " Comparisons",
      "Estimated time: ~" + TimeEstimateFormat.format(arrayController.getRealTime()) + "ms",
      String.format("%,d", arrayController.getSwaps()) + " Swaps",
      String.format("%,d", arrayController.getWrites()) + " Writes to main array",
      String.format("%,d", arrayController.getWritesAux()) + " Writes to auxiliary array",
      arrayController.getLength() + " Elements"
    };

    for (int i = 0; i < labels.length; i++) {
      text(labels[i], textXPosition, lineHeight * (i + 1));
    }
  }

  // RenderContext implementation - delegates to Processing PApplet methods

  @Override
  public void background(int rgb) {
    super.background(rgb);
  }

  @Override
  public void fill(int rgb) {
    super.fill(rgb);
  }

  @Override
  public void fill(int rgb, float alpha) {
    super.fill(rgb, alpha);
  }

  @Override
  public void textSize(int size) {
    super.textSize(Math.max(1, size));
  }

  @Override
  public void text(String str, float x, float y) {
    super.text(str, x, y);
  }

  @Override
  public void stroke(int rgb) {
    super.stroke(rgb);
  }

  @Override
  public void stroke(int rgb, float alpha) {
    super.stroke(rgb, alpha);
  }

  @Override
  public void noStroke() {
    super.noStroke();
  }

  @Override
  public void noFill() {
    super.noFill();
  }

  @Override
  public void rect(float x, float y, float w, float h) {
    super.rect(x, y, w, h);
  }

  @Override
  public void line(float x1, float y1, float x2, float y2) {
    super.line(x1, y1, x2, y2);
  }

  @Override
  public void line(float x1, float y1, float z1, float x2, float y2, float z2) {
    super.line(x1, y1, z1, x2, y2, z2);
  }

  @Override
  public void ellipse(float x, float y, float w, float h) {
    super.ellipse(x, y, w, h);
  }

  @Override
  public void circle(float x, float y, float extent) {
    super.circle(x, y, extent);
  }

  @Override
  public void lights() {
    super.lights();
  }

  @Override
  public void pushMatrix() {
    super.pushMatrix();
  }

  @Override
  public void popMatrix() {
    super.popMatrix();
  }

  @Override
  public void translate(float x, float y) {
    super.translate(x, y);
  }

  @Override
  public void translate(float x, float y, float z) {
    super.translate(x, y, z);
  }

  @Override
  public void rotateX(float angle) {
    super.rotateX(angle);
  }

  @Override
  public void rotateY(float angle) {
    super.rotateY(angle);
  }

  @Override
  public void rotateZ(float angle) {
    super.rotateZ(angle);
  }

  @Override
  public void box(float size) {
    super.box(size);
  }

  @Override
  public void box(float w, float h, float d) {
    super.box(w, h, d);
  }

  @Override
  public float frameRate() {
    return this.frameRate;
  }

  @Override
  public void loadPixels() {
    super.loadPixels();
  }

  @Override
  public void updatePixels() {
    super.updatePixels();
  }

  @Override
  public int[] pixels() {
    return this.pixels;
  }

  // color/red/green/blue are final on PApplet and already satisfy RenderContext

  @Override
  public ProcessingLoadedImage loadImage(String path) {
    return new ProcessingLoadedImage(super.loadImage(path));
  }

  @Override
  public void setResizable(boolean resizable) {
    getSurface().setResizable(resizable);
  }

  @Override
  public int getWidth() {
    return super.width;
  }

  @Override
  public int getHeight() {
    return super.height;
  }
}
