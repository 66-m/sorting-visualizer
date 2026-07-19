package io.github.compilerstuck.control;

import io.github.compilerstuck.control.config.RunAllEntryPref;
import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.control.config.ShuffleType;
import io.github.compilerstuck.control.config.UserPreferences;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.model.FrameGate;
import io.github.compilerstuck.control.model.SnapshotPublisher;
import io.github.compilerstuck.control.model.SortingSessionManager;
import io.github.compilerstuck.control.model.SortingStateManager;
import io.github.compilerstuck.control.render.DelayContext;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.control.render.asset.ImageHandle;
import io.github.compilerstuck.control.render.asset.ImageRepository;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import io.github.compilerstuck.sound.SilentSound;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.ImageSourceVisualization;
import io.github.compilerstuck.visual.Visualization;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Live collaborator bundle for a running visualizer session. Provides the operations the Settings
 * UI needs without depending on the game class.
 */
public final class AppContext {
  private static final Logger LOGGER = Logger.getLogger(AppContext.class.getName());

  private final ArrayController arrayController;
  private final SortingStateManager stateManager;
  private final SortingSessionManager sessionManager;
  private final FrameGate frameGate = new FrameGate();
  private final UserPreferences preferences;
  private SnapshotPublisher snapshotPublisher;
  private Sound sound;
  private ColorGradient colorGradient;
  private Visualization visualization;
  private final List<SortingAlgorithm> algorithms = new ArrayList<>();
  private int size;
  private RenderSystem renderSystem;
  private DelayContext delayContext;
  private ImageRepository imageRepository;
  private Runnable shutdownHandler;

  private int speedLevel = SettingsDefaults.DEFAULT_SPEED_LEVEL; // 1–5, default Normal
  private int stepsPerFrame =
      SettingsDefaults.stepsPerFrame(SettingsDefaults.DEFAULT_SPEED_LEVEL);
  private boolean perfStatsEnabled;

  public AppContext(
      ArrayController arrayController,
      SortingStateManager stateManager,
      SortingSessionManager sessionManager,
      UserPreferences preferences) {
    this.arrayController = arrayController;
    this.stateManager = stateManager;
    this.sessionManager = sessionManager;
    this.preferences = preferences != null ? preferences : UserPreferences.load();
    this.size = this.preferences.getArraySize();
    this.speedLevel = this.preferences.getSpeedLevel();
    this.perfStatsEnabled = this.preferences.isPerfStats();
    if (sessionManager != null) {
      sessionManager.setFrameGate(frameGate);
    }
    applySpeedLevel();
  }

  public UserPreferences getPreferences() {
    return preferences;
  }

  public void persistPreferences() {
    preferences.setArraySize(size);
    preferences.setSpeedLevel(speedLevel);
    if (sound != null) {
      preferences.setMuted(sound.isMuted());
    }
    preferences.setShuffleType(arrayController.getShuffleType());
    preferences.setPrintMeasurements(stateManager.shouldPrintMeasurements());
    preferences.setShowComparisonTable(stateManager.shouldShowComparisonTable());
    preferences.setPerfStats(perfStatsEnabled);
    if (colorGradient != null) {
      preferences.setGradientName(colorGradient.getName());
      if (colorGradient.getColor1() != null) {
        preferences.setGradientColor1Rgb(colorGradient.getColor1().getRGB());
      }
      if (colorGradient.getColor2() != null) {
        preferences.setGradientColor2Rgb(colorGradient.getColor2().getRGB());
      }
    }
    preferences.save();
  }

  public ArrayController getArrayController() {
    return arrayController;
  }

  /**
   * Read-only published array for visuals and sound. Falls back to the live controller when no
   * publisher is wired (tests).
   */
  public ArrayModel getPublishedArray() {
    if (snapshotPublisher != null) {
      return snapshotPublisher.publishedView();
    }
    return arrayController;
  }

  public SnapshotPublisher getSnapshotPublisher() {
    return snapshotPublisher;
  }

  public void setSnapshotPublisher(SnapshotPublisher snapshotPublisher) {
    this.snapshotPublisher = snapshotPublisher;
    if (snapshotPublisher != null) {
      snapshotPublisher.publish(arrayController);
    }
  }

  /** Copy working → published and clear working markers. Safe when the sort worker is idle. */
  public void publishArraySnapshot() {
    if (snapshotPublisher != null) {
      snapshotPublisher.publish(arrayController);
    }
  }

  public SortingStateManager getStateManager() {
    return stateManager;
  }

  public SortingSessionManager getSessionManager() {
    return sessionManager;
  }

  public FrameGate getFrameGate() {
    return frameGate;
  }

  public int getStepsPerFrame() {
    return stepsPerFrame;
  }

  public void setStepsPerFrame(int stepsPerFrame) {
    this.stepsPerFrame = Math.max(1, stepsPerFrame);
  }

  public int getSpeedLevel() {
    return speedLevel;
  }

  /** Applies speed level 1–5 as steps granted per draw frame. */
  public void setSpeedLevel(int level1to5) {
    this.speedLevel = SettingsDefaults.clampSpeedLevel(level1to5);
    applySpeedLevel();
    preferences.setSpeedLevel(this.speedLevel);
    preferences.save();
  }

  private void applySpeedLevel() {
    stepsPerFrame = SettingsDefaults.stepsPerFrame(speedLevel);
  }

  public Sound getSound() {
    return sound;
  }

  public void setSound(Sound sound) {
    this.sound = sound != null ? sound : new SilentSound(arrayController);
  }

  public ColorGradient getColorGradient() {
    return colorGradient;
  }

  public void setColorGradient(ColorGradient colorGradient) {
    this.colorGradient = colorGradient;
    if (this.colorGradient != null) {
      this.colorGradient.updateGradient(size);
      preferences.setGradientName(this.colorGradient.getName());
      if (this.colorGradient.getColor1() != null) {
        preferences.setGradientColor1Rgb(this.colorGradient.getColor1().getRGB());
      }
      if (this.colorGradient.getColor2() != null) {
        preferences.setGradientColor2Rgb(this.colorGradient.getColor2().getRGB());
      }
      preferences.save();
    }
    if (visualization != null && this.colorGradient != null) {
      visualization.updateColorGradient(this.colorGradient);
    }
  }

  public Visualization getVisualization() {
    return visualization;
  }

  public void setVisualization(Visualization visualization) {
    this.visualization = visualization;
    bindImageRepository(visualization);
    if (visualization != null && colorGradient != null) {
      visualization.updateColorGradient(colorGradient);
    }
  }

  public void setVisualizationId(String id) {
    preferences.setVisualizationId(id);
    preferences.save();
  }

  /** Persists per-visualization appearance settings (JSON map in prefs). */
  public void saveVisualizationSettings(VisualizationSettings settings) {
    if (settings == null) {
      return;
    }
    preferences.putVisualSettings(settings);
    preferences.save();
  }

  public void setAlgorithmId(String id) {
    preferences.setAlgorithmId(id);
    preferences.save();
  }

  public void setMuted(boolean muted) {
    if (sound != null) {
      sound.setIsMuted(muted);
    }
    preferences.setMuted(muted);
    preferences.save();
  }

  public List<SortingAlgorithm> getAlgorithms() {
    return new ArrayList<>(algorithms);
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public RenderSystem getRenderSystem() {
    return renderSystem;
  }

  public void setRenderSystem(RenderSystem renderSystem) {
    this.renderSystem = renderSystem;
  }

  /** Recent canvas FPS, or {@code 0} if graphics are not wired yet. */
  public int getFramesPerSecond() {
    return renderSystem != null ? renderSystem.framesPerSecond() : 0;
  }

  public DelayContext getDelayContext() {
    return delayContext;
  }

  public void setDelayContext(DelayContext delayContext) {
    this.delayContext = delayContext;
  }

  /** Wires draw system and delay port. */
  public void setGraphics(RenderSystem renderSystem, DelayContext delay) {
    this.renderSystem = renderSystem;
    this.delayContext = delay;
  }

  /** Resizes the array and dependent components; refuses while a sort is running. */
  public void updateArraySize(int newSize) {
    if (stateManager != null && stateManager.isRunning()) {
      LOGGER.log(Level.WARNING, "Ignoring array resize to {0} while a sort is active", newSize);
      return;
    }
    this.size = newSize;
    if (colorGradient != null) {
      colorGradient.updateGradient(newSize);
    }
    if (visualization != null && colorGradient != null) {
      visualization.updateColorGradient(colorGradient);
    }
    for (SortingAlgorithm alg : algorithms) {
      if (alg.getAlternativeSize() == arrayController.getLength()) {
        alg.setAlternativeSize(newSize);
      }
    }
    arrayController.resize(newSize);
    publishArraySnapshot();
    preferences.setArraySize(newSize);
    preferences.save();
  }

  public void setStart(boolean shouldStart) {
    stateManager.setStartRequested(shouldStart);
  }

  public boolean isRunning() {
    return stateManager.isRunning();
  }

  public void cancelSorting() {
    frameGate.cancel();
    sessionManager.cancel();
  }

  public void setAlgorithms(List<SortingAlgorithm> algorithmList) {
    algorithms.clear();
    for (SortingAlgorithm alg : algorithmList) {
      if (alg.isSelected()) {
        algorithms.add(alg);
      }
    }
    applySpeedLevel();
  }

  public void setAlgorithm(SortingAlgorithm algorithm) {
    algorithms.clear();
    if (algorithm != null) {
      algorithms.add(algorithm);
    }
    applySpeedLevel();
  }

  public void setShowComparisonTable(boolean show) {
    stateManager.setShowComparisonTable(show);
    preferences.setShowComparisonTable(show);
    preferences.save();
  }

  public void setPrintMeasurements(boolean print) {
    stateManager.setPrintMeasurements(print);
    preferences.setPrintMeasurements(print);
    preferences.save();
  }

  public boolean isPerfStatsEnabled() {
    return perfStatsEnabled;
  }

  public void setPerfStatsEnabled(boolean enabled) {
    perfStatsEnabled = enabled;
    preferences.setPerfStats(enabled);
    preferences.save();
  }

  public void setShuffleType(ShuffleType shuffleType) {
    arrayController.setShuffleType(shuffleType);
    preferences.setShuffleType(shuffleType);
    preferences.save();
  }

  public void setImagePath(String path) {
    preferences.setImagePath(path != null ? path : "");
    preferences.save();
  }

  public ImageRepository getImageRepository() {
    return imageRepository;
  }

  public void setImageRepository(ImageRepository imageRepository) {
    this.imageRepository = imageRepository;
    bindImageRepository(visualization);
  }

  /**
   * Load and resize an image on the caller thread (render thread for GDX texture upload). Validates
   * nothing about the filesystem; Settings VM does NIO checks first.
   */
  public boolean loadImageForVisualization(ImageSourceVisualization viz, String path) {
    if (viz == null || imageRepository == null || renderSystem == null) {
      return false;
    }
    viz.bindRepository(imageRepository);
    ImageHandle handle =
        imageRepository.load(path, renderSystem.getWidth(), renderSystem.getHeight());
    if (handle == null) {
      return false;
    }
    viz.setImage(handle);
    setImagePath(path);
    return true;
  }

  private void bindImageRepository(Visualization visualization) {
    if (visualization instanceof ImageSourceVisualization imageViz && imageRepository != null) {
      imageViz.bindRepository(imageRepository);
    }
  }

  public void persistRunAll(boolean runAll, List<RunAllEntryPref> entries) {
    preferences.setRunAll(runAll);
    preferences.setRunAllEntries(entries);
    preferences.save();
  }

  public void shutdown() {
    persistPreferences();
    if (shutdownHandler != null) {
      shutdownHandler.run();
    }
  }

  /** Registers the composition-root quit path (typically {@code Game::shutdown}). */
  public void setShutdownHandler(Runnable shutdownHandler) {
    this.shutdownHandler = shutdownHandler;
  }
}
