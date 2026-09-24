package io.github._66_m.control.render.mesh;

/**
 * A mesh split into {@code pieceCount} pieces that the renderer moves independently (shards,
 * slices, globe tiles). Triangle soup like {@link MeshData}, plus a piece id per vertex and per
 * piece metadata (center, spin axis, UV rectangle). Uploaded to the GPU once per instance; treat as
 * immutable.
 */
public final class PieceMesh {
  public final float[] positions;
  public final float[] normals;

  /** 2 floats per vertex, or {@code null}. */
  public final float[] uvs;

  /** ARGB per vertex, or {@code null}. */
  public final int[] colors;

  /** Piece index per vertex. */
  public final int[] pieceOf;

  public final int pieceCount;

  /** Piece centers (3 floats per piece) in model space. */
  public final float[] centers;

  /** Unit spin axis per piece (3 floats), for tumbling shards. */
  public final float[] axes;

  /** Home UV rectangle per piece: u0, v0, u1, v1 (4 floats), or {@code null}. */
  public final float[] uvRects;

  /** Optional texture sampled with {@link #uvs}. */
  public final TextureImage texture;

  public PieceMesh(
      float[] positions,
      float[] normals,
      float[] uvs,
      int[] colors,
      int[] pieceOf,
      int pieceCount,
      float[] centers,
      float[] axes,
      float[] uvRects,
      TextureImage texture) {
    this.positions = positions;
    this.normals = normals;
    this.uvs = uvs;
    this.colors = colors;
    this.pieceOf = pieceOf;
    this.pieceCount = pieceCount;
    this.centers = centers;
    this.axes = axes;
    this.uvRects = uvRects;
    this.texture = texture;
  }

  /** Same geometry (arrays shared) with a different texture. */
  public PieceMesh withTexture(TextureImage newTexture) {
    return new PieceMesh(
        positions, normals, uvs, colors, pieceOf, pieceCount, centers, axes, uvRects, newTexture);
  }

  public int vertexCount() {
    return positions.length / 3;
  }
}
