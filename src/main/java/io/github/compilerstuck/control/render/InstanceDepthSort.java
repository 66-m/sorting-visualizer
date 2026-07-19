package io.github.compilerstuck.control.render;

import com.badlogic.gdx.math.Vector3;

/**
 * Back-to-front index order for translucent {@link InstanceData} draws. Scratch buffers are reused
 * across frames to avoid steady-state allocation.
 */
public final class InstanceDepthSort {
  private int[] indices;
  private float[] distSq;

  /**
   * True if any instance is not fully opaque ({@code alpha < 255}). Includes alpha {@code 0} so
   * transparent draws disable depth writes and do not punch holes in the scene.
   */
  public static boolean hasTranslucency(InstanceData data) {
    if (data == null || data.argb == null || data.count <= 0) {
      return false;
    }
    for (int i = 0; i < data.count; i++) {
      int a = (data.argb[i] >>> 24) & 0xFF;
      if (a < 255) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns indices {@code [0..count)} sorted far→near by squared distance to {@code cam}. The
   * returned array is owned by this sorter; valid until the next call. Only the first {@code
   * data.count} entries are meaningful.
   */
  public int[] backToFrontOrder(InstanceData data, Vector3 cam) {
    return backToFrontOrder(data, cam.x, cam.y, cam.z);
  }

  public int[] backToFrontOrder(InstanceData data, float camX, float camY, float camZ) {
    int n = data.count;
    ensureCapacity(n);
    for (int i = 0; i < n; i++) {
      int p = i * 3;
      float dx = data.pos[p] - camX;
      float dy = data.pos[p + 1] - camY;
      float dz = data.pos[p + 2] - camZ;
      distSq[i] = dx * dx + dy * dy + dz * dz;
      indices[i] = i;
    }
    quickSortDesc(0, n - 1);
    return indices;
  }

  /** Descending by {@code distSq[indices[i]]} (far first). */
  private void quickSortDesc(int lo, int hi) {
    while (lo < hi) {
      int p = partitionDesc(lo, hi);
      if (p - lo < hi - p) {
        quickSortDesc(lo, p - 1);
        lo = p + 1;
      } else {
        quickSortDesc(p + 1, hi);
        hi = p - 1;
      }
    }
  }

  private int partitionDesc(int lo, int hi) {
    float pivot = distSq[indices[hi]];
    int i = lo;
    for (int j = lo; j < hi; j++) {
      if (distSq[indices[j]] > pivot) {
        swap(i, j);
        i++;
      }
    }
    swap(i, hi);
    return i;
  }

  private void swap(int a, int b) {
    int t = indices[a];
    indices[a] = indices[b];
    indices[b] = t;
  }

  private void ensureCapacity(int n) {
    if (indices != null && indices.length >= n) {
      return;
    }
    indices = new int[n];
    distSq = new float[n];
  }
}
