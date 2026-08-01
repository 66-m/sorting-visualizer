package io.github.compilerstuck.sortingalgorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.render.CountingDelayContext;
import io.github.compilerstuck.visual.Marker;
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

  @Test
  void paceReplacesPreviousMarkersInsteadOfAccumulating() {
    ArrayController array = new ArrayController(8);
    CountingDelayContext counter = new CountingDelayContext();
    MarkerProbe probe = new MarkerProbe(array);
    probe.setDelayContext(counter);
    probe.setDelayStride(1);
    probe.sort();
    int setCount = 0;
    for (int i = 0; i < array.getLength(); i++) {
      if (array.getMarker(i) == Marker.SET) {
        setCount++;
      }
    }
    assertEquals(2, setCount);
    assertEquals(Marker.SET, array.getMarker(6));
    assertEquals(Marker.SET, array.getMarker(7));
    assertEquals(Marker.NORMAL, array.getMarker(0));
    assertEquals(Marker.NORMAL, array.getMarker(1));
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

  private static final class MarkerProbe extends SortingAlgorithm {
    MarkerProbe(ArrayController model) {
      super(model);
      this.name = "MarkerProbe";
    }

    @Override
    public void sort() {
      delay(new int[] {0, 1});
      delay(new int[] {2, 3});
      delay(new int[] {6, 7});
    }
  }
}
