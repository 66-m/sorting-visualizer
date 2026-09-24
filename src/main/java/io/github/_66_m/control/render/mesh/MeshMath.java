package io.github._66_m.control.render.mesh;

/** Small geometry helpers over triangle-soup arrays. */
public final class MeshMath {

  private MeshMath() {}

  /** Writes the flat face normal of every triangle into its three vertex normals. */
  public static void faceNormals(float[] positions, float[] normals) {
    for (int t = 0; t < positions.length / 9; t++) {
      int o = t * 9;
      float ax = positions[o + 3] - positions[o];
      float ay = positions[o + 4] - positions[o + 1];
      float az = positions[o + 5] - positions[o + 2];
      float bx = positions[o + 6] - positions[o];
      float by = positions[o + 7] - positions[o + 1];
      float bz = positions[o + 8] - positions[o + 2];
      float nx = ay * bz - az * by;
      float ny = az * bx - ax * bz;
      float nz = ax * by - ay * bx;
      float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
      if (len > 0f) {
        nx /= len;
        ny /= len;
        nz /= len;
      } else {
        ny = 1f;
      }
      for (int k = 0; k < 3; k++) {
        normals[o + k * 3] = nx;
        normals[o + k * 3 + 1] = ny;
        normals[o + k * 3 + 2] = nz;
      }
    }
  }

  /** Area of triangle {@code t}. */
  public static float triangleArea(float[] p, int t) {
    int o = t * 9;
    float ax = p[o + 3] - p[o];
    float ay = p[o + 4] - p[o + 1];
    float az = p[o + 5] - p[o + 2];
    float bx = p[o + 6] - p[o];
    float by = p[o + 7] - p[o + 1];
    float bz = p[o + 8] - p[o + 2];
    float cx = ay * bz - az * by;
    float cy = az * bx - ax * bz;
    float cz = ax * by - ay * bx;
    return 0.5f * (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
  }

  /**
   * Returns a copy of {@code mesh} (rotated so +Z becomes +Y when {@code zUp}), centered on its
   * bounding box and scaled so the farthest vertex sits at distance 1 from the origin. Normals are
   * rotated along and re-normalized.
   */
  public static MeshData normalize(MeshData mesh, boolean zUp) {
    float[] p = mesh.positions.clone();
    float[] n = mesh.normals.clone();
    if (zUp) {
      // (x, y, z) -> (x, z, -y)
      for (int i = 0; i < p.length; i += 3) {
        float y = p[i + 1];
        p[i + 1] = p[i + 2];
        p[i + 2] = -y;
        float ny = n[i + 1];
        n[i + 1] = n[i + 2];
        n[i + 2] = -ny;
      }
    }
    float minX = Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float minZ = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE;
    float maxY = -Float.MAX_VALUE;
    float maxZ = -Float.MAX_VALUE;
    for (int i = 0; i < p.length; i += 3) {
      minX = Math.min(minX, p[i]);
      maxX = Math.max(maxX, p[i]);
      minY = Math.min(minY, p[i + 1]);
      maxY = Math.max(maxY, p[i + 1]);
      minZ = Math.min(minZ, p[i + 2]);
      maxZ = Math.max(maxZ, p[i + 2]);
    }
    float cx = (minX + maxX) / 2f;
    float cy = (minY + maxY) / 2f;
    float cz = (minZ + maxZ) / 2f;
    float maxR2 = 0f;
    for (int i = 0; i < p.length; i += 3) {
      p[i] -= cx;
      p[i + 1] -= cy;
      p[i + 2] -= cz;
      maxR2 = Math.max(maxR2, p[i] * p[i] + p[i + 1] * p[i + 1] + p[i + 2] * p[i + 2]);
    }
    float scale = maxR2 > 0f ? 1f / (float) Math.sqrt(maxR2) : 1f;
    for (int i = 0; i < p.length; i++) {
      p[i] *= scale;
    }
    for (int i = 0; i < n.length; i += 3) {
      float len = (float) Math.sqrt(n[i] * n[i] + n[i + 1] * n[i + 1] + n[i + 2] * n[i + 2]);
      if (len > 0f) {
        n[i] /= len;
        n[i + 1] /= len;
        n[i + 2] /= len;
      }
    }
    return new MeshData(p, n, mesh.uvs, mesh.colors, mesh.texture);
  }
}
