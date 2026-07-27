package io.github.compilerstuck.control.render;

import java.util.Arrays;

/**
 * Rolling window of recent frame times for avg ms and 1% low FPS (mean of the slowest 1% of
 * samples, converted to FPS).
 */
public final class FrameTimeWindow {
  private final float[] samples;
  private final float[] scratch;
  private int count;
  private int next;

  public FrameTimeWindow(int capacity) {
    if (capacity < 1) {
      throw new IllegalArgumentException("capacity must be >= 1");
    }
    this.samples = new float[capacity];
    this.scratch = new float[capacity];
  }

  public void add(float frameMs) {
    if (!(frameMs > 0f) || Float.isNaN(frameMs) || Float.isInfinite(frameMs)) {
      return;
    }
    samples[next] = frameMs;
    next = (next + 1) % samples.length;
    if (count < samples.length) {
      count++;
    }
  }

  public int size() {
    return count;
  }

  public float avgMs() {
    if (count == 0) {
      return 0f;
    }
    float sum = 0f;
    for (int i = 0; i < count; i++) {
      sum += samples[i];
    }
    return sum / count;
  }

  /**
   * 1% low FPS: average of the worst {@code max(1, ceil(n * 0.01))} frame times, as {@code 1000 /
   * avgWorstMs}.
   */
  public float onePercentLowFps() {
    if (count == 0) {
      return 0f;
    }
    System.arraycopy(samples, 0, scratch, 0, count);
    Arrays.sort(scratch, 0, count);
    int worst = Math.max(1, (int) Math.ceil(count * 0.01));
    float sum = 0f;
    for (int i = count - worst; i < count; i++) {
      sum += scratch[i];
    }
    float avgWorstMs = sum / worst;
    if (!(avgWorstMs > 0f)) {
      return 0f;
    }
    return 1000f / avgWorstMs;
  }

  public void reset() {
    count = 0;
    next = 0;
  }
}
