package io.github.compilerstuck.control.model;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe state manager for sorting visualization. Encapsulates all mutable state that is
 * accessed from multiple threads.
 */
public class SortingStateManager {
  private final AtomicBoolean userInitiatedStart = new AtomicBoolean(false);
  private final AtomicBoolean isRunning = new AtomicBoolean(false);
  private final AtomicBoolean showResults = new AtomicBoolean(false);
  private final AtomicBoolean shouldRestart = new AtomicBoolean(false);
  private final AtomicBoolean showComparisonTable = new AtomicBoolean(false);
  private final AtomicBoolean printMeasurements = new AtomicBoolean(true);
  private final AtomicBoolean shouldContinueExecution = new AtomicBoolean(true);
  private final AtomicBoolean shuffling = new AtomicBoolean(false);

  /**
   * True while the sort worker is sleeping between phases (post-shuffle / post-sort). Render must
   * draw without granting FrameGate credits — the worker is not consuming steps.
   */
  private final AtomicBoolean frameGateSuspended = new AtomicBoolean(false);

  private volatile String currentOperation = "Waiting";

  /**
   * One-shot consume of a start request. Prefer {@link #isStartRequested()} for read-only checks
   * (e.g. HUD) so a click is not discarded mid-frame.
   *
   * @return true if start was requested
   */
  public boolean requestedStart() {
    return userInitiatedStart.getAndSet(false);
  }

  /** Non-consuming peek of the start-request flag. */
  public boolean isStartRequested() {
    return userInitiatedStart.get();
  }

  public void setStartRequested(boolean value) {
    userInitiatedStart.set(value);
  }

  public boolean isRunning() {
    return isRunning.get();
  }

  public void setRunning(boolean value) {
    isRunning.set(value);
  }

  public boolean shouldShowResults() {
    return showResults.get();
  }

  public void setShowResults(boolean value) {
    showResults.set(value);
  }

  public boolean shouldRestart() {
    return shouldRestart.get();
  }

  public void setRestart(boolean value) {
    shouldRestart.set(value);
  }

  public boolean shouldShowComparisonTable() {
    return showComparisonTable.get();
  }

  public void setShowComparisonTable(boolean value) {
    showComparisonTable.set(value);
  }

  public boolean shouldPrintMeasurements() {
    return printMeasurements.get();
  }

  public void setPrintMeasurements(boolean value) {
    printMeasurements.set(value);
  }

  public boolean shouldContinueExecution() {
    return shouldContinueExecution.get();
  }

  public void setContinueExecution(boolean value) {
    shouldContinueExecution.set(value);
  }

  /** True while the session worker is inside a shuffle animation. */
  public boolean isShuffling() {
    return shuffling.get();
  }

  public void setShuffling(boolean value) {
    shuffling.set(value);
  }

  /** True while the worker sleeps and will not consume FrameGate credits. */
  public boolean isFrameGateSuspended() {
    return frameGateSuspended.get();
  }

  public void setFrameGateSuspended(boolean value) {
    frameGateSuspended.set(value);
  }

  public String getCurrentOperation() {
    return currentOperation;
  }

  public void setCurrentOperation(String operation) {
    this.currentOperation = operation;
  }

  /** Resets all state for a new sorting run. */
  public void resetForNewRun() {
    userInitiatedStart.set(false);
    isRunning.set(false);
    showResults.set(false);
    shouldRestart.set(false);
    shuffling.set(false);
    frameGateSuspended.set(false);
    currentOperation = "Waiting";
  }
}
