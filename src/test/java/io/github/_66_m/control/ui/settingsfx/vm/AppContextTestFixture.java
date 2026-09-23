package io.github._66_m.control.ui.settingsfx.vm;

import io.github._66_m.control.AppContext;
import io.github._66_m.control.config.SettingsDefaults;
import io.github._66_m.control.config.UserPreferences;
import io.github._66_m.control.model.ArrayController;
import io.github._66_m.control.model.SnapshotPublisher;
import io.github._66_m.control.model.SortingSessionManager;
import io.github._66_m.control.model.SortingStateManager;
import io.github._66_m.control.render.DelayContext;
import io.github._66_m.control.render.FakeRenderSystem;
import io.github._66_m.control.render.asset.FakeImageRepository;
import io.github._66_m.sound.SilentSound;
import io.github._66_m.visual.Bars;
import io.github._66_m.visual.gradient.ColorGradient;
import java.awt.Color;

/**
 * Builds a headless {@link AppContext} suitable for Settings view-model unit tests (no libGDX
 * window / MIDI).
 */
public final class AppContextTestFixture {

  public final ArrayController arrayModel;
  public final SnapshotPublisher snapshotPublisher;
  public final SortingStateManager stateManager;
  public final SortingSessionManager sessionManager;
  public final SilentSound sound;
  public final FakeRenderSystem renderSystem;
  public final DelayContext delayContext;
  public final ColorGradient gradient;
  public final UserPreferences preferences;
  public final AppContext app;

  public AppContextTestFixture() {
    this(SettingsDefaults.DEFAULT_ARRAY_SIZE);
  }

  public AppContextTestFixture(int arraySize) {
    arrayModel = new ArrayController(arraySize);
    snapshotPublisher = new SnapshotPublisher();
    snapshotPublisher.publish(arrayModel);
    stateManager = new SortingStateManager();
    sound = new SilentSound(snapshotPublisher.publishedView());
    sessionManager = new SortingSessionManager(arrayModel, sound, stateManager, 0, 0);
    renderSystem = new FakeRenderSystem(200, 100);
    delayContext = () -> {};
    gradient = new ColorGradient(Color.BLACK, Color.RED, Color.WHITE, "Black -> Red");
    gradient.updateGradient(arraySize);

    // Fresh instance (do not load machine prefs; keeps tests hermetic).
    preferences = new UserPreferences();
    preferences.setArraySize(arraySize);
    preferences.setAlgorithmId(SettingsDefaults.DEFAULT_ALGORITHM_ID);
    preferences.setVisualizationId(SettingsDefaults.DEFAULT_VISUALIZATION_ID);
    preferences.setSpeedLevel(SettingsDefaults.DEFAULT_SPEED_LEVEL);
    preferences.setMuted(SettingsDefaults.DEFAULT_MUTED);

    app = new AppContext(arrayModel, stateManager, sessionManager, preferences);
    app.setSize(arraySize);
    app.setSnapshotPublisher(snapshotPublisher);
    app.setSound(sound);
    app.setColorGradient(gradient);
    app.setGraphics(renderSystem, delayContext);
    app.setImageRepository(new FakeImageRepository());
    arrayModel.setDelayContext(delayContext);
    app.setVisualization(
        new Bars(snapshotPublisher.publishedView(), gradient, sound, renderSystem));
  }

  /** Simulates an active sort for enablement tests without starting a real session. */
  public void setRunning(boolean running) {
    stateManager.setRunning(running);
  }
}
