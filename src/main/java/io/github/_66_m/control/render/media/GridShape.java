package io.github._66_m.control.render.media;

/** Column / row count for cutting a frame into {@code n} roughly square tiles. */
public record GridShape(int cols, int rows) {

  /**
   * Picks {@code cols ≈ √(n·aspect)} and {@code rows = ⌈n / cols⌉}, so tiles are close to square
   * for a frame of {@code aspect = width / height}. Cells past {@code n} stay empty.
   */
  public static GridShape forCount(int n, double aspect) {
    if (n <= 1) {
      return new GridShape(1, 1);
    }
    double a = aspect > 0 && Double.isFinite(aspect) ? aspect : 1.0;
    int cols = (int) Math.max(1, Math.min(n, Math.round(Math.sqrt(n * a))));
    int rows = (n + cols - 1) / cols;
    return new GridShape(cols, rows);
  }
}
