package io.github.compilerstuck.control.render;

import io.github.compilerstuck.control.config.Brand;
import io.github.compilerstuck.control.config.MainControllerConfig;
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
  private int cachedLayoutWidth = Integer.MIN_VALUE;
  private int cachedTextX;
  private int cachedTextSize;
  private int cachedLineHeight;
  private boolean labelsInitialized;

  public void drawWatermark(RenderSystem rs) {
    int w = rs.getWidth();
    ensureLayout(w);
    // Match first metrics line (e.g. "Waiting") so the watermark isn't flush to the top.
    rs.drawText(Brand.WATERMARK, w - 190, labelYs[0], 25);
  }

  public void drawMeasurements(
      RenderSystem rs, SortingStateManager stateManager, ArrayController arrayController) {
    int width = rs.getWidth();
    ensureLayout(width);
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
    String operation = stateManager.getCurrentOperation();
    int sortedPct = (int) (arrayController.getSortedPercentage() * 100);
    int segments = arrayController.getSegments();
    long comparisons = arrayController.getComparisons();
    long swaps = arrayController.getSwaps();
    long writes = arrayController.getWrites();
    long writesAux = arrayController.getWritesAux();
    int length = arrayController.getLength();
    String realTime = TimeEstimateFormat.format(arrayController.getRealTime());
    return sameKeys(
        operation, sortedPct, segments, comparisons, swaps, writes, writesAux, length, realTime);
  }

  private void ensureLayout(int width) {
    if (width == cachedLayoutWidth) {
      return;
    }
    cachedLayoutWidth = width;
    cachedTextSize = MainControllerConfig.scaleToWidth(MainControllerConfig.TEXT_Y_OFFSET, width);
    cachedTextX = MainControllerConfig.scaleToWidth(MainControllerConfig.TEXT_X_OFFSET, width);
    cachedLineHeight =
        MainControllerConfig.scaleToWidth(MainControllerConfig.LINE_HEIGHT_OFFSET, width);
    for (int i = 0; i < LABEL_COUNT; i++) {
      labelYs[i] = cachedLineHeight * (i + 1);
    }
    // Force label rebuild so layout-dependent strings stay consistent if we later embed sizes.
    labelsInitialized = false;
  }

  private void rebuildLabelsIfDirty(
      SortingStateManager stateManager, ArrayController arrayController) {
    String operation = stateManager.getCurrentOperation();
    int sortedPct = (int) (arrayController.getSortedPercentage() * 100);
    int segments = arrayController.getSegments();
    long comparisons = arrayController.getComparisons();
    long swaps = arrayController.getSwaps();
    long writes = arrayController.getWrites();
    long writesAux = arrayController.getWritesAux();
    int length = arrayController.getLength();
    String realTime = TimeEstimateFormat.format(arrayController.getRealTime());

    if (labelsInitialized
        && sameKeys(
            operation,
            sortedPct,
            segments,
            comparisons,
            swaps,
            writes,
            writesAux,
            length,
            realTime)) {
      return;
    }

    cachedOperation = operation;
    cachedSortedPct = sortedPct;
    cachedSegments = segments;
    cachedComparisons = comparisons;
    cachedSwaps = swaps;
    cachedWrites = writes;
    cachedWritesAux = writesAux;
    cachedLength = length;
    cachedRealTimeFormatted = realTime;

    labels[0] = operation;
    labels[1] = buildSortedLine(sortedPct, segments);
    labels[2] = formatCount(comparisons, " Comparisons");
    labels[3] = buildRealTimeLine(realTime);
    labels[4] = formatCount(swaps, " Swaps");
    labels[5] = formatCount(writes, " Writes to main array");
    labels[6] = formatCount(writesAux, " Writes to auxiliary array");
    labels[7] = length + " Elements";
    labelsInitialized = true;
  }

  private boolean sameKeys(
      String operation,
      int sortedPct,
      int segments,
      long comparisons,
      long swaps,
      long writes,
      long writesAux,
      int length,
      String realTime) {
    return sortedPct == cachedSortedPct
        && segments == cachedSegments
        && comparisons == cachedComparisons
        && swaps == cachedSwaps
        && writes == cachedWrites
        && writesAux == cachedWritesAux
        && length == cachedLength
        && Objects.equals(operation, cachedOperation)
        && Objects.equals(realTime, cachedRealTimeFormatted);
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
}
