package io.github.compilerstuck.control.render;

/**
 * Reusable per-frame instance buffer for 3D batch draws. Arrays are owned by the visual and grown
 * as needed; {@link #count} is the live instance count.
 *
 * <p>{@link #pos} and {@link #eulerRad} are Y-up world units (World3D, scene center).
 */
public final class InstanceData {
  public float[] pos; // x,y,z — World3D Y-up
  public float[] scale; // sx,sy,sz
  public float[] eulerRad; // rotX,rotY,rotZ in Y-up world radians
  public int[] argb;
  public int count;

  public void ensureCapacity(int n) {
    if (pos == null || pos.length < n * 3) {
      pos = new float[n * 3];
      scale = new float[n * 3];
      eulerRad = new float[n * 3];
      argb = new int[n];
    }
  }

  public void set(
      int i,
      float x,
      float y,
      float z,
      float sx,
      float sy,
      float sz,
      float rx,
      float ry,
      float rz,
      int color) {
    int p = i * 3;
    pos[p] = x;
    pos[p + 1] = y;
    pos[p + 2] = z;
    scale[p] = sx;
    scale[p + 1] = sy;
    scale[p + 2] = sz;
    eulerRad[p] = rx;
    eulerRad[p + 1] = ry;
    eulerRad[p + 2] = rz;
    argb[i] = color;
  }
}
