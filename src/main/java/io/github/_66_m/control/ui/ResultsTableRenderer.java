package io.github._66_m.control.ui;

import io.github._66_m.control.config.AppConfig;
import io.github._66_m.control.config.CanvasBackground;
import io.github._66_m.control.model.RunResult;
import io.github._66_m.control.render.RenderSystem;
import java.util.List;

/** Draws the algorithm comparison results table onto a {@link RenderSystem}. */
public final class ResultsTableRenderer {

  private float[] gridLines;
  private int[] gridArgb;
  private float[] rowLines;
  private int[] rowArgb;

  public void render(RenderSystem rs, CanvasBackground background, List<RunResult> results) {
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
    drawData(rs, width, height, textSize, topRow, cellPad, bg, results);
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
      List<RunResult> results) {
    if (results.isEmpty()) {
      return;
    }

    float columnWidth = width * AppConfig.TABLE_COLUMN_WIDTH_RATIO;
    float rowHeight = (height - topRow) / results.size();
    int textColor = packGray(background.overlayTextGray());

    if (rowLines == null || rowLines.length < results.size() * 4) {
      rowLines = new float[results.size() * 4];
      rowArgb = new int[results.size()];
    }

    for (int i = 0; i < results.size(); i++) {
      float rowY = topRow + rowHeight * i;
      int o = i * 4;
      rowLines[o] = 0;
      rowLines[o + 1] = rowY;
      rowLines[o + 2] = width;
      rowLines[o + 3] = rowY;
      rowArgb[i] = textColor;

      drawRow(rs, height, textSize, topRow, cellPad, i, columnWidth, rowY, results);
    }
    rs.strokeLines(rowLines, rowArgb, results.size());
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
      List<RunResult> results) {
    RunResult result = results.get(index);
    float rowCenterY = rowY + cellPad + (height - topRow) / results.size() / 2;

    rs.drawText(result.algorithmName(), columnWidth * 0 + cellPad, rowCenterY, textSize);
    rs.drawText(
        String.valueOf(result.elements()),
        columnWidth * 1 + columnWidth / 2 + cellPad,
        rowCenterY,
        textSize);
    rs.drawText(
        String.format("%,d", result.comparisons()),
        columnWidth * 2 + cellPad,
        rowCenterY,
        textSize);
    rs.drawText(
        "~" + TimeEstimateFormat.format(result.realTimeNanos()) + "ms",
        columnWidth * 3 + cellPad,
        rowCenterY,
        textSize);
    rs.drawText(
        String.format("%,d", result.swaps()), columnWidth * 4 + cellPad, rowCenterY, textSize);
    rs.drawText(
        String.format("%,d", result.writesMain()), columnWidth * 5 + cellPad, rowCenterY, textSize);
    rs.drawText(
        String.format("%,d", result.writesAux()), columnWidth * 6 + cellPad, rowCenterY, textSize);
  }

  private static int packGray(int channel) {
    int c = channel & 0xFF;
    return 0xFF000000 | (c << 16) | (c << 8) | c;
  }
}
