package io.github.compilerstuck.control.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.visual.Marker;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class SnapshotConcurrencyTest {

  @Test
  @Timeout(15)
  void publishedViewStaysConsistentUnderConcurrentWriter() throws Exception {
    int n = 256;
    ArrayController working = new ArrayController(n);
    SnapshotPublisher publisher = new SnapshotPublisher();
    FrameGate gate = new FrameGate();
    AtomicBoolean stop = new AtomicBoolean(false);
    AtomicReference<Throwable> writerError = new AtomicReference<>();

    Thread writer =
        new Thread(
            () -> {
              try {
                int i = 0;
                while (!stop.get()) {
                  gate.awaitStep();
                  int a = i % n;
                  int b = (i * 7 + 3) % n;
                  working.swap(a, b);
                  working.setMarker(a, Marker.SET);
                  i++;
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              } catch (Throwable t) {
                writerError.set(t);
              }
            },
            "snapshot-writer");
    writer.start();

    try {
      for (int frame = 0; frame < 300; frame++) {
        gate.awaitIdle();
        publisher.publish(working);
        gate.grant(16);

        ArrayModel published = publisher.publishedView();
        assertEquals(n, published.getLength());
        long sum = 0;
        for (int i = 0; i < n; i++) {
          int v = published.get(i);
          assertTrue(v >= 0 && v < n, "value out of range: " + v);
          sum += v;
          Marker m = published.getMarker(i);
          assertTrue(m == Marker.NORMAL || m == Marker.SET);
        }
        assertEquals((long) n * (n - 1) / 2, sum, "published values must stay a permutation");
      }
    } finally {
      stop.set(true);
      gate.cancel();
      writer.join(2000);
    }

    assertNull(writerError.get(), () -> "writer failed: " + writerError.get());
  }
}
