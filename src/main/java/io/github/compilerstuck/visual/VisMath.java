package io.github.compilerstuck.visual;

/** Small range-mapping helpers used by visualizations (GDX-free). */
public final class VisMath {

  private VisMath() {}

  public static final float PI = (float) Math.PI;
  private static final float DEG_TO_RAD = PI / 180f;

  /** Linear map from one range into another (Processing {@code map} semantics). */
  public static float map(float value, float start1, float stop1, float start2, float stop2) {
    float range1 = stop1 - start1;
    if (range1 == 0f) {
      return start2;
    }
    return start2 + (stop2 - start2) * ((value - start1) / range1);
  }

  public static float radians(float degrees) {
    return degrees * DEG_TO_RAD;
  }
}
