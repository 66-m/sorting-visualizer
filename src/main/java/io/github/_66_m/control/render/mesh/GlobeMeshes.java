package io.github._66_m.control.render.mesh;

import java.util.Arrays;

/**
 * Unit-sphere {@link PieceMesh}es for the Globe: latitude × longitude tiles or pole-to-pole
 * longitude wedges. UVs follow the equirectangular convention (u = longitude 0…1 from −180°, v = 0
 * at the south pole); each piece records its home UV rectangle so a visualization can show another
 * piece's texture in it. Longitude 0 faces the viewer (+Z), east is +X.
 */
public final class GlobeMeshes {

  private GlobeMeshes() {}

  /** Tile rows / columns for an equal-angle grid of about {@code n} tiles (2:1 aspect). */
  public static int[] equirectShape(int n) {
    int rows = Math.max(1, (int) Math.round(Math.sqrt(n / 2.0)));
    int cols = Math.max(1, (n + rows - 1) / rows);
    return new int[] {rows, cols};
  }

  /**
   * Columns per row (north to south) for exactly {@code n} roughly equal-area tiles: rows get
   * columns in proportion to the cosine of their mid latitude, at least one each.
   */
  public static int[] equalAreaColumns(int n) {
    int rows = Math.max(1, (int) Math.round(Math.sqrt(Math.PI * n / 4.0)));
    rows = Math.min(rows, n);
    double[] weight = new double[rows];
    double total = 0;
    for (int r = 0; r < rows; r++) {
      double mid = Math.PI / 2 - (r + 0.5) * Math.PI / rows;
      weight[r] = Math.cos(mid);
      total += weight[r];
    }
    int[] cols = new int[rows];
    double[] remainder = new double[rows];
    int assigned = 0;
    for (int r = 0; r < rows; r++) {
      double exact = n * weight[r] / total;
      cols[r] = Math.max(1, (int) Math.floor(exact));
      remainder[r] = exact - Math.floor(exact);
      assigned += cols[r];
    }
    // Largest remainders get the leftover tiles; trim from the widest rows if we overshot.
    Integer[] byRemainder = new Integer[rows];
    for (int r = 0; r < rows; r++) {
      byRemainder[r] = r;
    }
    Arrays.sort(byRemainder, (x, y) -> Double.compare(remainder[y], remainder[x]));
    for (int k = 0; assigned < n; k = (k + 1) % rows) {
      cols[byRemainder[k]]++;
      assigned++;
    }
    while (assigned > n) {
      int widest = 0;
      for (int r = 1; r < rows; r++) {
        if (cols[r] > cols[widest]) {
          widest = r;
        }
      }
      cols[widest]--;
      assigned--;
    }
    return cols;
  }

  /**
   * Tiles for rows given as column counts (north to south). The last piece (index = tile count) is
   * a whole sphere used as the planet core.
   */
  public static PieceMesh tiles(int[] colsPerRow) {
    int rows = colsPerRow.length;
    int pieces = Arrays.stream(colsPerRow).sum();
    Builder b = new Builder(pieces + 1);
    b.patch(pieces, 0f, 0f, 1f, 1f, 48, 24);
    int piece = 0;
    for (int r = 0; r < rows; r++) {
      float v1 = 1f - r / (float) rows;
      float v0 = 1f - (r + 1) / (float) rows;
      int cols = colsPerRow[r];
      int latSteps = Math.max(1, Math.min(16, Math.round(48f / rows)));
      int lonSteps = Math.max(1, Math.min(16, Math.round(96f / cols)));
      for (int c = 0; c < cols; c++) {
        float u0 = c / (float) cols;
        float u1 = (c + 1) / (float) cols;
        b.patch(piece++, u0, v0, u1, v1, lonSteps, latSteps);
      }
    }
    return b.build();
  }

  /** {@code wedges} pole-to-pole longitude wedges, plus the core sphere as the last piece. */
  public static PieceMesh wedges(int wedges) {
    int w = Math.max(1, wedges);
    Builder b = new Builder(w + 1);
    b.patch(w, 0f, 0f, 1f, 1f, 48, 24);
    int latSteps = w > 256 ? 24 : 48;
    int lonSteps = Math.max(1, Math.min(16, Math.round(128f / w)));
    for (int k = 0; k < w; k++) {
      b.patch(k, k / (float) w, 0f, (k + 1) / (float) w, 1f, lonSteps, latSteps);
    }
    return b.build();
  }

  /** Unit-sphere point for equirectangular {@code (u, v)} into {@code out[o..o+2]}. */
  static void spherePoint(float u, float v, float[] out, int o) {
    double lon = (u - 0.5) * 2 * Math.PI;
    double lat = (v - 0.5) * Math.PI;
    double cl = Math.cos(lat);
    out[o] = (float) (cl * Math.sin(lon));
    out[o + 1] = (float) Math.sin(lat);
    out[o + 2] = (float) (cl * Math.cos(lon));
  }

  private static final class Builder {
    private final ObjLoader.FloatList pos = new ObjLoader.FloatList();
    private final ObjLoader.FloatList uv = new ObjLoader.FloatList();
    private final ObjLoader.IntList pieceOf = new ObjLoader.IntList();
    private final float[] centers;
    private final float[] rects;
    private final int pieces;
    private final float[] a = new float[3];

    Builder(int pieces) {
      this.pieces = pieces;
      this.centers = new float[pieces * 3];
      this.rects = new float[pieces * 4];
    }

    void patch(int piece, float u0, float v0, float u1, float v1, int lonSteps, int latSteps) {
      rects[piece * 4] = u0;
      rects[piece * 4 + 1] = v0;
      rects[piece * 4 + 2] = u1;
      rects[piece * 4 + 3] = v1;
      spherePoint((u0 + u1) / 2f, (v0 + v1) / 2f, centers, piece * 3);
      for (int j = 0; j < latSteps; j++) {
        float va = v0 + (v1 - v0) * j / latSteps;
        float vb = v0 + (v1 - v0) * (j + 1) / latSteps;
        for (int i = 0; i < lonSteps; i++) {
          float ua = u0 + (u1 - u0) * i / lonSteps;
          float ub = u0 + (u1 - u0) * (i + 1) / lonSteps;
          vertex(piece, ua, va);
          vertex(piece, ub, va);
          vertex(piece, ub, vb);
          vertex(piece, ua, va);
          vertex(piece, ub, vb);
          vertex(piece, ua, vb);
        }
      }
    }

    private void vertex(int piece, float u, float v) {
      spherePoint(u, v, a, 0);
      pos.add(a[0]);
      pos.add(a[1]);
      pos.add(a[2]);
      uv.add(u);
      uv.add(v);
      pieceOf.add(piece);
    }

    PieceMesh build() {
      float[] p = pos.toArray();
      // On a unit sphere the normal is the position.
      return new PieceMesh(
          p,
          p.clone(),
          uv.toArray(),
          null,
          pieceOf.toArray(),
          pieces,
          centers,
          Partitions.randomAxes(pieces),
          rects,
          null);
    }
  }
}
