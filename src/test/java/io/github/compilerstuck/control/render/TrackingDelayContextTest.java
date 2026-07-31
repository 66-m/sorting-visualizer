package io.github.compilerstuck.control.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.model.EqualizePacing;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class TrackingDelayContextTest {

  @Test
  void batchingUsesPlainDelayWithoutDrain() {
    AtomicInteger delayCalls = new AtomicInteger();
    AtomicInteger delayFrameCalls = new AtomicInteger();
    DelayContext inner =
        new DelayContext() {
          @Override
          public void delay() {
            delayCalls.incrementAndGet();
          }

          @Override
          public void delayFrame() {
            delayFrameCalls.incrementAndGet();
          }
        };

    EqualizePacing pacing = new EqualizePacing();
    pacing.begin(120, 120, 1f); // floor 2s, target 1s → batch
    assertTrue(pacing.batchBeats());

    TrackingDelayContext tracking = new TrackingDelayContext(inner, pacing);
    tracking.delayFrame();
    tracking.delayFrame();

    assertEquals(2, delayCalls.get());
    assertEquals(0, delayFrameCalls.get());
    assertEquals(2, pacing.frameBeatsConsumed());
  }

  @Test
  void nonBatchingDelayFrameInvokesDelegateDelayFramePlusExtras() {
    AtomicInteger delayCalls = new AtomicInteger();
    AtomicInteger delayFrameCalls = new AtomicInteger();
    DelayContext inner =
        new DelayContext() {
          @Override
          public void delay() {
            delayCalls.incrementAndGet();
          }

          @Override
          public void delayFrame() {
            delayFrameCalls.incrementAndGet();
          }
        };

    EqualizePacing pacing = new EqualizePacing();
    pacing.begin(60, 60, 2f); // stretch → ~1 extra frame per beat on average

    TrackingDelayContext tracking = new TrackingDelayContext(inner, pacing);
    tracking.delayFrame();

    assertEquals(0, delayCalls.get());
    assertTrue(delayFrameCalls.get() >= 1);
    assertEquals(1, pacing.frameBeatsConsumed());

    int extrasOverRun = delayFrameCalls.get() - 1;
    for (int i = 0; i < 59; i++) {
      int before = delayFrameCalls.get();
      tracking.delayFrame();
      extrasOverRun += delayFrameCalls.get() - before - 1;
    }
    assertEquals(60, extrasOverRun);
  }
}
