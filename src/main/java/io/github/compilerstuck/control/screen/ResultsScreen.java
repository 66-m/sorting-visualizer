package io.github.compilerstuck.control.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import io.github.compilerstuck.control.SortingVisualizerGame;
import io.github.compilerstuck.control.model.SortingStateManager;
import io.github.compilerstuck.control.render.GdxRenderSystem;
import io.github.compilerstuck.control.ui.ResultsTableRenderer;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Comparison-table results mode. */
public final class ResultsScreen implements Screen {
  private static final Logger LOGGER = Logger.getLogger(ResultsScreen.class.getName());

  private final SortingVisualizerGame game;
  private final ResultsTableRenderer resultsTableRenderer = new ResultsTableRenderer();

  public ResultsScreen(SortingVisualizerGame game) {
    this.game = game;
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor(
        new InputAdapter() {
          @Override
          public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
              if (game.sound() != null) {
                game.sound().mute(true);
              }
              game.shutdown();
              return true;
            }
            return false;
          }
        });
  }

  @Override
  public void render(float delta) {
    SortingStateManager stateManager = game.stateManager();
    if (!(stateManager.shouldShowResults() && stateManager.shouldShowComparisonTable())) {
      game.showVisualizer();
      return;
    }

    try {
      GdxRenderSystem renderSystem = game.renderSystem();
      renderSystem.beginFrame();

      if (stateManager.shouldRestart()) {
        // Keep showResults so the table stays visible; only clear running/restart state.
        game.finishSession(false);
      }

      // Run was pressed while viewing results; hand off to the visualizer idle path.
      if (stateManager.isStartRequested()) {
        stateManager.setShowResults(false);
        game.showVisualizer();
        renderSystem.endFrame();
        return;
      }

      if (stateManager.shouldContinueExecution()) {
        resultsTableRenderer.render(
            renderSystem,
            game.currentAlgorithms(),
            game.sessionManager().getComparisons(),
            game.sessionManager().getRealTime(),
            game.sessionManager().getSwaps(),
            game.sessionManager().getWritesMain(),
            game.sessionManager().getWritesAux());
      }

      renderSystem.endWorld();
      renderSystem.endFrame();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error during results render", e);
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
