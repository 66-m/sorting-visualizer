package io.github.compilerstuck.sortingalgorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.render.CountingDelayContext;
import org.junit.jupiter.api.Test;

class SortingAlgorithmDelayStrideTest {

  @Test
  void delayStrideSkipsGateWaitsBetweenHits() {
    ArrayController array = new ArrayController(8);
    CountingDelayContext counter = new CountingDelayContext();
    StrideProbe probe = new StrideProbe(array, 20);
    probe.setDelayContext(counter);
    probe.setDelayStride(5);
    probe.sort();
    // Stride skips plain delay() waits; frame beats stay at 0.
    assertEquals(4, counter.stepCount());
    assertEquals(0, counter.frameBeatCount());
    assertEquals(5, probe.getDelayStride());
  }

  @Test
  void delayStrideOneCountsEveryCall() {
    ArrayController array = new ArrayController(8);
    CountingDelayContext counter = new CountingDelayContext();
    StrideProbe probe = new StrideProbe(array, 11);
    probe.setDelayContext(counter);
    probe.setDelayStride(1);
    probe.sort();
    assertEquals(11, counter.stepCount());
  }

  private static final class StrideProbe extends SortingAlgorithm {
    private final int calls;

    StrideProbe(ArrayController model, int calls) {
      super(model);
      this.calls = calls;
      this.name = "StrideProbe";
    }

    @Override
    public void sort() {
      for (int i = 0; i < calls && !isCancelled(); i++) {
        delay();
      }
    }
  }
}
