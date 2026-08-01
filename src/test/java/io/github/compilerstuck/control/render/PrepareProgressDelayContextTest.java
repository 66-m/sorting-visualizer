package io.github.compilerstuck.control.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class PrepareProgressDelayContextTest {

  @Test
  void reportsZeroThenHundredOnComplete() {
    List<String> labels = new ArrayList<>();
    AtomicInteger progress = new AtomicInteger(-1);
    CountingDelayContext counter = new CountingDelayContext();
    long start = System.nanoTime();
    PrepareProgressDelayContext ctx =
        new PrepareProgressDelayContext(counter, start, 1_000_000_000L, labels::add, progress::set);

    assertEquals(List.of("Prepare.. 0%"), labels);
    assertEquals(0, progress.get());

    ctx.complete();
    assertEquals("Prepare.. 100%", labels.get(labels.size() - 1));
    assertEquals(100, progress.get());
    assertEquals(0, counter.stepCount());
  }

  @Test
  void delayDelegatesToCounter() {
    CountingDelayContext counter = new CountingDelayContext();
    PrepareProgressDelayContext ctx =
        new PrepareProgressDelayContext(
            counter, System.nanoTime(), 1_000_000_000L, s -> {}, pct -> {});
    ctx.delay();
    ctx.delayFrame();
    assertEquals(2, counter.stepCount());
    assertEquals(1, counter.frameBeatCount());
    assertTrue(ctx.counter() == counter);
  }
}
