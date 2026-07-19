package io.github.compilerstuck.control.render;

/** Scene / session fields for {@link PerfOverlay} (not per-draw GPU counters). */
public final class PerfOverlayContext {
  public int width;
  public int height;
  public int arrayLength;
  public String visualization = "";
  public boolean legacy3d;
  public boolean legacy2d;
  public int stepsPerFrame;
  public boolean running;
}
