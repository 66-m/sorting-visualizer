package io.github.compilerstuck.Control;

import io.github.compilerstuck.Control.model.ArrayController;
import io.github.compilerstuck.Control.model.SortingSessionManager;
import io.github.compilerstuck.Control.model.SortingStateManager;
import io.github.compilerstuck.Control.render.RenderContext;
import io.github.compilerstuck.SortingAlgorithms.SortingAlgorithm;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import io.github.compilerstuck.Visual.Visualization;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Live collaborator bundle for a running visualizer session.
 * Provides the operations the Settings UI needs without depending on
 * {@link MainController}'s static fields.
 */
public final class AppContext {
    private static final Logger LOGGER = Logger.getLogger(AppContext.class.getName());

    private final ArrayController arrayController;
    private final SortingStateManager stateManager;
    private final SortingSessionManager sessionManager;
    private Sound sound;
    private ColorGradient colorGradient;
    private Visualization visualization;
    private final List<SortingAlgorithm> algorithms = new ArrayList<>();
    private int size;
    private RenderContext renderContext;

    public AppContext(ArrayController arrayController, SortingStateManager stateManager,
                       SortingSessionManager sessionManager) {
        this.arrayController = arrayController;
        this.stateManager = stateManager;
        this.sessionManager = sessionManager;
    }

    public ArrayController getArrayController() {
        return arrayController;
    }

    public SortingStateManager getStateManager() {
        return stateManager;
    }

    public SortingSessionManager getSessionManager() {
        return sessionManager;
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public ColorGradient getColorGradient() {
        return colorGradient;
    }

    public void setColorGradient(ColorGradient colorGradient) {
        this.colorGradient = colorGradient;
        if (this.colorGradient != null) {
            this.colorGradient.updateGradient(size);
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
        if (visualization != null && colorGradient != null) {
            visualization.updateColorGradient(colorGradient);
        }
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

    public RenderContext getRenderContext() {
        return renderContext;
    }

    public void setRenderContext(RenderContext renderContext) {
        this.renderContext = renderContext;
    }

    /**
     * Resizes the array and dependent components; refuses while a sort is running.
     */
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
    }

    public void setStart(boolean shouldStart) {
        stateManager.setStartRequested(shouldStart);
    }

    public boolean isRunning() {
        return stateManager.isRunning();
    }

    public void cancelSorting() {
        sessionManager.cancel();
    }

    public void setAlgorithms(List<SortingAlgorithm> algorithmList) {
        algorithms.clear();
        for (SortingAlgorithm alg : algorithmList) {
            if (alg.isSelected()) {
                algorithms.add(alg);
            }
        }
    }

    public void setAlgorithm(SortingAlgorithm algorithm) {
        algorithms.clear();
        algorithms.add(algorithm);
    }

    public void setDelayTime(int ms) {
        algorithms.forEach(a -> a.setDelayTime(ms));
    }

    public void setDelayFactor(double factor) {
        algorithms.forEach(a -> a.setDelayFactor(factor));
    }

    public void setShowComparisonTable(boolean show) {
        stateManager.setShowComparisonTable(show);
    }

    public void setPrintMeasurements(boolean print) {
        stateManager.setPrintMeasurements(print);
    }

    public void shutdown() {
        MainController.shutdown();
    }
}
