package io.github._66_m.control.render.mesh;

import io.github._66_m.control.config.visual.ModelOptions.AssemblyOrder;
import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * Turns a normalized mesh into roughly {@code target} cubes on a regular grid. Surface cells come
 * from dense surface samples; {@code solid} additionally flood-fills the inside. The grid
 * resolution is searched so the voxel count lands as close to the target as possible.
 */
public final class Voxelizer {

  static final int MIN_RES = 2;
  static final int MAX_RES = 200;

  /** Voxel centers in assembly order, plus per-voxel normal and color (arrays: do not mutate). */
  public static final class Voxels {
    private final int count;
    private final float size;
    private final float[] pos;
    private final float[] nrm;
    private final int[] color;

    Voxels(int count, float size, float[] pos, float[] nrm, int[] color) {
      this.count = count;
      this.size = size;
      this.pos = pos;
      this.nrm = nrm;
      this.color = color;
    }

    public int count() {
      return count;
    }

    /** Edge length of one voxel in model units. */
    public float size() {
      return size;
    }

    public float[] pos() {
      return pos;
    }

    public float[] nrm() {
      return nrm;
    }

    public int[] color() {
      return color;
    }
  }

  private Voxelizer() {}

  public static Voxels voxelize(MeshData mesh, int target, boolean solid, AssemblyOrder order) {
    int want = Math.max(1, target);
    int samples = Math.max(20_000, Math.min(800_000, want * 8));
    SurfaceSamples dense = SurfaceSamples.sample(mesh, samples, AssemblyOrder.HEIGHT);

    int lo = MIN_RES;
    int hi = MAX_RES;
    Grid best = null;
    while (lo <= hi) {
      int mid = (lo + hi) >>> 1;
      Grid grid = Grid.build(dense, mid, solid);
      if (best == null || Math.abs(grid.count - want) < Math.abs(best.count - want)) {
        best = grid;
      }
      if (grid.count < want) {
        lo = mid + 1;
      } else if (grid.count > want) {
        hi = mid - 1;
      } else {
        break;
      }
    }
    return best.toVoxels(order);
  }

  private static final class Grid {
    final int res;
    final float cell;
    final int count;
    final int[] cells; // occupied cell ids
    final float[] nx;
    final float[] ny;
    final float[] nz;
    final long[] rSum;
    final long[] gSum;
    final long[] bSum;
    final int[] hits;

    private Grid(
        int res,
        int[] cells,
        float[] nx,
        float[] ny,
        float[] nz,
        long[] rSum,
        long[] gSum,
        long[] bSum,
        int[] hits) {
      this.res = res;
      this.cell = 2f / res;
      this.cells = cells;
      this.count = cells.length;
      this.nx = nx;
      this.ny = ny;
      this.nz = nz;
      this.rSum = rSum;
      this.gSum = gSum;
      this.bSum = bSum;
      this.hits = hits;
    }

    static Grid build(SurfaceSamples s, int res, boolean solid) {
      int total = res * res * res;
      int[] slot = new int[total];
      Arrays.fill(slot, -1);
      int occupied = 0;
      int[] cellOf = new int[s.count];
      for (int k = 0; k < s.count; k++) {
        int c = cellId(s.pos, k, res);
        cellOf[k] = c;
        if (slot[c] < 0) {
          slot[c] = occupied++;
        }
      }
      if (solid) {
        occupied = fillInterior(slot, res, occupied);
      }
      int[] cells = new int[occupied];
      for (int c = 0; c < total; c++) {
        if (slot[c] >= 0) {
          cells[slot[c]] = c;
        }
      }
      float[] nx = new float[occupied];
      float[] ny = new float[occupied];
      float[] nz = new float[occupied];
      long[] r = new long[occupied];
      long[] g = new long[occupied];
      long[] b = new long[occupied];
      int[] hits = new int[occupied];
      for (int k = 0; k < s.count; k++) {
        int v = slot[cellOf[k]];
        nx[v] += s.nrm[k * 3];
        ny[v] += s.nrm[k * 3 + 1];
        nz[v] += s.nrm[k * 3 + 2];
        int c = s.color[k];
        r[v] += (c >> 16) & 0xFF;
        g[v] += (c >> 8) & 0xFF;
        b[v] += c & 0xFF;
        hits[v]++;
      }
      return new Grid(res, cells, nx, ny, nz, r, g, b, hits);
    }

    /** Marks every cell not reachable from the grid border as occupied (the model's inside). */
    private static int fillInterior(int[] slot, int res, int occupied) {
      int total = res * res * res;
      boolean[] outside = new boolean[total];
      ArrayDeque<Integer> queue = new ArrayDeque<>();
      for (int z = 0; z < res; z++) {
        for (int y = 0; y < res; y++) {
          for (int x = 0; x < res; x++) {
            boolean border =
                x == 0 || y == 0 || z == 0 || x == res - 1 || y == res - 1 || z == res - 1;
            int c = x + res * (y + res * z);
            if (border && slot[c] < 0 && !outside[c]) {
              outside[c] = true;
              queue.add(c);
            }
          }
        }
      }
      int[] dx = {1, -1, 0, 0, 0, 0};
      int[] dy = {0, 0, 1, -1, 0, 0};
      int[] dz = {0, 0, 0, 0, 1, -1};
      while (!queue.isEmpty()) {
        int c = queue.poll();
        int x = c % res;
        int y = (c / res) % res;
        int z = c / (res * res);
        for (int d = 0; d < 6; d++) {
          int nx = x + dx[d];
          int ny = y + dy[d];
          int nz = z + dz[d];
          if (nx < 0 || ny < 0 || nz < 0 || nx >= res || ny >= res || nz >= res) {
            continue;
          }
          int n = nx + res * (ny + res * nz);
          if (!outside[n] && slot[n] < 0) {
            outside[n] = true;
            queue.add(n);
          }
        }
      }
      for (int c = 0; c < total; c++) {
        if (slot[c] < 0 && !outside[c]) {
          slot[c] = occupied++;
        }
      }
      return occupied;
    }

    Voxels toVoxels(AssemblyOrder order) {
      float[] pos = new float[count * 3];
      float[] nrm = new float[count * 3];
      int[] color = new int[count];
      for (int v = 0; v < count; v++) {
        int c = cells[v];
        int x = c % res;
        int y = (c / res) % res;
        int z = c / (res * res);
        float px = -1f + (x + 0.5f) * cell;
        float py = -1f + (y + 0.5f) * cell;
        float pz = -1f + (z + 0.5f) * cell;
        pos[v * 3] = px;
        pos[v * 3 + 1] = py;
        pos[v * 3 + 2] = pz;
        float ax = nx[v];
        float ay = ny[v];
        float az = nz[v];
        if (hits[v] == 0) {
          // Interior voxel: point away from the center, darker than the surface.
          ax = px;
          ay = py;
          az = pz;
          color[v] = 0xFF505050;
        } else {
          int h = hits[v];
          color[v] =
              0xFF000000
                  | (int) (rSum[v] / h) << 16
                  | (int) (gSum[v] / h) << 8
                  | (int) (bSum[v] / h);
        }
        float len = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        if (len > 0f) {
          nrm[v * 3] = ax / len;
          nrm[v * 3 + 1] = ay / len;
          nrm[v * 3 + 2] = az / len;
        } else {
          nrm[v * 3 + 1] = 1f;
        }
      }
      SurfaceSamples ordered = SurfaceSamples.reorder(count, pos, nrm, color, order);
      return new Voxels(count, cell, ordered.pos, ordered.nrm, ordered.color);
    }

    private static int cellId(float[] p, int k, int res) {
      int x = clampCell((p[k * 3] + 1f) * 0.5f * res, res);
      int y = clampCell((p[k * 3 + 1] + 1f) * 0.5f * res, res);
      int z = clampCell((p[k * 3 + 2] + 1f) * 0.5f * res, res);
      return x + res * (y + res * z);
    }

    private static int clampCell(float f, int res) {
      return Math.max(0, Math.min(res - 1, (int) f));
    }
  }
}
