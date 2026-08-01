package io.github.compilerstuck.control.ui;

import io.github.compilerstuck.control.config.AppConfig;
import io.github.compilerstuck.control.config.CanvasBackground;
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
      CanvasBackground background,
      List<SortingAlgorithm> algorithms,
      List<String> comparisons,
      List<String> realTime,
      List<String> swaps,
      List<String> writesMain,
      List<String> writesAux) {
    int width = rs.getWidth();
    int height = rs.getHeight();

    CanvasBackground bg = background != null ? background : CanvasBackground.DARK;
    float clear = bg.clearComponent();
    rs.clear(clear, clear, clear);
    rs.setOverlayTextGray(bg.overlayTextGray());

    float textSize = AppConfig.scaleToWidth(AppConfig.FONT_SIZE_RATIO, width);
    float topRow = AppConfig.scaleToWidth(AppConfig.TABLE_TOP_ROW, width);
    float cellPad = AppConfig.scaleToWidth(AppConfig.TABLE_CELL_PADDING, width);

    drawGrid(rs, width, height, bg);
    drawHeaders(rs, width, textSize, cellPad);
    drawData(
        rs,
        width,
        height,
        textSize,
        topRow,
        cellPad,
        bg,
        algorithms,
        comparisons,
        realTime,
        swaps,
        writesMain,
        writesAux);
  }

  private void drawGrid(RenderSystem rs, int width, int height, CanvasBackground background) {
    float columnWidth = width * AppConfig.TABLE_COLUMN_WIDTH_RATIO;
    // 1 center divider + 5 column dividers
    int count = 6;
    if (gridLines == null || gridLines.length < count * 4) {
      gridLines = new float[count * 4];
      gridArgb = new int[count];
    }
    int textColor = packGray(background.overlayTextGray());

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

  private void drawHeaders(RenderSystem rs, int width, float textSize, float cellPad) {
    float columnWidth = width * AppConfig.TABLE_COLUMN_WIDTH_RATIO;
    float textY = textSize;
    float halfPad = cellPad * 0.5f;

    rs.drawText("Alg. name", columnWidth * 0 + cellPad, textY, textSize);
    rs.drawText("Elements", columnWidth * 1 + columnWidth / 2 + halfPad, textY, textSize);
    rs.drawText("Comparisons", columnWidth * 2 + cellPad, textY, textSize);
    rs.drawText("Est. real time", columnWidth * 3 + cellPad, textY, textSize);
    rs.drawText("Swaps", columnWidth * 4 + cellPad, textY, textSize);
    rs.drawText("Writes main", columnWidth * 5 + cellPad, textY, textSize);
    rs.drawText("Writes aux", columnWidth * 6 + cellPad, textY, textSize);
  }

  private void drawData(
      RenderSystem rs,
      int width,
      int height,
      float textSize,
      float topRow,
      float cellPad,
      CanvasBackground background,
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
    float rowHeight = (height - topRow) / algorithms.size();
    int textColor = packGray(background.overlayTextGray());

    if (rowLines == null || rowLines.length < algorithms.size() * 4) {
      rowLines = new float[algorithms.size() * 4];
      rowArgb = new int[algorithms.size()];
    }

    for (int i = 0; i < algorithms.size(); i++) {
      float rowY = topRow + rowHeight * i;
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
          topRow,
          cellPad,
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
      float topRow,
      float cellPad,
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
    float rowCenterY = rowY + cellPad + (height - topRow) / algorithms.size() / 2;

    rs.drawText(alg.getName(), columnWidth * 0 + cellPad, rowCenterY, textSize);
    rs.drawText(
        String.valueOf(alg.getAlternativeSize()),
        columnWidth * 1 + columnWidth / 2 + cellPad,
        rowCenterY,
        textSize);

    if (index < comparisons.size()) {
      rs.drawText(
          String.format("%,d", Long.parseLong(comparisons.get(index))),
          columnWidth * 2 + cellPad,
          rowCenterY,
          textSize);
    }

    if (index < realTime.size()) {
      String timeStr =
          "~" + TimeEstimateFormat.format(Double.parseDouble(realTime.get(index))) + "ms";
      rs.drawText(timeStr, columnWidth * 3 + cellPad, rowCenterY, textSize);
    }

    if (index < swaps.size()) {
      rs.drawText(
          String.format("%,d", Long.parseLong(swaps.get(index))),
          columnWidth * 4 + cellPad,
          rowCenterY,
          textSize);
    }

    if (index < writesMain.size()) {
      rs.drawText(
          String.format("%,d", Long.parseLong(writesMain.get(index))),
          columnWidth * 5 + cellPad,
          rowCenterY,
          textSize);
    }

    if (index < writesAux.size()) {
      rs.drawText(
          String.format("%,d", Long.parseLong(writesAux.get(index))),
          columnWidth * 6 + cellPad,
          rowCenterY,
          textSize);
    }
  }

  private static int packGray(int channel) {
    int c = channel & 0xFF;
    return 0xFF000000 | (c << 16) | (c << 8) | c;
  }
}
