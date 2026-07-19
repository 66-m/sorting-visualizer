package io.github.compilerstuck.control.model;

import io.github.compilerstuck.control.config.MainControllerConfig;
import io.github.compilerstuck.control.ui.TimeEstimateFormat;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import io.github.compilerstuck.sound.Sound;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the sorting session orchestration and thread coordination. Separated from UI concerns to
 * handle algorithm execution, measurements collection, and timing independently.
 */
public class SortingSessionManager {
  private static final Logger LOGGER = Logger.getLogger(SortingSessionManager.class.getName());

  private final ArrayController arrayController;
  private final Sound sound;
  private final SortingStateManager stateManager;
  private final int delayBetweenMs;
  private final int delayAfterMs;

  private final List<String> comparisons = new ArrayList<>();
  private final List<String> realTime = new ArrayList<>();
  private final List<String> swaps = new ArrayList<>();
  private final List<String> writesMain = new ArrayList<>();
  private final List<String> writesAux = new ArrayList<>();
  private final List<Integer> timestamps = new ArrayList<>();

  private Thread executionThread;
  private volatile CancellationToken cancellationToken = CancellationToken.alwaysActive();
  private volatile FrameGate frameGate;

  public SortingSessionManager(
      ArrayController arrayController, Sound sound, SortingStateManager stateManager) {
    this(
        arrayController,
        sound,
        stateManager,
        MainControllerConfig.DELAY_BETWEEN_ALGORITHMS,
        MainControllerConfig.DELAY_AFTER_SORT_RESULT);
  }

  public SortingSessionManager(
      ArrayController arrayController,
      Sound sound,
      SortingStateManager stateManager,
      int delayBetweenMs,
      int delayAfterMs) {
    this.arrayController = arrayController;
    this.sound = sound;
    this.stateManager = stateManager;
    this.delayBetweenMs = delayBetweenMs;
    this.delayAfterMs = delayAfterMs;
  }

  /**
   * Wires the pacing gate so session end can unblock the render thread's {@link
   * FrameGate#awaitIdle()}.
   */
  public void setFrameGate(FrameGate frameGate) {
    this.frameGate = frameGate;
  }

  /**
   * Starts the sorting algorithm execution in a background thread.
   *
   * @param algorithms the list of algorithms to execute
   */
  public void startSortingSession(List<SortingAlgorithm> algorithms) {
    if (algorithms == null || algorithms.isEmpty()) {
      LOGGER.log(Level.WARNING, "Attempted to start sorting session with empty algorithm list");
      return;
    }

    // Clear previous results
    clearMeasurements();

    CancellationToken token = new CancellationToken();
    this.cancellationToken = token;
    stateManager.setContinueExecution(true);

    OperationReporter reporter = stateManager::setCurrentOperation;
    arrayController.setCancellationToken(token);
    arrayController.setOperationReporter(reporter);

    for (SortingAlgorithm algorithm : algorithms) {
      algorithm.setCancellationToken(token);
      algorithm.setOperationReporter(reporter);
    }

    executionThread = new Thread(() -> executeSortingAlgorithms(algorithms));
    executionThread.setName("SortingThread");
    executionThread.start();
  }

  /** Cancels the active session token and stops further algorithm execution. */
  public void cancel() {
    cancellationToken.cancel();
    stateManager.setContinueExecution(false);
  }

  public CancellationToken getCancellationToken() {
    return cancellationToken;
  }

  /** Executes all algorithms in sequence, collecting measurements. */
  private void executeSortingAlgorithms(List<SortingAlgorithm> algorithms) {
    try {
      int startTime = (int) (System.currentTimeMillis() / 1000L);

      for (SortingAlgorithm algorithm : algorithms) {
        if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
          LOGGER.log(Level.INFO, "Sorting session cancelled by user");
          break;
        }

        recordTimestamp(startTime);
        prepareForAlgorithm();
        executeAlgorithm(algorithm);

        if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
          break;
        }

        recordMeasurements();
        pauseAfterAlgorithm();
      }

      if (stateManager.shouldContinueExecution()
          && !cancellationToken.isCancelled()
          && stateManager.shouldShowComparisonTable()) {
        stateManager.setShowResults(true);
      }

      stateManager.setRestart(true);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error during sorting session execution", e);
      stateManager.setRestart(true);
    } finally {
      stateManager.setFrameGateSuspended(false);
      // Drop unused step credits so the render thread cannot deadlock in awaitIdle().
      FrameGate gate = frameGate;
      if (gate != null) {
        gate.drain();
      }
      stateManager.setRunning(false);
    }
  }

  private void recordTimestamp(int startTime) {
    timestamps.add((int) (System.currentTimeMillis() / 1000L) - startTime);
  }

  private void prepareForAlgorithm() {
    sound.mute(true);
    sound.mute(false);

    stateManager.setShuffling(true);
    try {
      arrayController.shuffle();
    } finally {
      stateManager.setShuffling(false);
      // Drop unused shuffle credits so awaitIdle cannot stall during the inter-phase sleep.
      FrameGate gate = frameGate;
      if (gate != null) {
        gate.drain();
      }
    }

    if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
      return;
    }

    sleepWithoutStepCredits(delayBetweenMs, "Thread interrupted during delay");
    arrayController.resetMeasurements();
  }

  private void executeAlgorithm(SortingAlgorithm algorithm) {
    algorithm.beginTiming();
    try {
      algorithm.sort();
    } finally {
      algorithm.endTiming();
    }
  }

  private void recordMeasurements() {
    comparisons.add(Long.toString(arrayController.getComparisons()));
    realTime.add(Double.toString(arrayController.getRealTime()));
    swaps.add(Long.toString(arrayController.getSwaps()));
    writesMain.add(Long.toString(arrayController.getWrites()));
    writesAux.add(Long.toString(arrayController.getWritesAux()));
  }

  private void pauseAfterAlgorithm() {
    sleepWithoutStepCredits(delayAfterMs, "Thread interrupted during result pause");
    arrayController.resetMeasurements();
  }

  /**
   * Mutes and sleeps while telling the render thread to draw without FrameGate pacing. Draining
   * alone is not enough: during the sleep {@code isRunning} stays true, so the next frame would
   * grant fresh credits that nobody consumes until the sleep ends, freezing the view for the whole
   * pause.
   */
  private void sleepWithoutStepCredits(int delayMs, String interruptLog) {
    stateManager.setFrameGateSuspended(true);
    try {
      FrameGate gate = frameGate;
      if (gate != null) {
        gate.drain();
      }
      sound.mute(true);
      try {
        Thread.sleep(delayMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        LOGGER.log(Level.WARNING, interruptLog, e);
      }
      sound.mute(false);
    } finally {
      stateManager.setFrameGateSuspended(false);
    }
  }

  /** Clears all measurement data for a fresh session. */
  public void clearMeasurements() {
    comparisons.clear();
    realTime.clear();
    swaps.clear();
    writesMain.clear();
    writesAux.clear();
    timestamps.clear();
  }

  /** Waits for the current sorting session to complete. */
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

  /** Logs timing information to console after sorting completes. */
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

  public boolean hasResults() {
    return !comparisons.isEmpty();
  }

  /** Writes comparison metrics as CSV (columns aligned with the on-screen results table). */
  public void exportCsv(Path path, List<SortingAlgorithm> algorithms) throws IOException {
    try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      writer.write("algorithm,elements,comparisons,est_time_ms,swaps,writes_main,writes_aux\n");
      int rows = algorithms == null ? 0 : algorithms.size();
      for (int i = 0; i < rows; i++) {
        SortingAlgorithm alg = algorithms.get(i);
        writer.write(csvEscape(alg.getName()));
        writer.write(',');
        writer.write(Integer.toString(alg.getAlternativeSize()));
        writer.write(',');
        writer.write(i < comparisons.size() ? comparisons.get(i) : "");
        writer.write(',');
        if (i < realTime.size()) {
          double raw = Double.parseDouble(realTime.get(i));
          writer.write(TimeEstimateFormat.format(raw));
        }
        writer.write(',');
        writer.write(i < swaps.size() ? swaps.get(i) : "");
        writer.write(',');
        writer.write(i < writesMain.size() ? writesMain.get(i) : "");
        writer.write(',');
        writer.write(i < writesAux.size() ? writesAux.get(i) : "");
        writer.write('\n');
      }
    }
  }

  private static String csvEscape(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return '"' + value.replace("\"", "\"\"") + '"';
    }
    return value;
  }
}
