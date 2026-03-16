package io.github.compilerstuck.Control.model;

import io.github.compilerstuck.Control.config.MainControllerConfig;
import io.github.compilerstuck.SortingAlgorithms.SortingAlgorithm;
import io.github.compilerstuck.Sound.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the sorting session orchestration and thread coordination.
 * Separated from UI concerns to handle algorithm execution, measurements collection,
 * and timing independently.
 */
public class SortingSessionManager {
    private static final Logger LOGGER = Logger.getLogger(SortingSessionManager.class.getName());

    private final ArrayController arrayController;
    private final Sound sound;
    private final SortingStateManager stateManager;

    private final List<String> comparisons = new ArrayList<>();
    private final List<String> realTime = new ArrayList<>();
    private final List<String> swaps = new ArrayList<>();
    private final List<String> writesMain = new ArrayList<>();
    private final List<String> writesAux = new ArrayList<>();
    private final List<Integer> timestamps = new ArrayList<>();

    private Thread executionThread;

    public SortingSessionManager(ArrayController arrayController, Sound sound, SortingStateManager stateManager) {
        this.arrayController = arrayController;
        this.sound = sound;
        this.stateManager = stateManager;
    }

    /**
     * Starts the sorting algorithm execution in a background thread.
     * @param algorithms the list of algorithms to execute
     */
    public void startSortingSession(List<SortingAlgorithm> algorithms) {
        if (algorithms == null || algorithms.isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to start sorting session with empty algorithm list");
            return;
        }

        // Clear previous results
        clearMeasurements();

        executionThread = new Thread(() -> executeSortingAlgorithms(algorithms));
        executionThread.setName("SortingThread");
        executionThread.start();
    }

    /**
     * Executes all algorithms in sequence, collecting measurements.
     */
    private void executeSortingAlgorithms(List<SortingAlgorithm> algorithms) {
        try {
            int startTime = (int) (System.currentTimeMillis() / 1000L);

            for (SortingAlgorithm algorithm : algorithms) {
                if (!stateManager.shouldContinueExecution()) {
                    LOGGER.log(Level.INFO, "Sorting session cancelled by user");
                    break;
                }

                recordTimestamp(startTime);
                prepareForAlgorithm(algorithm);
                executeAlgorithm(algorithm);

                if (!stateManager.shouldContinueExecution()) {
                    break;
                }

                recordMeasurements(algorithm);
                pauseAfterAlgorithm();
            }

            if (stateManager.shouldContinueExecution() && stateManager.shouldShowComparisonTable()) {
                stateManager.setShowResults(true);
            }

            stateManager.setRestart(true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during sorting session execution", e);
            stateManager.setRestart(true);
        } finally {
            stateManager.setRunning(false);
        }
    }

    private void recordTimestamp(int startTime) {
        timestamps.add((int) (System.currentTimeMillis() / 1000L) - startTime);
    }

    private void prepareForAlgorithm(SortingAlgorithm algorithm) {
        sound.mute(true);
        sound.mute(false);

        arrayController.shuffle();

        if (!stateManager.shouldContinueExecution()) {
            return;
        }

        sound.mute(true);
        try {
            Thread.sleep(MainControllerConfig.DELAY_BETWEEN_ALGORITHMS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Thread interrupted during delay", e);
        }
        sound.mute(false);
        arrayController.resetMeasurements();
    }

    private void executeAlgorithm(SortingAlgorithm algorithm) {
        algorithm.sort();
    }

    private void recordMeasurements(SortingAlgorithm algorithm) {
        comparisons.add(Long.toString(arrayController.getComparisons()));
        realTime.add(Double.toString(arrayController.getRealTime()));
        swaps.add(Long.toString(arrayController.getSwaps()));
        writesMain.add(Long.toString(arrayController.getWrites()));
        writesAux.add(Long.toString(arrayController.getWritesAux()));
    }

    private void pauseAfterAlgorithm() {
        sound.mute(true);
        try {
            Thread.sleep(MainControllerConfig.DELAY_AFTER_SORT_RESULT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Thread interrupted during result pause", e);
        }
        sound.mute(false);
        arrayController.resetMeasurements();
    }

    /**
     * Clears all measurement data for a fresh session.
     */
    public void clearMeasurements() {
        comparisons.clear();
        realTime.clear();
        swaps.clear();
        writesMain.clear();
        writesAux.clear();
        timestamps.clear();
    }

    /**
     * Waits for the current sorting session to complete.
     */
    public void waitForCompletion() {
        if (executionThread != null && executionThread.isAlive()) {
            try {
                executionThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Interrupted while waiting for sorting thread", e);
            }
        }
    }

    // Getters for measurements
    public List<String> getComparisons() {
        return new ArrayList<>(comparisons);
    }

    public List<String> getRealTime() {
        return new ArrayList<>(realTime);
    }

    public List<String> getSwaps() {
        return new ArrayList<>(swaps);
    }

    public List<String> getWritesMain() {
        return new ArrayList<>(writesMain);
    }

    public List<String> getWritesAux() {
        return new ArrayList<>(writesAux);
    }

    public List<Integer> getTimestamps() {
        return new ArrayList<>(timestamps);
    }

    /**
     * Logs timing information to console after sorting completes.
     */
    public void printTimestampsToConsole(List<SortingAlgorithm> algorithms) {
        System.out.println("\n\nTimestamps:\n");
        for (int i = 0; i < algorithms.size() && i < timestamps.size(); i++) {
            int seconds = timestamps.get(i);
            int minutes = seconds / 60;
            int secs = seconds % 60;
            String time = String.format("%02d:%02d", minutes, secs);
            System.out.println(time + " " + algorithms.get(i).getName());
        }
    }
}
