package io.github.compilerstuck.control.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FrameGateTest {

  @Test
  @DisplayName("awaitStep consumes one credit after grant")
  void awaitConsumesCredit() throws InterruptedException {
    FrameGate gate = new FrameGate();
    gate.grant(2);
    assertEquals(2, gate.availableCredits());
    gate.awaitStep();
    assertEquals(1, gate.availableCredits());
    gate.awaitStep();
    assertEquals(0, gate.availableCredits());
  }

  @Test
  @DisplayName("cancel unblocks a waiting awaitStep")
  void cancelUnblocksWaiter() throws InterruptedException {
    FrameGate gate = new FrameGate();
    AtomicBoolean finished = new AtomicBoolean(false);
    Thread waiter =
        new Thread(
            () -> {
              try {
                gate.awaitStep();
                finished.set(true);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
    waiter.start();
    Thread.sleep(50);
    assertFalse(finished.get());
    gate.cancel();
    waiter.join(1000);
    assertTrue(finished.get());
    assertTrue(gate.isCancelled());
  }

  @Test
  @DisplayName("reset clears cancel and credits")
  void resetClearsState() {
    FrameGate gate = new FrameGate();
    gate.grant(5);
    gate.cancel();
    gate.reset();
    assertEquals(0, gate.availableCredits());
    assertFalse(gate.isCancelled());
  }

  @Test
  @DisplayName("reset unblocks awaitIdle waiting on leftover credits")
  void resetUnblocksAwaitIdle() throws InterruptedException {
    FrameGate gate = new FrameGate();
    gate.grant(4);
    AtomicBoolean idleReached = new AtomicBoolean(false);
    Thread waiter =
        new Thread(
            () -> {
              try {
                gate.awaitIdle();
                idleReached.set(true);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
    waiter.start();
    Thread.sleep(30);
    assertFalse(idleReached.get());
    gate.reset();
    waiter.join(1000);
    assertTrue(idleReached.get());
    assertEquals(0, gate.availableCredits());
  }

  @Test
  @DisplayName("grant then await from another thread")
  void crossThreadGrant() throws InterruptedException {
    FrameGate gate = new FrameGate();
    AtomicInteger steps = new AtomicInteger();
    Thread sorter =
        new Thread(
            () -> {
              try {
                for (int i = 0; i < 3; i++) {
                  gate.awaitStep();
                  steps.incrementAndGet();
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
    sorter.start();
    for (int i = 0; i < 3; i++) {
      Thread.sleep(20);
      gate.grant(1);
    }
    sorter.join(2000);
    assertEquals(3, steps.get());
  }

  @Test
  @DisplayName("awaitIdle returns when credits drain to zero")
  void awaitIdleAfterDrain() throws InterruptedException {
    FrameGate gate = new FrameGate();
    gate.grant(2);
    AtomicBoolean idleReached = new AtomicBoolean(false);
    Thread waiter =
        new Thread(
            () -> {
              try {
                gate.awaitIdle();
                idleReached.set(true);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
    waiter.start();
    Thread.sleep(30);
    assertFalse(idleReached.get());
    gate.awaitStep();
    assertFalse(idleReached.get());
    gate.awaitStep();
    waiter.join(1000);
    assertTrue(idleReached.get());
    assertEquals(0, gate.availableCredits());
  }

  @Test
  @DisplayName("awaitIdle returns immediately when already idle")
  void awaitIdleWhenAlreadyIdle() throws InterruptedException {
    FrameGate gate = new FrameGate();
    gate.awaitIdle();
    assertEquals(0, gate.availableCredits());
  }

  @Test
  @DisplayName("drain unblocks awaitIdle with leftover credits")
  void drainUnblocksAwaitIdle() throws InterruptedException {
    FrameGate gate = new FrameGate();
    gate.grant(8);
    AtomicBoolean idleReached = new AtomicBoolean(false);
    Thread waiter =
        new Thread(
            () -> {
              try {
                gate.awaitIdle();
                idleReached.set(true);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
    waiter.start();
    Thread.sleep(30);
    assertFalse(idleReached.get());
    gate.drain();
    waiter.join(1000);
    assertTrue(idleReached.get());
    assertEquals(0, gate.availableCredits());
    assertFalse(gate.isCancelled());
  }

  @Test
  @DisplayName("drain then awaitStep yields one visible frame despite multi-credit grant")
  void drainThenAwaitGivesOneFrame() throws InterruptedException {
    FrameGate gate = new FrameGate();
    AtomicInteger columns = new AtomicInteger();
    java.util.concurrent.CountDownLatch column1 = new java.util.concurrent.CountDownLatch(1);
    java.util.concurrent.CountDownLatch column2 = new java.util.concurrent.CountDownLatch(1);
    java.util.concurrent.CountDownLatch column3 = new java.util.concurrent.CountDownLatch(1);
    java.util.concurrent.CountDownLatch[] columnReady = {column1, column2, column3};
    Thread sorter =
        new Thread(
            () -> {
              try {
                for (int i = 0; i < 3; i++) {
                  columns.incrementAndGet(); // settle column (like GravitySort rewrite)
                  columnReady[i].countDown();
                  gate.drain(); // delayFrame: force publish
                  gate.awaitStep();
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            },
            "gravity-frame-sorter");
    sorter.start();
    for (int expected = 1; expected <= 3; expected++) {
      assertTrue(
          columnReady[expected - 1].await(2, java.util.concurrent.TimeUnit.SECONDS),
          "column " + expected + " should settle");
      assertEquals(expected, columns.get(), "one column per published frame");
      // Grant only after awaitStep is blocking; otherwise drain can wipe a premature grant and
      // awaitIdle returns with columns still unchanged (flake under CI scheduling).
      waitUntilWaiting(sorter);
      gate.awaitIdle();
      gate.grant(25); // default speed budget — must not skip columns
    }
    sorter.join(2000);
    assertFalse(sorter.isAlive());
    assertEquals(3, columns.get());
  }

  /** Waits until {@code thread} is blocked in {@code Object.wait} (e.g. FrameGate.awaitStep). */
  private static void waitUntilWaiting(Thread thread) throws InterruptedException {
    long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(2);
    while (System.nanoTime() < deadline) {
      Thread.State state = thread.getState();
      if (state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING) {
        Thread.sleep(1);
        state = thread.getState();
        if (state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING) {
          return;
        }
      }
      Thread.onSpinWait();
    }
    fail("thread did not block in awaitStep: state=" + thread.getState());
  }
}
