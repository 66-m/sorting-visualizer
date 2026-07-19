package io.github.compilerstuck.control.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SortingStateManagerTest {

  private SortingStateManager state;

  @BeforeEach
  void setUp() {
    state = new SortingStateManager();
  }

  @Test
  @DisplayName("requestedStart is one-shot: true then false")
  void requestedStartIsOneShot() {
    state.setStartRequested(true);
    assertTrue(state.requestedStart());
    assertFalse(state.requestedStart());
  }

  @Test
  @DisplayName("isStartRequested peeks without consuming")
  void isStartRequestedDoesNotConsume() {
    state.setStartRequested(true);
    assertTrue(state.isStartRequested());
    assertTrue(state.isStartRequested());
    assertTrue(state.requestedStart());
    assertFalse(state.isStartRequested());
  }

  @Test
  @DisplayName("setStartRequested sets the start flag")
  void setStartRequested() {
    assertFalse(state.requestedStart());
    state.setStartRequested(true);
    assertTrue(state.requestedStart());
  }

  @Test
  @DisplayName("resetForNewRun clears running, results, restart, shuffling, and operation")
  void resetForNewRunClearsCoreState() {
    state.setStartRequested(true);
    state.setRunning(true);
    state.setShowResults(true);
    state.setRestart(true);
    state.setShuffling(true);
    state.setCurrentOperation("Bubble Sort");

    state.resetForNewRun();

    assertFalse(state.requestedStart());
    assertFalse(state.isRunning());
    assertFalse(state.shouldShowResults());
    assertFalse(state.shouldRestart());
    assertFalse(state.isShuffling());
    assertEquals("Waiting", state.getCurrentOperation());
  }

  @Test
  @DisplayName("shuffling flag round-trips")
  void shufflingFlagRoundTrip() {
    assertFalse(state.isShuffling());
    state.setShuffling(true);
    assertTrue(state.isShuffling());
    state.setShuffling(false);
    assertFalse(state.isShuffling());
  }

  @Test
  @DisplayName("frameGateSuspended flag round-trips and clears on reset")
  void frameGateSuspendedRoundTrip() {
    assertFalse(state.isFrameGateSuspended());
    state.setFrameGateSuspended(true);
    assertTrue(state.isFrameGateSuspended());
    state.resetForNewRun();
    assertFalse(state.isFrameGateSuspended());
  }

  @Test
  @DisplayName("showResults flag is independent of other flags")
  void showResultsIsIndependent() {
    state.setShowResults(true);
    assertTrue(state.shouldShowResults());
    assertFalse(state.shouldRestart());
    assertFalse(state.isRunning());
    assertTrue(state.shouldPrintMeasurements());
    assertTrue(state.shouldContinueExecution());
    assertFalse(state.shouldShowComparisonTable());
  }

  @Test
  @DisplayName("restart flag is independent of other flags")
  void restartIsIndependent() {
    state.setRestart(true);
    assertTrue(state.shouldRestart());
    assertFalse(state.shouldShowResults());
    assertFalse(state.isRunning());
  }

  @Test
  @DisplayName("comparison table flag is independent of other flags")
  void comparisonTableIsIndependent() {
    state.setShowComparisonTable(true);
    assertTrue(state.shouldShowComparisonTable());
    assertFalse(state.shouldShowResults());
    assertFalse(state.shouldRestart());
  }

  @Test
  @DisplayName("printMeasurements flag is independent of other flags")
  void printMeasurementsIsIndependent() {
    state.setPrintMeasurements(false);
    assertFalse(state.shouldPrintMeasurements());
    assertFalse(state.shouldShowResults());
    assertTrue(state.shouldContinueExecution());
  }

  @Test
  @DisplayName("continueExecution flag is independent of other flags")
  void continueExecutionIsIndependent() {
    state.setContinueExecution(false);
    assertFalse(state.shouldContinueExecution());
    assertFalse(state.shouldShowResults());
    assertTrue(state.shouldPrintMeasurements());
  }

  @Test
  @DisplayName("setCurrentOperation and getCurrentOperation round-trip")
  void currentOperationRoundTrip() {
    assertEquals("Waiting", state.getCurrentOperation());
    state.setCurrentOperation("Merge Sort");
    assertEquals("Merge Sort", state.getCurrentOperation());
  }

  @Test
  @DisplayName("resetForNewRun does not clear independent UI flags")
  void resetForNewRunPreservesIndependentFlags() {
    state.setShowComparisonTable(true);
    state.setPrintMeasurements(false);
    state.setContinueExecution(false);

    state.resetForNewRun();

    assertTrue(state.shouldShowComparisonTable());
    assertFalse(state.shouldPrintMeasurements());
    assertFalse(state.shouldContinueExecution());
  }
}
