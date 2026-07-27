package io.github.compilerstuck.control.render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * Packs {@link InstanceData} into a GPU instance buffer: 4×vec4 world matrix + RGBA (20 floats per
 * instance). Positions and euler are Y-up world units.
 */
public final class InstanceTransform {
  /** Floats per instance: mat4 (16) + color (4). */
  public static final int FLOATS_PER_INSTANCE = 20;

  private final Matrix4 tmpMatrix = new Matrix4();
  private final Vector3 tmpPos = new Vector3();

  private float sceneW = 1280f;
  private float sceneH = 720f;

  public void setSceneSize(float width, float height) {
    sceneW = width;
    sceneH = height;
  }

  public float sceneW() {
    return sceneW;
  }

  public float sceneH() {
    return sceneH;
  }

  /**
   * Packs {@code data.count} instances into {@code out} starting at index 0. {@code out.length}
   * must be ≥ {@code data.count * FLOATS_PER_INSTANCE}.
   *
   * @return number of floats written
   */
  public int pack(InstanceData data, float[] out) {
    if (data == null || data.count <= 0) {
      return 0;
    }
    int written = 0;
    for (int i = 0; i < data.count; i++) {
      written += packOne(data, i, out, written);
    }
    return written;
  }

  /**
   * Packs instances in {@code order[0..data.count)} sequence (e.g. back-to-front for translucency).
   *
   * @return number of floats written
   */
  public int packOrdered(InstanceData data, int[] order, float[] out) {
    if (data == null || data.count <= 0) {
      return 0;
    }
    if (order == null) {
      return pack(data, out);
    }
    int written = 0;
    for (int k = 0; k < data.count; k++) {
      written += packOne(data, order[k], out, written);
    }
    return written;
  }

  /** Packs a single instance at {@code instanceIndex} into {@code out} at {@code outOffset}. */
  public int packOne(InstanceData data, int instanceIndex, float[] out, int outOffset) {
    buildMatrix(data, instanceIndex, tmpMatrix);
    float[] m = tmpMatrix.val;
    int o = outOffset;
    System.arraycopy(m, 0, out, o, 16);
    o += 16;
    unpackArgb(data.argb[instanceIndex], out, o);
    return FLOATS_PER_INSTANCE;
  }

  /** Builds the world matrix for one instance into {@code out}. */
  public void buildMatrix(InstanceData data, int instanceIndex, Matrix4 out) {
    int p = instanceIndex * 3;
    tmpPos.set(data.pos[p], data.pos[p + 1], data.pos[p + 2]);
    out.idt();
    out.translate(tmpPos);
    if (data.eulerRad != null) {
      out.rotateRad(Vector3.X, data.eulerRad[p]);
      out.rotateRad(Vector3.Y, data.eulerRad[p + 1]);
      out.rotateRad(Vector3.Z, data.eulerRad[p + 2]);
    }
    out.scale(data.scale[p], data.scale[p + 1], data.scale[p + 2]);
  }

  /**
   * Transforms a local-space point by the same world matrix as {@link #buildMatrix} and writes
   * world XYZ into {@code out} at {@code outOffset}.
   */
  public void transformLocalPoint(
      InstanceData data,
      int instanceIndex,
      float lx,
      float ly,
      float lz,
      float[] out,
      int outOffset) {
    buildMatrix(data, instanceIndex, tmpMatrix);
    mulPoint(tmpMatrix.val, lx, ly, lz, out, outOffset);
  }

  /**
   * Transforms {@code localCount} packed local XYZ triples ({@code localXyz}) by the instance world
   * matrix into {@code out} (same packing).
   */
  public void transformLocalPoints(
      InstanceData data, int instanceIndex, float[] localXyz, int localCount, float[] out) {
    buildMatrix(data, instanceIndex, tmpMatrix);
    float[] m = tmpMatrix.val;
    for (int c = 0; c < localCount; c++) {
      int o = c * 3;
      mulPoint(m, localXyz[o], localXyz[o + 1], localXyz[o + 2], out, o);
    }
  }

  private static void mulPoint(
      float[] m, float lx, float ly, float lz, float[] out, int outOffset) {
    out[outOffset] = m[0] * lx + m[4] * ly + m[8] * lz + m[12];
    out[outOffset + 1] = m[1] * lx + m[5] * ly + m[9] * lz + m[13];
    out[outOffset + 2] = m[2] * lx + m[6] * ly + m[10] * lz + m[14];
  }

  /**
   * Unpacks ARGB into RGBA floats in {@code [0,1]}. Alpha {@code 0} is true transparency (callers
   * that want opaque must pass {@code 0xFF……}).
   */
  static void unpackArgb(int argb, float[] out, int offset) {
    out[offset] = ((argb >>> 16) & 0xFF) / 255f;
    out[offset + 1] = ((argb >>> 8) & 0xFF) / 255f;
    out[offset + 2] = (argb & 0xFF) / 255f;
    out[offset + 3] = ((argb >>> 24) & 0xFF) / 255f;
  }
}
