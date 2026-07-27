package io.github.compilerstuck.control.ui;

import io.github.compilerstuck.control.config.AppConfig;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import java.util.List;

/** Draws the algorithm comparison results table onto a {@link RenderSystem}. */
public final class ResultsTableRenderer {

  private float[] gridLines;
  private int[] gridArgb;
  private float[] rowLines;
  private int[] rowArgb;

  public void render(
      RenderSystem rs,
      List<SortingAlgorithm> algorithms,
      List<String> comparisons,
      List<String> realTime,
      List<String> swaps,
      List<String> writesMain,
      List<String> writesAux) {
    int width = rs.getWidth();
    int height = rs.getHeight();

    float bg = AppConfig.RESULTS_TABLE_BACKGROUND / 255f;
    rs.clear(bg, bg, bg);

    float textSize = AppConfig.scaleToWidth(AppConfig.FONT_SIZE_RATIO, width);

    drawGrid(rs, width, height);
    drawHeaders(rs, width, textSize);
    drawData(
        rs,
        width,
        height,
        textSize,
        algorithms,
        comparisons,
        realTime,
        swaps,
        writesMain,
        writesAux);
  }

  private void drawGrid(RenderSystem rs, int width, int height) {
    float columnWidth = width * AppConfig.TABLE_COLUMN_WIDTH_RATIO;
    // 1 center divider + 5 column dividers
    int count = 6;
    if (gridLines == null || gridLines.length < count * 4) {
      gridLines = new float[count * 4];
      gridArgb = new int[count];
    }
    int textColor = packGray(AppConfig.RESULTS_TABLE_TEXT_COLOR);

    float x0 = columnWidth + columnWidth / 2;
    gridLines[0] = x0;
    gridLines[1] = 0;
    gridLines[2] = x0;
    gridLines[3] = height;
    gridArgb[0] = textColor;

    for (int i = 2; i < 7; i++) {
      int o = (i - 1) * 4;
      float x = columnWidth * i;
      gridLines[o] = x;
      gridLines[o + 1] = 0;
      gridLines[o + 2] = x;
      gridLines[o + 3] = height;
      gridArgb[i - 1] = textColor;
    }
    rs.strokeLines(gridLines, gridArgb, count);
  }

  private void drawHeaders(RenderSystem rs, int width, float textSize) {
    float columnWidth = width * AppConfig.TABLE_COLUMN_WIDTH_RATIO;
    float textY = textSize;

    rs.drawText("Alg. name", columnWidth * 0 + 10, textY, textSize);
    rs.drawText("Elements", columnWidth * 1 + columnWidth / 2 + 5, textY, textSize);
    rs.drawText("Comparisons", columnWidth * 2 + 10, textY, textSize);
    rs.drawText("Est. real time", columnWidth * 3 + 10, textY, textSize);
    rs.drawText("Swaps", columnWidth * 4 + 10, textY, textSize);
    rs.drawText("Writes main", columnWidth * 5 + 10, textY, textSize);
    rs.drawText("Writes aux", columnWidth * 6 + 10, textY, textSize);
  }

  private void drawData(
      RenderSystem rs,
      int width,
      int height,
      float textSize,
      List<SortingAlgorithm> algorithms,
      List<String> comparisons,
      List<String> realTime,
      List<String> swaps,
      List<String> writesMain,
      List<String> writesAux) {
    if (algorithms.isEmpty()) {
      return;
    }

    float columnWidth = width * AppConfig.TABLE_COLUMN_WIDTH_RATIO;
    float rowHeight = (height - AppConfig.TABLE_TOP_ROW) / algorithms.size();
    int textColor = packGray(AppConfig.RESULTS_TABLE_TEXT_COLOR);

    if (rowLines == null || rowLines.length < algorithms.size() * 4) {
      rowLines = new float[algorithms.size() * 4];
      rowArgb = new int[algorithms.size()];
    }

    for (int i = 0; i < algorithms.size(); i++) {
      float rowY = AppConfig.TABLE_TOP_ROW + rowHeight * i;
      int o = i * 4;
      rowLines[o] = 0;
      rowLines[o + 1] = rowY;
      rowLines[o + 2] = width;
      rowLines[o + 3] = rowY;
      rowArgb[i] = textColor;

      drawRow(
          rs,
          height,
          textSize,
          i,
          columnWidth,
          rowY,
          algorithms,
          comparisons,
          realTime,
          swaps,
          writesMain,
          writesAux);
    }
    rs.strokeLines(rowLines, rowArgb, algorithms.size());
  }

  private void drawRow(
      RenderSystem rs,
      int height,
      float textSize,
      int index,
      float columnWidth,
      float rowY,
      List<SortingAlgorithm> algorithms,
      List<String> comparisons,
      List<String> realTime,
      List<String> swaps,
      List<String> writesMain,
      List<String> writesAux) {
    SortingAlgorithm alg = algorithms.get(index);
    float rowCenterY = rowY + 10 + (height - AppConfig.TABLE_TOP_ROW) / algorithms.size() / 2;

    rs.drawText(alg.getName(), columnWidth * 0 + 10, rowCenterY, textSize);
    rs.drawText(
        String.valueOf(alg.getAlternativeSize()),
        (int) (columnWidth * 1 + columnWidth / 2) + 10,
        rowCenterY,
        textSize);

    if (index < comparisons.size()) {
      rs.drawText(
          String.format("%,d", Long.parseLong(comparisons.get(index))),
          (int) (columnWidth * 2) + 10,
          rowCenterY,
          textSize);
    }

    if (index < realTime.size()) {
      String timeStr =
          "~" + TimeEstimateFormat.format(Double.parseDouble(realTime.get(index))) + "ms";
      rs.drawText(timeStr, (int) (columnWidth * 3) + 10, rowCenterY, textSize);
    }

    if (index < swaps.size()) {
      rs.drawText(
          String.format("%,d", Long.parseLong(swaps.get(index))),
          (int) (columnWidth * 4) + 10,
          rowCenterY,
          textSize);
    }

    if (index < writesMain.size()) {
      rs.drawText(
          String.format("%,d", Long.parseLong(writesMain.get(index))),
          (int) (columnWidth * 5) + 10,
          rowCenterY,
          textSize);
    }

    if (index < writesAux.size()) {
      rs.drawText(
          String.format("%,d", Long.parseLong(writesAux.get(index))),
          (int) (columnWidth * 6) + 10,
          rowCenterY,
          textSize);
    }
  }

  private static int packGray(int channel) {
    int c = channel & 0xFF;
    return 0xFF000000 | (c << 16) | (c << 8) | c;
  }
}
