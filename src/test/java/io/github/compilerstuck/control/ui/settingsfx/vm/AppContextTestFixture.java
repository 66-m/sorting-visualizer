package io.github.compilerstuck.control.ui.settingsfx.vm;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.control.config.UserPreferences;
import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.model.SortingSessionManager;
import io.github.compilerstuck.control.model.SortingStateManager;
import io.github.compilerstuck.control.render.HeadlessRenderContext;
import io.github.compilerstuck.sound.SilentSound;
import io.github.compilerstuck.visual.Bars;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

/**
 * Builds a headless {@link AppContext} suitable for Settings view-model unit tests (no Processing /
 * NEWT / MIDI).
 */
public final class AppContextTestFixture {

  public final ArrayController arrayController;
  public final SortingStateManager stateManager;
  public final SortingSessionManager sessionManager;
  public final SilentSound sound;
  public final HeadlessRenderContext renderContext;
  public final ColorGradient gradient;
  public final UserPreferences preferences;
  public final AppContext app;

  public AppContextTestFixture() {
    this(SettingsDefaults.DEFAULT_ARRAY_SIZE);
  }

  public AppContextTestFixture(int arraySize) {
    arrayController = new ArrayController(arraySize);
    stateManager = new SortingStateManager();
    sound = new SilentSound(arrayController);
    sessionManager = new SortingSessionManager(arrayController, sound, stateManager, 0, 0);
    renderContext = new HeadlessRenderContext(200, 100);
    gradient = new ColorGradient(Color.BLACK, Color.RED, Color.WHITE, "Black -> Red");
    gradient.updateGradient(arraySize);

    // Fresh instance (do not load machine prefs — keeps tests hermetic).
    preferences = new UserPreferences();
    preferences.setArraySize(arraySize);
    preferences.setAlgorithmId(SettingsDefaults.DEFAULT_ALGORITHM_ID);
    preferences.setVisualizationId(SettingsDefaults.DEFAULT_VISUALIZATION_ID);
    preferences.setSpeedLevel(SettingsDefaults.DEFAULT_SPEED_LEVEL);
    preferences.setMuted(SettingsDefaults.DEFAULT_MUTED);
    preferences.setUseStepEngine(SettingsDefaults.DEFAULT_USE_STEP_ENGINE);

    app = new AppContext(arrayController, stateManager, sessionManager, preferences);
    app.setSize(arraySize);
    app.setSound(sound);
    app.setColorGradient(gradient);
    app.setRenderContext(renderContext);
    app.setVisualization(new Bars(arrayController, gradient, sound, renderContext));
  }

  /** Simulates an active sort for enablement tests without starting a real session. */
  public void setRunning(boolean running) {
    stateManager.setRunning(running);
  }
}
