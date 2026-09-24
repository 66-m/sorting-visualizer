package io.github._66_m.control.render.mesh;

/**
 * GDX-free triangle soup: every triangle owns its three vertices (no index buffer), which keeps
 * splitting into shards / slices trivial. Positions are normalized by {@link MeshNormalizer} to a
 * unit-radius ball around the origin, Y up.
 *
 * <p>Arrays: {@code positions}/{@code normals} = 3 floats per vertex, {@code uvs} = 2 floats per
 * vertex ({@code null} when the model has none), {@code colors} = ARGB per vertex ({@code null}
 * when the model has no color information).
 */
public final class MeshData {
  public final float[] positions;
  public final float[] normals;
  public final float[] uvs;
  public final int[] colors;

  /** Diffuse texture sampled with {@link #uvs}; {@code null} when none. */
  public final TextureImage texture;

  public MeshData(
      float[] positions, float[] normals, float[] uvs, int[] colors, TextureImage texture) {
    if (positions.length % 9 != 0) {
      throw new IllegalArgumentException("positions must hold whole triangles");
    }
    this.positions = positions;
    this.normals = normals;
    this.uvs = uvs;
    this.colors = colors;
    this.texture = texture;
  }

  public int vertexCount() {
    return positions.length / 3;
  }

  public int triangleCount() {
    return positions.length / 9;
  }

  /** {@code true} when {@link #colorAt} returns real model colors (texture or vertex colors). */
  public boolean hasColor() {
    return (texture != null && uvs != null) || colors != null;
  }

  /**
   * Color at barycentric {@code (u, v)} of triangle {@code tri} (weight of vertex 0 is {@code 1 − u
   * − v}): texture when available, else interpolated vertex colors, else mid gray.
   */
  public int colorAt(int tri, float u, float v) {
    int v0 = tri * 3;
    float w0 = 1f - u - v;
    if (texture != null && uvs != null) {
      float tu = w0 * uvs[v0 * 2] + u * uvs[(v0 + 1) * 2] + v * uvs[(v0 + 2) * 2];
      float tv = w0 * uvs[v0 * 2 + 1] + u * uvs[(v0 + 1) * 2 + 1] + v * uvs[(v0 + 2) * 2 + 1];
      return texture.sample(tu, tv);
    }
    if (colors != null) {
      return lerp3(colors[v0], colors[v0 + 1], colors[v0 + 2], w0, u, v);
    }
    return 0xFF999999;
  }

  static int lerp3(int c0, int c1, int c2, float w0, float w1, float w2) {
    int r =
        Math.round(w0 * ((c0 >> 16) & 0xFF) + w1 * ((c1 >> 16) & 0xFF) + w2 * ((c2 >> 16) & 0xFF));
    int g = Math.round(w0 * ((c0 >> 8) & 0xFF) + w1 * ((c1 >> 8) & 0xFF) + w2 * ((c2 >> 8) & 0xFF));
    int b = Math.round(w0 * (c0 & 0xFF) + w1 * (c1 & 0xFF) + w2 * (c2 & 0xFF));
    return 0xFF000000 | (clamp255(r) << 16) | (clamp255(g) << 8) | clamp255(b);
  }

  private static int clamp255(int v) {
    return Math.max(0, Math.min(255, v));
  }
}
