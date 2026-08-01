package io.github.compilerstuck.control.render;

import io.github.compilerstuck.control.config.AppConfig;
import io.github.compilerstuck.control.config.Brand;
import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.model.SortingStateManager;
import io.github.compilerstuck.control.ui.TimeEstimateFormat;
import java.util.Objects;

/** Screen-space watermark and metrics overlay (always after the world pass). */
public final class HudRenderer {
  private static final int LABEL_COUNT = 8;

  private final String[] labels = new String[LABEL_COUNT];
  private final float[] labelYs = new float[LABEL_COUNT];
  private final StringBuilder sb = new StringBuilder(64);

  private String cachedOperation;
  private int cachedSortedPct = Integer.MIN_VALUE;
  private int cachedSegments = Integer.MIN_VALUE;
  private long cachedComparisons = Long.MIN_VALUE;
  private long cachedSwaps = Long.MIN_VALUE;
  private long cachedWrites = Long.MIN_VALUE;
  private long cachedWritesAux = Long.MIN_VALUE;
  private int cachedLength = Integer.MIN_VALUE;
  private String cachedRealTimeFormatted;
  private boolean cachedPreparing;
  private int cachedLayoutWidth = Integer.MIN_VALUE;
  private int cachedTextX;
  private int cachedTextSize;
  private int cachedLineHeight;
  private int cachedWatermarkX;
  private int cachedWatermarkSize;
  private boolean labelsInitialized;

  public void drawWatermark(RenderSystem rs) {
    ensureLayout(rs);
    // Match first metrics line (e.g. "Waiting") so the watermark isn't flush to the top.
    rs.drawText(Brand.WATERMARK, cachedWatermarkX, labelYs[0], cachedWatermarkSize);
  }

  public void drawMeasurements(
      RenderSystem rs, SortingStateManager stateManager, ArrayController arrayController) {
    ensureLayout(rs);
    rebuildLabelsIfDirty(stateManager, arrayController);
    rs.drawTexts(labels, cachedTextX, labelYs, cachedTextSize, LABEL_COUNT);
  }

  /** Package-visible for tests: label array reused across frames. */
  String[] labelsForTest() {
    return labels;
  }

  /** Package-visible for tests: true when metrics/layout match the last rebuild. */
  boolean wouldSkipRebuild(
      SortingStateManager stateManager, ArrayController arrayController, int width) {
    if (!labelsInitialized || width != cachedLayoutWidth) {
      return false;
    }
    return sameKeys(readMetrics(stateManager, arrayController));
  }

  private void ensureLayout(RenderSystem rs) {
    int width = rs.getWidth();
    if (width == cachedLayoutWidth) {
      return;
    }
    cachedLayoutWidth = width;
    cachedTextSize = AppConfig.scaleToWidth(AppConfig.TEXT_Y_OFFSET, width);
    cachedTextX = AppConfig.scaleToWidth(AppConfig.TEXT_X_OFFSET, width);
    cachedLineHeight = AppConfig.scaleToWidth(AppConfig.LINE_HEIGHT_OFFSET, width);
    cachedWatermarkSize = AppConfig.scaleToWidth(AppConfig.WATERMARK_TEXT_SIZE, width);
    // Pin the trailing glyph to the same inset as left-side metrics (not a fixed text-width guess).
    float watermarkW = rs.measureTextWidth(Brand.WATERMARK, cachedWatermarkSize);
    cachedWatermarkX = Math.round(width - watermarkW - cachedTextX);
    for (int i = 0; i < LABEL_COUNT; i++) {
      labelYs[i] = cachedLineHeight * (i + 1);
    }
    // Force label rebuild so layout-dependent strings stay consistent if we later embed sizes.
    labelsInitialized = false;
  }

  private void rebuildLabelsIfDirty(
      SortingStateManager stateManager, ArrayController arrayController) {
    Metrics m = readMetrics(stateManager, arrayController);

    if (labelsInitialized && sameKeys(m)) {
      return;
    }

    cachedOperation = m.operation;
    cachedSortedPct = m.sortedPct;
    cachedSegments = m.segments;
    cachedComparisons = m.comparisons;
    cachedSwaps = m.swaps;
    cachedWrites = m.writes;
    cachedWritesAux = m.writesAux;
    cachedLength = m.length;
    cachedRealTimeFormatted = m.realTime;
    cachedPreparing = m.preparing;

    labels[0] = m.operation;
    labels[1] = buildSortedLine(m.sortedPct, m.segments);
    labels[2] = formatCount(m.comparisons, " Comparisons");
    labels[3] = buildRealTimeLine(m.realTime);
    labels[4] = formatCount(m.swaps, " Swaps");
    labels[5] = formatCount(m.writes, " Writes to main array");
    labels[6] = formatCount(m.writesAux, " Writes to auxiliary array");
    labels[7] = m.length + " Elements";
    labelsInitialized = true;
  }

  private Metrics readMetrics(SortingStateManager stateManager, ArrayController arrayController) {
    int length = arrayController.getLength();
    if (stateManager.isEqualizePreparing()) {
      // Keep end-of-shuffle numbers; only the operation line switches to Prepare..
      if (labelsInitialized) {
        return new Metrics(
            stateManager.getCurrentOperation(),
            cachedSortedPct,
            cachedSegments,
            cachedComparisons,
            cachedSwaps,
            cachedWrites,
            cachedWritesAux,
            length,
            cachedRealTimeFormatted != null
                ? cachedRealTimeFormatted
                : TimeEstimateFormat.format(0),
            true);
      }
      // No prior HUD frame (tests / first draw): show zeros, never live dry-run counters.
      return new Metrics(
          stateManager.getCurrentOperation(),
          0,
          0,
          0L,
          0L,
          0L,
          0L,
          length,
          TimeEstimateFormat.format(0),
          true);
    }
    return new Metrics(
        stateManager.getCurrentOperation(),
        (int) (arrayController.getSortedPercentage() * 100),
        arrayController.getSegments(),
        arrayController.getComparisons(),
        arrayController.getSwaps(),
        arrayController.getWrites(),
        arrayController.getWritesAux(),
        length,
        TimeEstimateFormat.format(arrayController.getRealTime()),
        false);
  }

  private boolean sameKeys(Metrics m) {
    return m.preparing == cachedPreparing
        && m.sortedPct == cachedSortedPct
        && m.segments == cachedSegments
        && m.comparisons == cachedComparisons
        && m.swaps == cachedSwaps
        && m.writes == cachedWrites
        && m.writesAux == cachedWritesAux
        && m.length == cachedLength
        && Objects.equals(m.operation, cachedOperation)
        && Objects.equals(m.realTime, cachedRealTimeFormatted);
  }

  private String buildSortedLine(int sortedPct, int segments) {
    sb.setLength(0);
    sb.append(sortedPct).append("% Sorted (").append(segments).append(" Segments)");
    return sb.toString();
  }

  private String buildRealTimeLine(String realTime) {
    sb.setLength(0);
    sb.append("Est. real time: ~").append(realTime).append("ms");
    return sb.toString();
  }

  private String formatCount(long value, String suffix) {
    sb.setLength(0);
    sb.append(String.format("%,d", value)).append(suffix);
    return sb.toString();
  }

  private record Metrics(
      String operation,
      int sortedPct,
      int segments,
      long comparisons,
      long swaps,
      long writes,
      long writesAux,
      int length,
      String realTime,
      boolean preparing) {}
}
