package io.github.compilerstuck.control.render;

/**
 * Per-frame GPU submission counters for {@link GdxRenderSystem}. Always updated; overlay/logging is
 * opt-in via {@code --perf-stats}.
 */
public final class FrameStats {
  public float frameMs;
  public int fps;
  public float avgFrameMs;
  public float onePercentLowFps;
  public float heapUsedMb;
  public float heapMaxMb;
  public int spriteEnds;
  public int spriteRenderCalls;

  /** Hardware-instanced Mesh draws. */
  public int instancedDraws;

  public int instancesSubmitted;

  /** World-space 3D line mesh draws. */
  public int lineDraws;

  /** World2D GeometryBatch2D draws. */
  public int geo2dDraws;

  /** Circles/rects/lines/ellipses submitted through GeometryBatch2D. */
  public int geo2dPrimitives;

  public int textDraws;
  public int pixelUploads;

  public void reset() {
    frameMs = 0f;
    fps = 0;
    avgFrameMs = 0f;
    onePercentLowFps = 0f;
    heapUsedMb = 0f;
    heapMaxMb = 0f;
    spriteEnds = 0;
    spriteRenderCalls = 0;
    instancedDraws = 0;
    instancesSubmitted = 0;
    lineDraws = 0;
    geo2dDraws = 0;
    geo2dPrimitives = 0;
    textDraws = 0;
    pixelUploads = 0;
  }

  /** Copy of the last completed frame (for overlay / logging after {@code endFrame}). */
  public void copyTo(FrameStats out) {
    out.frameMs = frameMs;
    out.fps = fps;
    out.avgFrameMs = avgFrameMs;
    out.onePercentLowFps = onePercentLowFps;
    out.heapUsedMb = heapUsedMb;
    out.heapMaxMb = heapMaxMb;
    out.spriteEnds = spriteEnds;
    out.spriteRenderCalls = spriteRenderCalls;
    out.instancedDraws = instancedDraws;
    out.instancesSubmitted = instancesSubmitted;
    out.lineDraws = lineDraws;
    out.geo2dDraws = geo2dDraws;
    out.geo2dPrimitives = geo2dPrimitives;
    out.textDraws = textDraws;
    out.pixelUploads = pixelUploads;
  }

  public String summaryLine() {
    return String.format(
        "perf fps=%d frame=%.2fms avg=%.2fms 1%%low=%.0ffps heap=%.0f/%.0fMB"
            + " spriteEnds=%d spriteCalls=%d instancedDraws=%d instances=%d lineDraws=%d"
            + " geo2dDraws=%d geo2dPrims=%d texts=%d pixels=%d",
        fps,
        frameMs,
        avgFrameMs,
        onePercentLowFps,
        heapUsedMb,
        heapMaxMb,
        spriteEnds,
        spriteRenderCalls,
        instancedDraws,
        instancesSubmitted,
        lineDraws,
        geo2dDraws,
        geo2dPrimitives,
        textDraws,
        pixelUploads);
  }
}
