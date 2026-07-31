package io.github.compilerstuck.control.render;

import io.github.compilerstuck.control.config.AppConfig;

/** Screen-space debug overlay for {@link FrameStats} (enabled via {@code --perf-stats}). */
public final class PerfOverlay {

  public void draw(RenderSystem rs, FrameStats stats) {
    draw(rs, stats, null);
  }

  public void draw(RenderSystem rs, FrameStats stats, PerfOverlayContext ctx) {
    if (rs == null || stats == null) {
      return;
    }
    Layout layout = Layout.forWidth(rs.getWidth(), rs.getHeight());

    layout.line(rs, "fps " + stats.fps);
    layout.line(
        rs,
        String.format(
            "frame %.2f ms  avg %.2f  1%%low %.0f fps",
            stats.frameMs, stats.avgFrameMs, stats.onePercentLowFps));
    layout.line(rs, String.format("heap %.1f / %.1f MB", stats.heapUsedMb, stats.heapMaxMb));

    if (ctx != null) {
      layout.line(rs, ctx.width + "x" + ctx.height + "  N=" + ctx.arrayLength);
      String viz =
          ctx.visualization == null || ctx.visualization.isEmpty() ? "?" : ctx.visualization;
      layout.line(
          rs, viz + "  " + (ctx.running ? "running" : "idle") + "  steps " + ctx.stepsPerFrame);
      layout.line(rs, "path 3d=instanced  2d=geo");
    }

    layout.line(rs, "texts " + stats.textDraws + "  pixels " + stats.pixelUploads);
    layout.line(rs, "spriteCalls " + stats.spriteRenderCalls + " (ends " + stats.spriteEnds + ")");
    layout.line(rs, "instanced " + stats.instancedDraws);
    layout.line(rs, "instances " + stats.instancesSubmitted);
    layout.line(rs, "lineDraws " + stats.lineDraws);
    layout.line(rs, "geo2d " + stats.geo2dDraws + " prims " + stats.geo2dPrimitives);
  }

  private static final class Layout {
    private final float x;
    private final float y0;
    private final float lineH;
    private final float textSize;
    private int line;

    private Layout(float x, float y0, float lineH, float textSize) {
      this.x = x;
      this.y0 = y0;
      this.lineH = lineH;
      this.textSize = textSize;
    }

    static Layout forWidth(int width, int height) {
      float textSize = AppConfig.scaleToWidth(AppConfig.PERF_TEXT_SIZE, width);
      float lineH = AppConfig.scaleToWidth(AppConfig.PERF_LINE_HEIGHT, width);
      float margin = AppConfig.scaleToWidth(AppConfig.PERF_MARGIN, width);
      return new Layout(margin, height - margin - lineH, lineH, textSize);
    }

    void line(RenderSystem rs, String s) {
      rs.drawText(s, x, y0 - line * lineH, textSize);
      line++;
    }
  }
}
