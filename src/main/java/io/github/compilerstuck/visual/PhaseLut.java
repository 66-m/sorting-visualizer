package io.github.compilerstuck.visual;

/**
 * Cached sin/cos samples for {@code phase = turns * 2π * i / n}. Rebuilds only when {@code n} or
 * {@code turns} change.
 */
public final class PhaseLut {

  private float[] sin;
  private float[] cos;
  private int length = -1;
  private double turns = Double.NaN;

  public float[] sin() {
    return sin;
  }

  public float[] cos() {
    return cos;
  }

  public void ensure(int n, double turns) {
    if (n <= 0) {
      return;
    }
    if (length == n && this.turns == turns) {
      return;
    }
    this.length = n;
    this.turns = turns;
    sin = new float[n];
    cos = new float[n];
    double scale = turns * 2.0 * Math.PI / n;
    for (int i = 0; i < n; i++) {
      double phase = scale * i;
      sin[i] = (float) Math.sin(phase);
      cos[i] = (float) Math.cos(phase);
    }
  }
}
