package io.github._66_m.control.render.mesh;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github._66_m.control.config.visual.ModelOptions.AssemblyOrder;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Sampling, voxelizing and partitioning the built-in rocket. */
class MeshPipelineTest {

  private static final MeshData ROCKET = DefaultModel.rocket();

  @Test
  void samplingIsDeterministicAndOrdered() {
    SurfaceSamples a = SurfaceSamples.sample(ROCKET, 500, AssemblyOrder.HEIGHT);
    SurfaceSamples b = SurfaceSamples.sample(ROCKET, 500, AssemblyOrder.HEIGHT);
    assertEquals(500, a.count);
    assertArrayEquals(a.pos, b.pos);
    for (int k = 1; k < a.count; k++) {
      assertTrue(a.pos[k * 3 + 1] >= a.pos[(k - 1) * 3 + 1], "sorted by height");
    }
  }

  @Test
  void radialOrderGrowsOutward() {
    SurfaceSamples s = SurfaceSamples.sample(ROCKET, 300, AssemblyOrder.RADIAL);
    double prev = -1;
    for (int k = 0; k < s.count; k++) {
      double r =
          SurfaceSamples.orderKey(
              AssemblyOrder.RADIAL, s.pos[k * 3], s.pos[k * 3 + 1], s.pos[k * 3 + 2]);
      assertTrue(r >= prev);
      prev = r;
    }
  }

  @Test
  void voxelCountLandsNearTheTarget() {
    for (int target : new int[] {200, 1500}) {
      Voxelizer.Voxels v = Voxelizer.voxelize(ROCKET, target, false, AssemblyOrder.HEIGHT);
      assertTrue(Math.abs(v.count() - target) < target * 0.35, target + " -> " + v.count());
    }
  }

  @Test
  void solidFillAddsInteriorVoxels() {
    int res = 600;
    Voxelizer.Voxels shell = Voxelizer.voxelize(ROCKET, res, false, AssemblyOrder.HEIGHT);
    Voxelizer.Voxels solid = Voxelizer.voxelize(ROCKET, res, true, AssemblyOrder.HEIGHT);
    assertTrue(solid.count() > 0 && shell.count() > 0);
    assertTrue(solid.size() >= shell.size(), "solid needs a coarser grid for the same count");
  }

  @Test
  void shardsAssignEveryTriangleToAValidPiece() {
    PieceMesh shards = Partitions.shards(ROCKET, 64, AssemblyOrder.RADIAL);
    assertEquals(64, shards.pieceCount);
    assertEquals(ROCKET.vertexCount(), shards.vertexCount());
    boolean[] used = new boolean[64];
    for (int p : shards.pieceOf) {
      assertTrue(p >= 0 && p < 64);
      used[p] = true;
    }
    int usedCount = 0;
    for (boolean u : used) {
      usedCount += u ? 1 : 0;
    }
    assertTrue(usedCount > 48, "most shards get triangles: " + usedCount);
  }

  @Test
  void slicesKeepTheSurfaceAreaAndStayInTheirSlab() {
    int pieces = 40;
    PieceMesh slices = Partitions.slices(ROCKET, pieces, 1);
    double before = area(ROCKET.positions);
    double after = area(slices.positions);
    assertEquals(before, after, before * 1e-3);
    float h = slices.centers[4] - slices.centers[1];
    for (int v = 0; v < slices.vertexCount(); v++) {
      int p = slices.pieceOf[v];
      float y = slices.positions[v * 3 + 1];
      assertTrue(Math.abs(y - slices.centers[p * 3 + 1]) <= h / 2 + 1e-4f);
    }
  }

  @Test
  void clipKeepsTheRequestedSide() {
    float[] tri = new float[3 * 11];
    tri[1] = 0f; // y of v0
    tri[11 + 1] = 2f;
    tri[22 + 1] = 2f;
    tri[22] = 1f;
    float[] out = new float[8 * 11];
    int n = Partitions.clip(tri, 3, out, 1, 1f, true);
    assertEquals(4, n);
    for (int i = 0; i < n; i++) {
      assertTrue(out[i * 11 + 1] >= 1f - 1e-6f);
    }
  }

  @Test
  void equalAreaColumnsSumExactlyAndThinOutAtThePoles() {
    for (int n : new int[] {1, 10, 180, 2000, 16_384}) {
      int[] cols = GlobeMeshes.equalAreaColumns(n);
      assertEquals(n, Arrays.stream(cols).sum(), "n=" + n);
      if (cols.length > 4) {
        assertTrue(cols[0] < cols[cols.length / 2]);
      }
    }
  }

  @Test
  void globeTilesCarryUvRectsAndACorePiece() {
    PieceMesh tiles = GlobeMeshes.tiles(GlobeMeshes.equalAreaColumns(100));
    assertEquals(101, tiles.pieceCount);
    for (int p = 0; p < 100; p++) {
      assertTrue(tiles.uvRects[p * 4 + 2] > tiles.uvRects[p * 4]);
      assertTrue(tiles.uvRects[p * 4 + 3] > tiles.uvRects[p * 4 + 1]);
    }
    PieceMesh wedges = GlobeMeshes.wedges(12);
    assertEquals(13, wedges.pieceCount);
  }

  private static double area(float[] p) {
    double sum = 0;
    for (int t = 0; t < p.length / 9; t++) {
      sum += MeshMath.triangleArea(p, t);
    }
    return sum;
  }
}
