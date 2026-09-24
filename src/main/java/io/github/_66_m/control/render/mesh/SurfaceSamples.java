package io.github._66_m.control.render.mesh;

import io.github._66_m.control.config.visual.ModelOptions.AssemblyOrder;
import java.util.Arrays;
import java.util.SplittableRandom;

/**
 * {@code count} points on a mesh surface (position, normal, color), stored in assembly order:
 * sample {@code k} is the home of array index {@code k}.
 */
public final class SurfaceSamples {

  /** Fixed seed: the same model always samples the same points. */
  static final long SEED = 0x5EED_5A3D_1E5L;

  public final int count;
  public final float[] pos;
  public final float[] nrm;
  public final int[] color;

  private SurfaceSamples(int count, float[] pos, float[] nrm, int[] color) {
    this.count = count;
    this.pos = pos;
    this.nrm = nrm;
    this.color = color;
  }

  /**
   * Area-weighted, stratified sampling of {@code count} points, then sorted by {@code order}.
   * Normals are the triangles' interpolated vertex normals.
   */
  public static SurfaceSamples sample(MeshData mesh, int count, AssemblyOrder order) {
    int n = Math.max(1, count);
    int tris = mesh.triangleCount();
    double[] cdf = new double[tris];
    double total = 0;
    for (int t = 0; t < tris; t++) {
      total += MeshMath.triangleArea(mesh.positions, t);
      cdf[t] = total;
    }
    SplittableRandom rnd = new SplittableRandom(SEED);
    float[] pos = new float[n * 3];
    float[] nrm = new float[n * 3];
    int[] color = new int[n];
    float[] p = mesh.positions;
    float[] nm = mesh.normals;
    for (int k = 0; k < n; k++) {
      double target = total > 0 ? (k + rnd.nextDouble()) / n * total : 0;
      int t = total > 0 ? lowerBound(cdf, target) : k % tris;
      double r1 = Math.sqrt(rnd.nextDouble());
      double r2 = rnd.nextDouble();
      float u = (float) (r1 * (1 - r2));
      float v = (float) (r1 * r2);
      float w = 1f - u - v;
      int o = t * 9;
      for (int c = 0; c < 3; c++) {
        pos[k * 3 + c] = w * p[o + c] + u * p[o + 3 + c] + v * p[o + 6 + c];
        nrm[k * 3 + c] = w * nm[o + c] + u * nm[o + 3 + c] + v * nm[o + 6 + c];
      }
      normalize(nrm, k * 3);
      color[k] = mesh.colorAt(t, u, v);
    }
    return reorder(n, pos, nrm, color, order);
  }

  /** Sorts samples by {@link #orderKey}; ties keep sampling order. */
  static SurfaceSamples reorder(int n, float[] pos, float[] nrm, int[] color, AssemblyOrder order) {
    Integer[] idx = new Integer[n];
    double[] keys = new double[n];
    for (int k = 0; k < n; k++) {
      idx[k] = k;
      keys[k] = orderKey(order, pos[k * 3], pos[k * 3 + 1], pos[k * 3 + 2]);
    }
    Arrays.sort(idx, (a, b) -> Double.compare(keys[a], keys[b]));
    float[] p2 = new float[n * 3];
    float[] n2 = new float[n * 3];
    int[] c2 = new int[n];
    for (int k = 0; k < n; k++) {
      int s = idx[k];
      System.arraycopy(pos, s * 3, p2, k * 3, 3);
      System.arraycopy(nrm, s * 3, n2, k * 3, 3);
      c2[k] = color[s];
    }
    return new SurfaceSamples(n, p2, n2, c2);
  }

  /** Sort key of a point for {@code order}; smaller keys come first. */
  public static double orderKey(AssemblyOrder order, float x, float y, float z) {
    return switch (order) {
      case HEIGHT -> y;
      case RADIAL -> Math.sqrt(x * x + y * y + z * z);
      case ANGLE -> Math.atan2(z, x);
      case SPIRAL -> {
        final int bands = 10;
        double band = Math.floor((y + 1.0) * 0.5 * bands);
        double angle = (Math.atan2(z, x) + Math.PI) / (2 * Math.PI);
        yield band + angle;
      }
    };
  }

  private static int lowerBound(double[] cdf, double target) {
    int lo = 0;
    int hi = cdf.length - 1;
    while (lo < hi) {
      int mid = (lo + hi) >>> 1;
      if (cdf[mid] < target) {
        lo = mid + 1;
      } else {
        hi = mid;
      }
    }
    return lo;
  }

  private static void normalize(float[] a, int o) {
    float len = (float) Math.sqrt(a[o] * a[o] + a[o + 1] * a[o + 1] + a[o + 2] * a[o + 2]);
    if (len > 0f) {
      a[o] /= len;
      a[o + 1] /= len;
      a[o + 2] /= len;
    } else {
      a[o + 1] = 1f;
    }
  }
}
