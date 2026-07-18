package io.github.compilerstuck.control;

import static org.junit.jupiter.api.Assertions.*;

import io.github.compilerstuck.control.config.DelayStrategy;
import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.model.FrameGate;
import io.github.compilerstuck.sortingalgorithms.QuickSortMiddlePivot;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Headless drain of FrameGate-backed delays for QuickSort. */
class StepEngineParityTest {

  @Test
  @DisplayName("QuickSort completes under FrameGate with auto-grant ProcessingContext")
  void quickSortWithFrameGate() throws InterruptedException {
    ArrayController array = new ArrayController(64);
    for (int i = 0; i < array.getLength(); i++) {
      array.set(i, array.getLength() - 1 - i);
    }

    FrameGate gate = new FrameGate();
    AtomicInteger delays = new AtomicInteger();
    SortingAlgorithm sort = new QuickSortMiddlePivot(array);
    sort.setProcessingContext(
        ms -> {
          delays.incrementAndGet();
          try {
            gate.awaitStep();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });
    sort.setDelayStrategy(DelayStrategy.ALWAYS);
    sort.setDelay(true);

    Thread sorter = new Thread(sort::sort, "test-sort");
    sorter.start();

    long deadline = System.currentTimeMillis() + 10_000;
    while (sorter.isAlive() && System.currentTimeMillis() < deadline) {
      gate.grant(500);
      Thread.sleep(5);
    }
    gate.cancel();
    sorter.join(2000);

    assertFalse(sorter.isAlive(), "sort thread should finish");
    assertTrue(array.isSorted());
    assertTrue(delays.get() > 0);
  }
}
