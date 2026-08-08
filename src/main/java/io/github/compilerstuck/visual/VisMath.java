package io.github.compilerstuck.visual;

/** Small range-mapping helpers used by visualizations (GDX-free). */
public final class VisMath {

  private VisMath() {}

  public static final float PI = (float) Math.PI;

  /** Fixed tilt used by several 3D visuals ({@code PI/3}). */
  public static final float ROT_X_PI_3 = PI / 3f;

  public static final float COS_ROT_X_PI_3 = (float) Math.cos(ROT_X_PI_3);

  public static final float SIN_ROT_X_PI_3 = (float) Math.sin(ROT_X_PI_3);

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

  /**
   * Circular distance on {@code [0, length)} between index {@code i} and value {@code value}, as
   * used by disparity visuals: {@code min(|i-v|, |i-length-v|, |i+length-v|)}.
   */
  public static int circularDistance(int i, int value, int length) {
    return Math.min(
        Math.min(Math.abs(i - value), Math.abs(i - length - value)), Math.abs(i + length - value));
  }
}
