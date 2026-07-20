package io.github.compilerstuck.control.render;

/** Screen-space debug overlay for {@link FrameStats} (enabled via {@code --perf-stats}). */
public final class PerfOverlay {
  private static final float TEXT_SIZE = 16f;
  private static final float LINE = 18f;
  private static final float MARGIN_X = 8f;

  public void draw(RenderSystem rs, FrameStats stats) {
    draw(rs, stats, null);
  }

  public void draw(RenderSystem rs, FrameStats stats, PerfOverlayContext ctx) {
    if (rs == null || stats == null) {
      return;
    }
    int h = rs.getHeight();
    float y = h - 8f - LINE;
    int line = 0;

    line = text(rs, "fps " + stats.fps, y, line);
    line =
        text(
            rs,
            String.format(
                "frame %.2f ms  avg %.2f  1%%low %.0f fps",
                stats.frameMs, stats.avgFrameMs, stats.onePercentLowFps),
            y,
            line);
    line =
        text(rs, String.format("heap %.1f / %.1f MB", stats.heapUsedMb, stats.heapMaxMb), y, line);

    if (ctx != null) {
      line = text(rs, ctx.width + "x" + ctx.height + "  N=" + ctx.arrayLength, y, line);
      String viz =
          ctx.visualization == null || ctx.visualization.isEmpty() ? "?" : ctx.visualization;
      line =
          text(
              rs,
              viz + "  " + (ctx.running ? "running" : "idle") + "  steps " + ctx.stepsPerFrame,
              y,
              line);
      line =
          text(
              rs,
              "path 3d="
                  + (ctx.legacy3d ? "legacy" : "instanced")
                  + " 2d="
                  + (ctx.legacy2d ? "legacy" : "geo"),
              y,
              line);
    }

    line = text(rs, "texts " + stats.textDraws + "  pixels " + stats.pixelUploads, y, line);
    line = text(rs, "shapeBegins " + stats.shapeBegins, y, line);
    line =
        text(
            rs,
            "spriteCalls " + stats.spriteRenderCalls + " (ends " + stats.spriteEnds + ")",
            y,
            line);
    line =
        text(
            rs,
            "modelRenders " + stats.modelRenders + " instanced " + stats.instancedDraws,
            y,
            line);
    line = text(rs, "instances " + stats.instancesSubmitted, y, line);
    line = text(rs, "lineDraws " + stats.lineDraws, y, line);
    line = text(rs, "geo2d " + stats.geo2dDraws + " prims " + stats.geo2dPrimitives, y, line);
    text(rs, "modelRestarts " + stats.modelBatchRestarts, y, line);
  }

  private static int text(RenderSystem rs, String s, float y0, int line) {
    rs.drawText(s, MARGIN_X, y0 - line * LINE, TEXT_SIZE);
    return line + 1;
  }
}
