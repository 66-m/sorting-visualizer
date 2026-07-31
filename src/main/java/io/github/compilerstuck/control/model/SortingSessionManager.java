package io.github.compilerstuck.control.model;

import io.github.compilerstuck.control.config.AppConfig;
import io.github.compilerstuck.control.render.CountingDelayContext;
import io.github.compilerstuck.control.render.DelayContext;
import io.github.compilerstuck.control.render.TrackingDelayContext;
import io.github.compilerstuck.control.ui.TimeEstimateFormat;
import io.github.compilerstuck.sortingalgorithms.BogoSort;
import io.github.compilerstuck.sortingalgorithms.GravitySort;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import io.github.compilerstuck.sound.Sound;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
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

  private BooleanSupplier equalizeEnabled = () -> false;
  private DoubleSupplier equalizeTargetSec = () -> 10.0;
  private Supplier<DelayContext> productionDelayContext = () -> null;

  public SortingSessionManager(
      ArrayController arrayController, Sound sound, SortingStateManager stateManager) {
    this(
        arrayController,
        sound,
        stateManager,
        AppConfig.DELAY_BETWEEN_ALGORITHMS,
        AppConfig.DELAY_AFTER_SORT_RESULT);
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
   * Optional equalize-sort-duration support. {@code productionDelay} supplies the live FrameGate
   * context (may be null in unit tests).
   */
  public void setEqualizeSupport(
      BooleanSupplier enabled,
      DoubleSupplier targetDurationSec,
      Supplier<DelayContext> productionDelay) {
    this.equalizeEnabled = enabled != null ? enabled : () -> false;
    this.equalizeTargetSec = targetDurationSec != null ? targetDurationSec : () -> 10.0;
    this.productionDelayContext = productionDelay != null ? productionDelay : () -> null;
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

  CancellationToken getCancellationToken() {
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
      stateManager.equalizePacing().clear();
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
    sound.cutNotes();

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
    DelayContext production = productionDelayContext.get();
    DelayContext fallback =
        production != null
            ? production
            : () -> {
              /* no-op */
            };
    try {
      if (tryArmEqualizePacing(algorithm, fallback)) {
        algorithm.setDelayContext(
            new TrackingDelayContext(fallback, stateManager.equalizePacing()));
      } else {
        if (algorithm instanceof GravitySort gravity) {
          gravity.setColumnStride(1);
        }
        algorithm.setDelayContext(fallback);
      }

      algorithm.beginTiming();
      try {
        algorithm.sort();
      } finally {
        algorithm.endTiming();
      }
    } finally {
      stateManager.equalizePacing().clear();
      if (algorithm instanceof GravitySort gravity) {
        gravity.setColumnStride(1);
      }
      algorithm.setDelayContext(fallback);
    }
  }

  /**
   * Silent dry-run that counts visual steps, restores the array, and arms {@link EqualizePacing}.
   *
   * @return true when equalize pacing was armed for the visual pass
   */
  boolean tryArmEqualizePacing(SortingAlgorithm algorithm, DelayContext production) {
    if (!equalizeEnabled.getAsBoolean()) {
      return false;
    }
    if (algorithm instanceof BogoSort) {
      LOGGER.log(Level.INFO, "Equalize skipped for Bogo Sort (unbounded)");
      return false;
    }
    if (!stateManager.shouldContinueExecution() || cancellationToken.isCancelled()) {
      return false;
    }

    float sliderTarget = (float) equalizeTargetSec.getAsDouble();

    // Gravity's full column walk is O(n·max) CPU — impossible in a few seconds at 100k. Estimate
    // beats, then raise columnStride so only ~budget samples are visualized/computed.
    if (algorithm instanceof GravitySort gravity) {
      int naturalBeats = GravitySort.estimateFrameBeats(arrayController);
      if (naturalBeats <= 0) {
        return false;
      }
      int n = arrayController.getLength();
      int maxPerFrame = AppConfig.equalizeMaxFrameBeatsPerFrame(n);
      int budget =
          Math.max(
              1,
              Math.round(maxPerFrame * AppConfig.TARGET_FRAME_RATE * Math.max(0.1f, sliderTarget)));
      int stride = Math.max(1, (naturalBeats + budget - 1) / budget);
      gravity.setColumnStride(stride);
      int visualBeats = GravitySort.countVisualBeats(naturalBeats, stride);
      LOGGER.log(
          Level.INFO,
          "Gravity equalize stride={0} (naturalBeats={1}, budget={2}, visualBeats={3})",
          new Object[] {stride, naturalBeats, budget, visualBeats});
      return armEqualize(algorithm.getName(), visualBeats, visualBeats, sliderTarget);
    }

    int[] snapshot = Arrays.copyOf(arrayController.getArray(), arrayController.getLength());
    OperationReporter previousReporter = stateManager::setCurrentOperation;
    CancellationToken dryToken = new CancellationToken();
    CancellationToken sessionToken = cancellationToken;
    CountingDelayContext counter =
        new CountingDelayContext(
            System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(AppConfig.EQUALIZE_DRY_RUN_TIMEOUT_MS),
            sessionToken::isCancelled,
            dryToken::cancel);

    stateManager.setCurrentOperation("Preparing…");
    // Render must not grant FrameGate credits during the dry-run (nothing consumes them).
    stateManager.setFrameGateSuspended(true);
    FrameGate gate = frameGate;
    if (gate != null) {
      gate.drain();
    }

    algorithm.setDelayContext(counter);
    algorithm.setOperationReporter(OperationReporter.NOOP);
    algorithm.setCancellationToken(dryToken);

    try {
      sound.withMuted(algorithm::sort);
    } finally {
      algorithm.endTiming();
      algorithm.setCancellationToken(sessionToken);
      algorithm.setOperationReporter(previousReporter);
      algorithm.setDelayContext(production);
      arrayController.restoreContents(snapshot);
      arrayController.resetMeasurements();
      if (gate != null) {
        gate.drain();
      }
      stateManager.setFrameGateSuspended(false);
    }

    if (sessionToken.isCancelled() || !stateManager.shouldContinueExecution()) {
      return false;
    }
    if (counter.timedOut()) {
      LOGGER.log(
          Level.INFO,
          "Equalize dry-run timed out for {0}; using normal speed pacing",
          algorithm.getName());
      return false;
    }
    if (counter.aborted() || dryToken.isCancelled()) {
      return false;
    }

    int totalSteps = clampToInt(counter.stepCount());
    int frameBeats = clampToInt(counter.frameBeatCount());
    if (totalSteps <= 0) {
      return false;
    }

    return armEqualize(algorithm.getName(), totalSteps, frameBeats, sliderTarget);
  }

  private boolean armEqualize(
      String algorithmName, int totalSteps, int frameBeats, float sliderTarget) {
    stateManager
        .equalizePacing()
        .begin(totalSteps, frameBeats, sliderTarget, arrayController.getLength());
    LOGGER.log(
        Level.INFO,
        "Equalize armed for {0}: steps={1}, frameBeats={2}, targetSec={3}, batch={4}, maxSteps/frame={5}",
        new Object[] {
          algorithmName,
          totalSteps,
          frameBeats,
          sliderTarget,
          stateManager.equalizePacing().batchBeats(),
          stateManager.equalizePacing().maxStepsPerFrame()
        });
    return true;
  }

  private static int clampToInt(long value) {
    if (value > Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    if (value < 0) {
      return 0;
    }
    return (int) value;
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
      sound.withMuted(
          () -> {
            try {
              Thread.sleep(delayMs);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              LOGGER.log(Level.WARNING, interruptLog, e);
            }
          });
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
