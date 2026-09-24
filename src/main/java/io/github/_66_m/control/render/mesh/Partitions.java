package io.github._66_m.control.render.mesh;

import io.github._66_m.control.config.visual.ModelOptions.AssemblyOrder;
import java.util.Arrays;
import java.util.SplittableRandom;

/** Splits a {@link MeshData} into {@link PieceMesh} pieces: Voronoi-like shards or slabs. */
public final class Partitions {

  /**
   * Largest mesh the piece renderer takes (every triangle becomes 3 GPU vertices of 13 floats);
   * bigger models still work in Point Cloud and Voxels.
   */
  public static final int MAX_PIECE_TRIANGLES = 750_000;

  private Partitions() {}

  /** {@code true} when {@code mesh} is small enough for shards / slices. */
  public static boolean fitsPieces(MeshData mesh) {
    return mesh.triangleCount() <= MAX_PIECE_TRIANGLES;
  }

  // ---------------------------------------------------------------------------------------------
  // Shards

  /**
   * Splits the surface into {@code pieces} shards: area-stratified seed points (refined by one
   * Lloyd step, sorted by {@code order}), every triangle assigned to its nearest seed by centroid.
   * Piece {@code k}'s center is its seed.
   */
  public static PieceMesh shards(MeshData mesh, int pieces, AssemblyOrder order) {
    int p = Math.max(1, pieces);
    int tris = mesh.triangleCount();
    SurfaceSamples seeds0 = SurfaceSamples.sample(mesh, p, order);
    float[] seeds = seeds0.pos.clone();
    float[] centroids = new float[tris * 3];
    float[] areas = new float[tris];
    for (int t = 0; t < tris; t++) {
      for (int c = 0; c < 3; c++) {
        centroids[t * 3 + c] =
            (mesh.positions[t * 9 + c]
                    + mesh.positions[t * 9 + 3 + c]
                    + mesh.positions[t * 9 + 6 + c])
                / 3f;
      }
      areas[t] = MeshMath.triangleArea(mesh.positions, t);
    }
    int[] owner = assign(centroids, tris, seeds, p);
    // One Lloyd step evens out shard sizes.
    float[] sum = new float[p * 3];
    float[] weight = new float[p];
    for (int t = 0; t < tris; t++) {
      int o = owner[t];
      float a = Math.max(areas[t], 1e-9f);
      sum[o * 3] += centroids[t * 3] * a;
      sum[o * 3 + 1] += centroids[t * 3 + 1] * a;
      sum[o * 3 + 2] += centroids[t * 3 + 2] * a;
      weight[o] += a;
    }
    for (int k = 0; k < p; k++) {
      if (weight[k] > 0f) {
        seeds[k * 3] = sum[k * 3] / weight[k];
        seeds[k * 3 + 1] = sum[k * 3 + 1] / weight[k];
        seeds[k * 3 + 2] = sum[k * 3 + 2] / weight[k];
      }
    }
    // Keep piece ids in assembly order after the seeds moved.
    seeds = sortByOrder(seeds, p, order);
    owner = assign(centroids, tris, seeds, p);

    int verts = tris * 3;
    int[] pieceOf = new int[verts];
    for (int t = 0; t < tris; t++) {
      pieceOf[t * 3] = owner[t];
      pieceOf[t * 3 + 1] = owner[t];
      pieceOf[t * 3 + 2] = owner[t];
    }
    return new PieceMesh(
        mesh.positions,
        mesh.normals,
        mesh.uvs,
        mesh.colors,
        pieceOf,
        p,
        seeds,
        randomAxes(p),
        null,
        mesh.texture);
  }

  private static float[] sortByOrder(float[] seeds, int p, AssemblyOrder order) {
    Integer[] idx = new Integer[p];
    double[] keys = new double[p];
    for (int k = 0; k < p; k++) {
      idx[k] = k;
      keys[k] = SurfaceSamples.orderKey(order, seeds[k * 3], seeds[k * 3 + 1], seeds[k * 3 + 2]);
    }
    Arrays.sort(idx, (a, b) -> Double.compare(keys[a], keys[b]));
    float[] out = new float[p * 3];
    for (int k = 0; k < p; k++) {
      System.arraycopy(seeds, idx[k] * 3, out, k * 3, 3);
    }
    return out;
  }

  /** Nearest seed for every point, via a uniform grid over [-1, 1]³ with ring search. */
  static int[] assign(float[] points, int count, float[] seeds, int p) {
    int res = Math.max(1, (int) Math.ceil(Math.cbrt(p)));
    int cells = res * res * res;
    int[] start = new int[cells + 1];
    int[] cellOf = new int[p];
    for (int k = 0; k < p; k++) {
      cellOf[k] = cell(seeds, k, res);
      start[cellOf[k] + 1]++;
    }
    for (int c = 0; c < cells; c++) {
      start[c + 1] += start[c];
    }
    int[] fill = Arrays.copyOf(start, cells);
    int[] members = new int[p];
    for (int k = 0; k < p; k++) {
      members[fill[cellOf[k]]++] = k;
    }
    float cellSize = 2f / res;
    int[] out = new int[count];
    for (int i = 0; i < count; i++) {
      float x = points[i * 3];
      float y = points[i * 3 + 1];
      float z = points[i * 3 + 2];
      int cx = clampCell((x + 1f) * 0.5f * res, res);
      int cy = clampCell((y + 1f) * 0.5f * res, res);
      int cz = clampCell((z + 1f) * 0.5f * res, res);
      int best = 0;
      float bestD = Float.MAX_VALUE;
      for (int r = 0; r <= res; r++) {
        // Anything in ring r is at least (r - 1) cells away; stop once that exceeds the best.
        float minRing = Math.max(0, r - 1) * cellSize;
        if (minRing * minRing > bestD) {
          break;
        }
        for (int dz = -r; dz <= r; dz++) {
          for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
              if (Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) != r) {
                continue;
              }
              int gx = cx + dx;
              int gy = cy + dy;
              int gz = cz + dz;
              if (gx < 0 || gy < 0 || gz < 0 || gx >= res || gy >= res || gz >= res) {
                continue;
              }
              int c = gx + res * (gy + res * gz);
              for (int m = start[c]; m < start[c + 1]; m++) {
                int k = members[m];
                float ex = seeds[k * 3] - x;
                float ey = seeds[k * 3 + 1] - y;
                float ez = seeds[k * 3 + 2] - z;
                float d = ex * ex + ey * ey + ez * ez;
                if (d < bestD) {
                  bestD = d;
                  best = k;
                }
              }
            }
          }
        }
      }
      out[i] = best;
    }
    return out;
  }

  private static int cell(float[] p, int k, int res) {
    int x = clampCell((p[k * 3] + 1f) * 0.5f * res, res);
    int y = clampCell((p[k * 3 + 1] + 1f) * 0.5f * res, res);
    int z = clampCell((p[k * 3 + 2] + 1f) * 0.5f * res, res);
    return x + res * (y + res * z);
  }

  private static int clampCell(float f, int res) {
    return Math.max(0, Math.min(res - 1, (int) f));
  }

  static float[] randomAxes(int p) {
    SplittableRandom rnd = new SplittableRandom(SurfaceSamples.SEED ^ p);
    float[] axes = new float[p * 3];
    for (int k = 0; k < p; k++) {
      double z = rnd.nextDouble() * 2 - 1;
      double a = rnd.nextDouble() * 2 * Math.PI;
      double r = Math.sqrt(1 - z * z);
      axes[k * 3] = (float) (r * Math.cos(a));
      axes[k * 3 + 1] = (float) (r * Math.sin(a));
      axes[k * 3 + 2] = (float) z;
    }
    return axes;
  }

  // ---------------------------------------------------------------------------------------------
  // Slices

  /** Floats per clipped vertex: position 3, normal 3, uv 2, rgb 3. */
  private static final int VF = 11;

  /**
   * Cuts the mesh into {@code pieces} equal slabs along {@code axis} (0 = X, 1 = Y, 2 = Z),
   * clipping triangles at the slab planes. Piece {@code k} is the {@code k}-th slab from the low
   * end; its center holds the slab middle on the axis (other coordinates 0).
   */
  public static PieceMesh slices(MeshData mesh, int pieces, int axis) {
    int p = Math.max(1, pieces);
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    for (int i = axis; i < mesh.positions.length; i += 3) {
      min = Math.min(min, mesh.positions[i]);
      max = Math.max(max, mesh.positions[i]);
    }
    float span = Math.max(1e-6f, max - min);
    float h = span / p;
    Out out = new Out(mesh.triangleCount() * 3 * 2);
    float[] tri = new float[3 * VF];
    float[] polyA = new float[16 * VF];
    float[] polyB = new float[16 * VF];
    for (int t = 0; t < mesh.triangleCount(); t++) {
      loadTriangle(mesh, t, tri);
      float a0 = tri[axis];
      float a1 = tri[VF + axis];
      float a2 = tri[2 * VF + axis];
      int s0 = slab(Math.min(a0, Math.min(a1, a2)), min, h, p);
      int s1 = slab(Math.max(a0, Math.max(a1, a2)), min, h, p);
      if (s0 == s1) {
        out.addPolygon(tri, 3, s0);
        continue;
      }
      for (int s = s0; s <= s1; s++) {
        float lo = min + s * h;
        float hi = min + (s + 1) * h;
        System.arraycopy(tri, 0, polyA, 0, 3 * VF);
        int n = clip(polyA, 3, polyB, axis, lo, true);
        n = clip(polyB, n, polyA, axis, hi, false);
        if (n >= 3) {
          out.addPolygon(polyA, n, s);
        }
      }
    }
    float[] centers = new float[p * 3];
    for (int k = 0; k < p; k++) {
      centers[k * 3 + axis] = min + (k + 0.5f) * h;
    }
    return out.toPieceMesh(mesh, p, centers);
  }

  private static int slab(float v, float min, float h, int p) {
    return Math.max(0, Math.min(p - 1, (int) ((v - min) / h)));
  }

  private static void loadTriangle(MeshData m, int t, float[] tri) {
    for (int c = 0; c < 3; c++) {
      int v = t * 3 + c;
      int o = c * VF;
      tri[o] = m.positions[v * 3];
      tri[o + 1] = m.positions[v * 3 + 1];
      tri[o + 2] = m.positions[v * 3 + 2];
      tri[o + 3] = m.normals[v * 3];
      tri[o + 4] = m.normals[v * 3 + 1];
      tri[o + 5] = m.normals[v * 3 + 2];
      tri[o + 6] = m.uvs != null ? m.uvs[v * 2] : 0f;
      tri[o + 7] = m.uvs != null ? m.uvs[v * 2 + 1] : 0f;
      int col = m.colors != null ? m.colors[v] : 0xFFFFFFFF;
      tri[o + 8] = (col >> 16) & 0xFF;
      tri[o + 9] = (col >> 8) & 0xFF;
      tri[o + 10] = col & 0xFF;
    }
  }

  /**
   * Sutherland–Hodgman against one plane on {@code axis}: keeps {@code v >= plane} when {@code
   * keepAbove}, else {@code v <= plane}. Returns the output vertex count.
   */
  static int clip(float[] in, int n, float[] out, int axis, float plane, boolean keepAbove) {
    int m = 0;
    for (int i = 0; i < n; i++) {
      int j = (i + 1) % n;
      float di = in[i * VF + axis] - plane;
      float dj = in[j * VF + axis] - plane;
      if (!keepAbove) {
        di = -di;
        dj = -dj;
      }
      boolean inI = di >= 0;
      boolean inJ = dj >= 0;
      if (inI) {
        System.arraycopy(in, i * VF, out, m * VF, VF);
        m++;
      }
      if (inI != inJ) {
        float t = di / (di - dj);
        for (int k = 0; k < VF; k++) {
          out[m * VF + k] = in[i * VF + k] + (in[j * VF + k] - in[i * VF + k]) * t;
        }
        out[m * VF + axis] = plane;
        m++;
      }
    }
    return m;
  }

  /** Growable clipped-triangle output. */
  private static final class Out {
    private float[] v;
    private int[] piece;
    private int count; // vertices

    Out(int capacity) {
      v = new float[Math.max(3, capacity) * VF];
      piece = new int[Math.max(3, capacity)];
    }

    void addPolygon(float[] poly, int n, int pieceId) {
      for (int k = 1; k + 1 < n; k++) {
        add(poly, 0, pieceId);
        add(poly, k, pieceId);
        add(poly, k + 1, pieceId);
      }
    }

    private void add(float[] poly, int idx, int pieceId) {
      if (count == piece.length) {
        v = Arrays.copyOf(v, v.length * 2);
        piece = Arrays.copyOf(piece, piece.length * 2);
      }
      System.arraycopy(poly, idx * VF, v, count * VF, VF);
      piece[count] = pieceId;
      count++;
    }

    PieceMesh toPieceMesh(MeshData src, int p, float[] centers) {
      float[] pos = new float[count * 3];
      float[] nrm = new float[count * 3];
      float[] uvs = src.uvs != null ? new float[count * 2] : null;
      int[] colors = src.colors != null ? new int[count] : null;
      for (int i = 0; i < count; i++) {
        int o = i * VF;
        pos[i * 3] = v[o];
        pos[i * 3 + 1] = v[o + 1];
        pos[i * 3 + 2] = v[o + 2];
        nrm[i * 3] = v[o + 3];
        nrm[i * 3 + 1] = v[o + 4];
        nrm[i * 3 + 2] = v[o + 5];
        if (uvs != null) {
          uvs[i * 2] = v[o + 6];
          uvs[i * 2 + 1] = v[o + 7];
        }
        if (colors != null) {
          int r = Math.max(0, Math.min(255, Math.round(v[o + 8])));
          int g = Math.max(0, Math.min(255, Math.round(v[o + 9])));
          int b = Math.max(0, Math.min(255, Math.round(v[o + 10])));
          colors[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
        }
      }
      return new PieceMesh(
          pos,
          nrm,
          uvs,
          colors,
          Arrays.copyOf(piece, count),
          p,
          centers,
          randomAxes(p),
          null,
          src.texture);
    }
  }
}
