package io.github._66_m.control.render.mesh;

/**
 * Built-in model used until the user picks an OBJ file: a vertex-colored rocket (body, stripe, nose
 * cone, four fins, nozzle), generated in code so nothing has to be bundled.
 */
public final class DefaultModel {

  private static final int WHITE = 0xFFEDEDED;
  private static final int RED = 0xFFD7263D;
  private static final int DARK = 0xFF3A3F4B;
  private static final int WINDOW = 0xFF4FC3F7;
  private static final int FLAME = 0xFFFFA000;

  private static volatile MeshData rocket;

  private DefaultModel() {}

  /** The normalized default rocket (built once). */
  public static MeshData rocket() {
    MeshData m = rocket;
    if (m == null) {
      synchronized (DefaultModel.class) {
        m = rocket;
        if (m == null) {
          m = MeshMath.normalize(build(), false);
          rocket = m;
        }
      }
    }
    return m;
  }

  static MeshData build() {
    Builder b = new Builder();
    int seg = 48;
    float r = 0.28f;
    // Nozzle flare, body sections with a stripe band, nose cone.
    b.lathe(seg, new float[] {-1.18f, -1.0f}, new float[] {0.20f, 0.14f}, FLAME);
    b.lathe(seg, new float[] {-1.0f, -0.95f}, new float[] {0.14f, r}, DARK);
    b.lathe(seg, new float[] {-0.95f, -0.55f}, new float[] {r, r}, WHITE);
    b.lathe(seg, new float[] {-0.55f, -0.40f}, new float[] {r, r}, RED);
    b.lathe(seg, new float[] {-0.40f, 0.45f}, new float[] {r, r}, WHITE);
    b.lathe(seg, new float[] {0.45f, 0.55f}, new float[] {r, r * 0.98f}, DARK);
    b.lathe(
        seg,
        new float[] {0.55f, 0.75f, 0.95f, 1.10f, 1.20f},
        new float[] {r * 0.98f, r * 0.85f, r * 0.55f, r * 0.2f, 0f},
        RED);
    // Porthole: a small disc slightly outside the body facing +Z.
    b.disc(0f, 0.15f, r + 0.005f, 0.09f, 24, WINDOW);
    // Fins.
    for (int k = 0; k < 4; k++) {
      double a = Math.PI / 4 + k * Math.PI / 2;
      b.fin((float) Math.cos(a), (float) Math.sin(a), r, RED);
    }
    return b.toMesh();
  }

  private static final class Builder {
    private final ObjLoader.FloatList pos = new ObjLoader.FloatList();
    private final ObjLoader.IntList col = new ObjLoader.IntList();

    void tri(float[] a, float[] b, float[] c, int color) {
      for (float[] v : new float[][] {a, b, c}) {
        pos.add(v[0]);
        pos.add(v[1]);
        pos.add(v[2]);
        col.add(color);
      }
    }

    /**
     * Surface of revolution around Y through profile points {@code (radius[i], y[i])}, split into
     * rows of at most {@link #ROW_HEIGHT} so shards and voxels get evenly sized triangles.
     */
    void lathe(int seg, float[] y, float[] radius, int color) {
      for (int i = 0; i + 1 < y.length; i++) {
        int rows = Math.max(1, (int) Math.ceil(Math.abs(y[i + 1] - y[i]) / ROW_HEIGHT));
        for (int r = 0; r < rows; r++) {
          float t0 = r / (float) rows;
          float t1 = (r + 1) / (float) rows;
          float ya = y[i] + (y[i + 1] - y[i]) * t0;
          float yb = y[i] + (y[i + 1] - y[i]) * t1;
          float ra = radius[i] + (radius[i + 1] - radius[i]) * t0;
          float rb = radius[i] + (radius[i + 1] - radius[i]) * t1;
          for (int s = 0; s < seg; s++) {
            double a0 = 2 * Math.PI * s / seg;
            double a1 = 2 * Math.PI * (s + 1) / seg;
            float[] p00 = ring(ra, ya, a0);
            float[] p01 = ring(ra, ya, a1);
            float[] p10 = ring(rb, yb, a0);
            float[] p11 = ring(rb, yb, a1);
            tri(p00, p10, p11, color);
            tri(p00, p11, p01, color);
          }
        }
      }
    }

    private static final float ROW_HEIGHT = 0.04f;

    void disc(float cx, float cy, float z, float radius, int seg, int color) {
      float[] center = {cx, cy, z};
      for (int s = 0; s < seg; s++) {
        double a0 = 2 * Math.PI * s / seg;
        double a1 = 2 * Math.PI * (s + 1) / seg;
        float[] p0 = {cx + radius * (float) Math.cos(a0), cy + radius * (float) Math.sin(a0), z};
        float[] p1 = {cx + radius * (float) Math.cos(a1), cy + radius * (float) Math.sin(a1), z};
        tri(center, p0, p1, color);
      }
    }

    /** A swept fin in the plane spanned by radial direction {@code (dx, dz)} and Y. */
    void fin(float dx, float dz, float r, int color) {
      float t = 0.025f; // half thickness
      float px = -dz;
      float pz = dx;
      float[][] outline = {
        {r, -0.95f}, {r + 0.38f, -1.12f}, {r + 0.38f, -0.85f}, {r, -0.45f},
      };
      float[][] front = new float[4][];
      float[][] back = new float[4][];
      for (int i = 0; i < 4; i++) {
        float rad = outline[i][0];
        float y = outline[i][1];
        front[i] = new float[] {dx * rad + px * t, y, dz * rad + pz * t};
        back[i] = new float[] {dx * rad - px * t, y, dz * rad - pz * t};
      }
      tri(front[0], front[1], front[2], color);
      tri(front[0], front[2], front[3], color);
      tri(back[0], back[2], back[1], color);
      tri(back[0], back[3], back[2], color);
      for (int i = 0; i < 4; i++) {
        int j = (i + 1) % 4;
        tri(front[i], back[i], back[j], color);
        tri(front[i], back[j], front[j], color);
      }
    }

    private static float[] ring(float radius, float y, double a) {
      return new float[] {radius * (float) Math.cos(a), y, radius * (float) Math.sin(a)};
    }

    MeshData toMesh() {
      float[] p = pos.toArray();
      float[] n = new float[p.length];
      MeshMath.faceNormals(p, n);
      return new MeshData(p, n, null, col.toArray(), null);
    }
  }
}
