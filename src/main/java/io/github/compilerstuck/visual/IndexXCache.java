package io.github.compilerstuck.visual;

/** Cached {@code map(i, 0, n, 0, screenWidth)} X positions for bar-style visuals. */
public final class IndexXCache {

  private float[] xs;
  private int length = -1;
  private int screenWidth = -1;

  public float[] xs() {
    return xs;
  }

  public void ensure(int n, int screenWidth) {
    if (n <= 0) {
      return;
    }
    if (length == n && this.screenWidth == screenWidth) {
      return;
    }
    this.length = n;
    this.screenWidth = screenWidth;
    if (xs == null || xs.length < n) {
      xs = new float[n];
    }
    for (int i = 0; i < n; i++) {
      xs[i] = VisMath.map(i, 0, n, 0, screenWidth);
    }
  }
}
