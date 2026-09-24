package io.github._66_m.control.render.mesh;

/**
 * Per-frame state of every piece of a {@link PieceMesh}, filled by the visualization and uploaded
 * by the renderer each frame. World units, Y up (World3D).
 */
public final class PieceFrame {

  /** Floats per piece in {@link #data}: 3×4 matrix, color+highlight, UV scale+offset. */
  public static final int FLOATS_PER_PIECE = 20;

  /** How fragments get their base color. */
  public enum ColorMode {
    /** Per-piece color from {@link #setColor}. */
    PIECE,
    /** The mesh's vertex colors. */
    VERTEX,
    /** The mesh's texture (with per-piece UV remap). */
    TEXTURE
  }

  public float[] data = new float[0];
  public int count;
  public ColorMode colorMode = ColorMode.PIECE;

  public void ensureCapacity(int pieces) {
    if (data.length < pieces * FLOATS_PER_PIECE) {
      data = new float[pieces * FLOATS_PER_PIECE];
    }
  }

  /**
   * Sets piece {@code p}'s transform: rotation matrix {@code r} (row-major 3×3, may include a
   * uniform scale) and translation {@code (tx, ty, tz)}: world = r·local + t.
   */
  public void setTransform(int p, float[] r, float tx, float ty, float tz) {
    int o = p * FLOATS_PER_PIECE;
    data[o] = r[0];
    data[o + 1] = r[1];
    data[o + 2] = r[2];
    data[o + 3] = tx;
    data[o + 4] = r[3];
    data[o + 5] = r[4];
    data[o + 6] = r[5];
    data[o + 7] = ty;
    data[o + 8] = r[6];
    data[o + 9] = r[7];
    data[o + 10] = r[8];
    data[o + 11] = tz;
  }

  /** Piece color (used in {@link ColorMode#PIECE}) and highlight toward white (0–1). */
  public void setColor(int p, int argb, float highlight) {
    int o = p * FLOATS_PER_PIECE + 12;
    data[o] = ((argb >> 16) & 0xFF) / 255f;
    data[o + 1] = ((argb >> 8) & 0xFF) / 255f;
    data[o + 2] = (argb & 0xFF) / 255f;
    data[o + 3] = highlight;
  }

  /**
   * Draws the piece in a flat color even in {@link ColorMode#TEXTURE} / {@link ColorMode#VERTEX}
   * (e.g. the Globe's core). Encoded as a negative highlight.
   */
  public void setSolidColor(int p, int argb) {
    setColor(p, argb, -1f);
  }

  /** UV remap for the piece: {@code uv' = uv · scale + offset}. */
  public void setUv(int p, float scaleU, float scaleV, float offsetU, float offsetV) {
    int o = p * FLOATS_PER_PIECE + 16;
    data[o] = scaleU;
    data[o + 1] = scaleV;
    data[o + 2] = offsetU;
    data[o + 3] = offsetV;
  }
}
