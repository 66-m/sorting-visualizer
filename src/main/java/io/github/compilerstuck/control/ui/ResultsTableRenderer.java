package io.github.compilerstuck.control.ui;

import io.github.compilerstuck.control.config.MainControllerConfig;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import java.util.List;

/** Draws the algorithm comparison results table onto a {@link RenderContext}. */
public final class ResultsTableRenderer {

  public void render(
      RenderContext ctx,
      List<SortingAlgorithm> algorithms,
      List<String> comparisons,
      List<String> realTime,
      List<String> swaps,
      List<String> writesMain,
      List<String> writesAux) {
    int width = ctx.getWidth();
    int height = ctx.getHeight();

    ctx.textSize(MainControllerConfig.scaleToWidth(MainControllerConfig.FONT_SIZE_RATIO, width));
    ctx.background(MainControllerConfig.RESULTS_TABLE_BACKGROUND);
    ctx.fill(MainControllerConfig.RESULTS_TABLE_TEXT_COLOR);
    ctx.stroke(MainControllerConfig.RESULTS_TABLE_TEXT_COLOR);

    drawGrid(ctx, width, height);
    drawHeaders(ctx, width);
    drawData(ctx, width, height, algorithms, comparisons, realTime, swaps, writesMain, writesAux);
  }

  private void drawGrid(RenderContext ctx, int width, int height) {
    float columnWidth = width * MainControllerConfig.TABLE_COLUMN_WIDTH_RATIO;

    ctx.line(
        (int) (columnWidth + columnWidth / 2), 0, (int) (columnWidth + columnWidth / 2), height);

    for (int i = 2; i < 7; i++) {
      ctx.line((int) (columnWidth * i), 0, (int) (columnWidth * i), height);
    }
  }

  private void drawHeaders(RenderContext ctx, int width) {
    float columnWidth = width * MainControllerConfig.TABLE_COLUMN_WIDTH_RATIO;
    int textY = MainControllerConfig.scaleToWidth(MainControllerConfig.FONT_SIZE_RATIO, width);

    ctx.text("Alg. name", columnWidth * 0 + 10, textY);
    ctx.text("Elements", columnWidth * 1 + columnWidth / 2 + 5, textY);
    ctx.text("Comparisons", columnWidth * 2 + 10, textY);
    ctx.text("Est. time", columnWidth * 3 + 10, textY);
    ctx.text("Swaps", columnWidth * 4 + 10, textY);
    ctx.text("Writes main", columnWidth * 5 + 10, textY);
    ctx.text("Writes aux", columnWidth * 6 + 10, textY);
  }

  private void drawData(
      RenderContext ctx,
      int width,
      int height,
      List<SortingAlgorithm> algorithms,
      List<String> comparisons,
      List<String> realTime,
      List<String> swaps,
      List<String> writesMain,
      List<String> writesAux) {
    if (algorithms.isEmpty()) {
      return;
    }

    float columnWidth = width * MainControllerConfig.TABLE_COLUMN_WIDTH_RATIO;
    float rowHeight = (height - MainControllerConfig.TABLE_TOP_ROW) / algorithms.size();

    for (int i = 0; i < algorithms.size(); i++) {
      float rowY = MainControllerConfig.TABLE_TOP_ROW + rowHeight * i;
      ctx.line(0, (int) rowY, width, (int) rowY);

      drawRow(
          ctx,
          height,
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
  }

  private void drawRow(
      RenderContext ctx,
      int height,
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
    float rowCenterY =
        rowY + 10 + (height - MainControllerConfig.TABLE_TOP_ROW) / algorithms.size() / 2;

    ctx.text(alg.getName(), columnWidth * 0 + 10, (int) rowCenterY);
    ctx.text(
        String.valueOf(alg.getAlternativeSize()),
        (int) (columnWidth * 1 + columnWidth / 2) + 10,
        (int) rowCenterY);

    if (index < comparisons.size()) {
      ctx.text(
          String.format("%,d", Long.parseLong(comparisons.get(index))),
          (int) (columnWidth * 2) + 10,
          (int) rowCenterY);
    }

    if (index < realTime.size()) {
      String timeStr =
          "~" + TimeEstimateFormat.format(Double.parseDouble(realTime.get(index))) + "ms";
      ctx.text(timeStr, (int) (columnWidth * 3) + 10, (int) rowCenterY);
    }

    if (index < swaps.size()) {
      ctx.text(
          String.format("%,d", Long.parseLong(swaps.get(index))),
          (int) (columnWidth * 4) + 10,
          (int) rowCenterY);
    }

    if (index < writesMain.size()) {
      ctx.text(
          String.format("%,d", Long.parseLong(writesMain.get(index))),
          (int) (columnWidth * 5) + 10,
          (int) rowCenterY);
    }

    if (index < writesAux.size()) {
      ctx.text(
          String.format("%,d", Long.parseLong(writesAux.get(index))),
          (int) (columnWidth * 6) + 10,
          (int) rowCenterY);
    }
  }
}
