package io.github.compilerstuck.control.render;

/** Optional context for {@link PerfOverlay} (array size, viz name, run state). */
public final class PerfOverlayContext {
  public int width;
  public int height;
  public int arrayLength;
  public String visualization;
  public boolean running;
  public int stepsPerFrame;
}
