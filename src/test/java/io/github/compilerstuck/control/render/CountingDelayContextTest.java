package io.github.compilerstuck.control.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class CountingDelayContextTest {

  @Test
  void countsDelayAndDelayFrameSeparately() {
    CountingDelayContext ctx = new CountingDelayContext();
    ctx.delay();
    ctx.delay();
    ctx.delayFrame();
    assertEquals(3, ctx.stepCount());
    assertEquals(1, ctx.frameBeatCount());
    assertFalse(ctx.timedOut());
  }

  @Test
  void timeoutInvokesAbortCallback() {
    AtomicBoolean aborted = new AtomicBoolean();
    CountingDelayContext ctx =
        new CountingDelayContext(System.nanoTime() - 1, null, () -> aborted.set(true));
    ctx.delay();
    assertTrue(ctx.timedOut());
    assertTrue(aborted.get());
    assertEquals(0, ctx.stepCount());
  }

  @Test
  void externalCancelAbortsWithoutTimeout() {
    AtomicBoolean aborted = new AtomicBoolean();
    CountingDelayContext ctx =
        new CountingDelayContext(Long.MAX_VALUE, () -> true, () -> aborted.set(true));
    ctx.delayFrame();
    assertTrue(ctx.aborted());
    assertFalse(ctx.timedOut());
    assertTrue(aborted.get());
    assertEquals(0, ctx.frameBeatCount());
  }
}
