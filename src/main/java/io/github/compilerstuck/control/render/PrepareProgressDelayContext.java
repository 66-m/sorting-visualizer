package io.github.compilerstuck.control.render;

import io.github.compilerstuck.control.model.OperationReporter;
import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * Counts dry-run delays like {@link CountingDelayContext} while reporting time-based Prepare..
 * progress (same HUD style as {@code Shuffling.. N%}).
 */
public final class PrepareProgressDelayContext implements DelayContext {
  /** Minimum gap between HUD / progress-bar updates during a fast dry-run. */
  static final long REPORT_INTERVAL_NANOS = 50_000_000L; // 50ms

  private final CountingDelayContext counter;
  private final long startNanos;
  private final long timeoutNanos;
  private final OperationReporter reporter;
  private final IntConsumer progressSink;
  private long lastReportNanos;
  private int lastReportedPct = -1;

  public PrepareProgressDelayContext(
      CountingDelayContext counter,
      long startNanos,
      long timeoutNanos,
      OperationReporter reporter,
      IntConsumer progressSink) {
    this.counter = Objects.requireNonNull(counter, "counter");
    this.startNanos = startNanos;
    this.timeoutNanos = Math.max(1L, timeoutNanos);
    this.reporter = reporter != null ? reporter : OperationReporter.NOOP;
    this.progressSink = progressSink != null ? progressSink : pct -> {};
    this.lastReportNanos = startNanos;
    report(0);
  }

  @Override
  public void delay() {
    counter.delay();
    maybeReport();
  }

  @Override
  public void delayFrame() {
    counter.delayFrame();
    maybeReport();
  }

  /** Forces a final 100% report when the dry-run finishes before the timeout. */
  public void complete() {
    report(100);
  }

  public CountingDelayContext counter() {
    return counter;
  }

  private void maybeReport() {
    long now = System.nanoTime();
    if (now - lastReportNanos < REPORT_INTERVAL_NANOS) {
      return;
    }
    lastReportNanos = now;
    long elapsed = Math.max(0L, now - startNanos);
    int pct = (int) Math.min(99L, (elapsed * 100L) / timeoutNanos);
    report(pct);
  }

  private void report(int pct) {
    if (pct == lastReportedPct) {
      return;
    }
    lastReportedPct = pct;
    reporter.report("Prepare.. " + pct + "%");
    progressSink.accept(pct);
  }
}
