package io.github.compilerstuck.control.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.SortingVisualizerGame;
import io.github.compilerstuck.control.config.AppConfig;
import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.model.SortingStateManager;
import io.github.compilerstuck.control.render.GdxRenderSystem;
import io.github.compilerstuck.control.render.HudRenderer;
import io.github.compilerstuck.control.render.PerfOverlay;
import io.github.compilerstuck.control.render.PerfOverlayContext;
import io.github.compilerstuck.visual.Visualization;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Main visualization mode: idle, setup delay, active sort (Phase 6 snapshot frame order), HUD. */
public final class VisualizerScreen implements Screen {
  private static final Logger LOGGER = Logger.getLogger(VisualizerScreen.class.getName());

  private final SortingVisualizerGame game;
  private final HudRenderer hudRenderer = new HudRenderer();
  private final PerfOverlay perfOverlay = new PerfOverlay();
  private final PerfOverlayContext perfContext = new PerfOverlayContext();

  public VisualizerScreen(SortingVisualizerGame game) {
    this.game = game;
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor(
        new InputAdapter() {
          @Override
          public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.Q
                && (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                    || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
              game.shutdown();
              return true;
            }
            if (keycode == Input.Keys.ESCAPE) {
              SortingStateManager state = game.stateManager();
              if (state != null && (state.isRunning() || game.setupDelayRemaining() >= 0f)) {
                game.cancelSorting();
              } else {
                focusSettingsWindow();
              }
              return true;
            }
            return false;
          }
        });
  }

  @Override
  public void render(float delta) {
    SortingStateManager stateManager = game.stateManager();
    GdxRenderSystem renderSystem = game.renderSystem();
    ArrayController arrayController = game.arrayController();

    if (stateManager.shouldShowResults() && stateManager.shouldShowComparisonTable()) {
      game.showResults();
      return;
    }

    try {
      float frameDelta = renderSystem.deltaTime();
      renderSystem.beginFrame();

      if (stateManager.shouldRestart()) {
        game.finishSession(true);
        game.publishThenDraw(frameDelta);
      } else if (stateManager.isRunning()) {
        if (stateManager.isFrameGateSuspended()) {
          // Worker is in a timed pause (not consuming credits); keep animating without pacing.
          game.publishThenDraw(frameDelta);
        } else {
          handleActiveSort(frameDelta);
        }
      } else {
        handleIdleState(frameDelta);
      }

      renderSystem.endWorld();

      hudRenderer.drawWatermark(renderSystem);
      // Peek only; requestedStart() would consume a Run click made during a slow draw.
      if (stateManager.shouldPrintMeasurements()
          && (stateManager.isRunning() || !stateManager.isStartRequested())) {
        hudRenderer.drawMeasurements(renderSystem, stateManager, arrayController);
      }

      if (game.perfStatsEnabled()) {
        fillPerfContext(renderSystem, stateManager, arrayController);
        perfOverlay.draw(renderSystem, renderSystem.lastFrameStats(), perfContext);
      }

      renderSystem.endFrame();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error during visualizer render", e);
      // A failed frame must not leave the sort worker blocked forever in awaitStep().
      if (game.appContext() != null) {
        game.appContext().getFrameGate().drain();
      }
    }
  }

  private void fillPerfContext(
      GdxRenderSystem renderSystem,
      SortingStateManager stateManager,
      ArrayController arrayController) {
    perfContext.width = renderSystem.getWidth();
    perfContext.height = renderSystem.getHeight();
    perfContext.arrayLength = arrayController != null ? arrayController.getLength() : 0;
    perfContext.running = stateManager != null && stateManager.isRunning();
    AppContext app = game.appContext();
    if (app != null) {
      perfContext.stepsPerFrame = app.getStepsPerFrame();
      Visualization viz = app.getVisualization();
      perfContext.visualization = viz != null ? viz.getName() : "";
    } else {
      perfContext.stepsPerFrame = 0;
      perfContext.visualization = "";
    }
  }

  private void focusSettingsWindow() {
    AppContext app = game.appContext();
    if (app != null) {
      app.settingsBridge().focusSettings();
    }
  }

  private void handleActiveSort(float delta) {
    if (game.appContext() != null) {
      try {
        game.appContext().getFrameGate().awaitIdle();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    if (game.appContext() != null) {
      game.appContext().publishArraySnapshot();
    }
    if (game.appContext() != null) {
      int steps =
          game.stateManager().isShuffling()
              ? AppConfig.shuffleStepsForDelta(delta)
              : game.appContext().getStepsPerFrame();
      game.appContext().getFrameGate().grant(steps);
    }
    game.drawWorld(delta);
    game.arrayController().update();
    publishProgressIfDue(delta, (int) (game.arrayController().getSortedPercentage() * 100));
  }

  private void publishProgressIfDue(float delta, int progress) {
    game.setProgressPublishAccum(game.progressPublishAccum() + delta);
    boolean boundary = progress <= 0 || progress >= 100;
    boolean due = game.progressPublishAccum() >= 0.1f || boundary;
    if (due && progress != game.lastPublishedProgress()) {
      if (game.appContext() != null) {
        game.appContext().settingsBridge().setProgress(progress);
      }
      game.setLastPublishedProgress(progress);
      game.setProgressPublishAccum(0f);
    }
  }

  private void handleIdleState(float delta) {
    SortingStateManager stateManager = game.stateManager();
    if (game.setupDelayRemaining() >= 0f) {
      if (!stateManager.shouldContinueExecution()) {
        game.finishSession(true);
        game.publishThenDraw(delta);
        return;
      }
      game.publishThenDraw(delta);
      game.setSetupDelayRemaining(game.setupDelayRemaining() - delta);
      if (game.setupDelayRemaining() <= 0f) {
        game.setSetupDelayRemaining(-1f);
        game.startSortingSessionBody();
      }
    } else if (stateManager.requestedStart()) {
      stateManager.setShowResults(false);
      stateManager.setStartRequested(false);
      stateManager.setContinueExecution(true);
      int delayMs =
          game.appContext() != null && game.appContext().isFiveSecondStartDelay()
              ? AppConfig.SETUP_DELAY_LONG
              : AppConfig.SETUP_DELAY;
      game.setSetupDelayRemaining(delayMs / 1000f);
      if (game.appContext() != null) {
        game.appContext().settingsBridge().setInputsEnabled(false);
      }
      game.setLastPublishedProgress(-1);
      game.setProgressPublishAccum(0f);
      game.publishThenDraw(delta);
    } else {
      game.publishThenDraw(delta);
    }
  }

  @Override
  public void resize(int width, int height) {
    if (game.renderSystem() != null) {
      game.renderSystem().resize(width, height);
    }
  }

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {}

  @Override
  public void dispose() {
    // Shared resources owned by SortingVisualizerGame.
  }
}
