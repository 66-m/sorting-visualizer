package io.github.compilerstuck.control.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FrameTimeWindowTest {

  @Test
  void avgAndOnePercentLowWithUniformSamples() {
    FrameTimeWindow w = new FrameTimeWindow(100);
    for (int i = 0; i < 100; i++) {
      w.add(10f);
    }
    assertEquals(10f, w.avgMs(), 0.001f);
    assertEquals(100f, w.onePercentLowFps(), 0.001f);
  }

  @Test
  void onePercentLowReflectsWorstFrames() {
    FrameTimeWindow w = new FrameTimeWindow(100);
    for (int i = 0; i < 99; i++) {
      w.add(10f);
    }
    w.add(50f); // single worst sample → 1% bucket
    assertEquals(10.4f, w.avgMs(), 0.001f);
    assertEquals(20f, w.onePercentLowFps(), 0.001f); // 1000/50
  }

  @Test
  void ignoresNonPositiveSamples() {
    FrameTimeWindow w = new FrameTimeWindow(8);
    w.add(0f);
    w.add(-1f);
    w.add(Float.NaN);
    assertEquals(0, w.size());
    assertEquals(0f, w.avgMs(), 0f);
    assertEquals(0f, w.onePercentLowFps(), 0f);
  }

  @Test
  void ringsWhenCapacityExceeded() {
    FrameTimeWindow w = new FrameTimeWindow(3);
    w.add(10f);
    w.add(20f);
    w.add(30f);
    w.add(40f); // drops 10
    assertEquals(3, w.size());
    assertEquals(30f, w.avgMs(), 0.001f);
    assertTrue(w.onePercentLowFps() > 0f);
  }
}
