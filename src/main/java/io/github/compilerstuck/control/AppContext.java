package io.github.compilerstuck.control;

import io.github.compilerstuck.control.config.DelayStrategy;
import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.model.FrameGate;
import io.github.compilerstuck.control.model.SortingSessionManager;
import io.github.compilerstuck.control.model.SortingStateManager;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import io.github.compilerstuck.visual.Visualization;

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

    private static final int[] DELAY_TIME = {50, 10, 1, 1, 1};
    private static final double[] DELAY_FACTOR = {1.0, 1.0, 1.0, 0.12, 0.02};
    /** Speed levels 1–5 → steps per frame when step engine is enabled. */
    private static final int[] STEPS_PER_FRAME = {1, 5, 25, 200, 2000};

    private final ArrayController arrayController;
    private final SortingStateManager stateManager;
    private final SortingSessionManager sessionManager;
    private final FrameGate frameGate = new FrameGate();
    private Sound sound;
    private ColorGradient colorGradient;
    private Visualization visualization;
    private final List<SortingAlgorithm> algorithms = new ArrayList<>();
    private int size;
    private RenderContext renderContext;

    /** Legacy sleep delay by default; enable via {@code -Dsv.stepEngine=true} or Settings. */
    private boolean useStepEngine = Boolean.getBoolean("sv.stepEngine");
    private int speedLevel = 3; // 1–5, default Normal
    private int stepsPerFrame = STEPS_PER_FRAME[2];

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

    public FrameGate getFrameGate() {
        return frameGate;
    }

    public boolean isUseStepEngine() {
        return useStepEngine;
    }

    public void setUseStepEngine(boolean useStepEngine) {
        this.useStepEngine = useStepEngine;
        applySpeedLevel();
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

    /**
     * Applies speed level 1–5: steps/frame in step-engine mode, delay time/factor in legacy mode.
     */
    public void setSpeedLevel(int level1to5) {
        this.speedLevel = Math.max(1, Math.min(5, level1to5));
        applySpeedLevel();
    }

    private void applySpeedLevel() {
        int idx = speedLevel - 1;
        if (useStepEngine) {
            stepsPerFrame = STEPS_PER_FRAME[idx];
            algorithms.forEach(a -> a.setDelayStrategy(DelayStrategy.ALWAYS));
        } else {
            setDelayTime(DELAY_TIME[idx]);
            setDelayFactor(DELAY_FACTOR[idx]);
            algorithms.forEach(a -> a.setDelayStrategy(DelayStrategy.DEFAULT));
        }
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
        algorithms.add(algorithm);
        applySpeedLevel();
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
