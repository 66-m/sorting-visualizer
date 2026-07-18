package io.github.compilerstuck.control.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

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
        Thread waiter = new Thread(() -> {
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
    @DisplayName("grant then await from another thread")
    void crossThreadGrant() throws InterruptedException {
        FrameGate gate = new FrameGate();
        AtomicInteger steps = new AtomicInteger();
        Thread sorter = new Thread(() -> {
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
}
