package io.github.compilerstuck.control.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.github.compilerstuck.control.config.SettingsDefaults;

/**
 * World-space 3D line segments as a single {@link Mesh} draw. Hairlines use {@code GL_LINES};
 * widths {@code > 1} use camera-facing quads (portable — {@code glLineWidth} is ignored on many GL
 * cores). Does not use ShapeRenderer or ModelBatch.
 */
public final class LineRenderer3D implements Disposable {
  private static final String VERT_PATH = "shaders/line3d.vert";
  private static final String FRAG_PATH = "shaders/line3d.frag";

  /** Floats per vertex: xyz + rgba. */
  private static final int FLOATS_PER_VERT = 7;

  /** Cover max array size so first large-N line frame does not rebuild the mesh. */
  private static final int INITIAL_SEGMENTS = SettingsDefaults.ARRAY_SIZE_MAX;

  private final ShaderProgram shader;
  private Mesh mesh;
  private float[] verts;
  private int maxVerts;
  private final float[] tmpRgba = new float[4];
  private final Vector3 tmpDir = new Vector3();
  private final Vector3 tmpSide = new Vector3();
  private final Vector3 tmpToCam = new Vector3();
  private final Vector3 tmpAlt = new Vector3();

  public LineRenderer3D() {
    boolean prevPedantic = ShaderProgram.pedantic;
    ShaderProgram.pedantic = false;
    String prevVert = ShaderProgram.prependVertexCode;
    String prevFrag = ShaderProgram.prependFragmentCode;
    ShaderProgram.prependVertexCode = "#version 300 es\n";
    ShaderProgram.prependFragmentCode = "#version 300 es\n";
    try {
      shader = new ShaderProgram(Gdx.files.internal(VERT_PATH), Gdx.files.internal(FRAG_PATH));
    } finally {
      ShaderProgram.prependVertexCode = prevVert;
      ShaderProgram.prependFragmentCode = prevFrag;
      ShaderProgram.pedantic = prevPedantic;
    }
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("line3d shader: " + shader.getLog());
    }
    // Size for hairlines (2 verts/seg); thick path grows to 6 verts/seg on demand.
    maxVerts = INITIAL_SEGMENTS * 2;
    verts = new float[maxVerts * FLOATS_PER_VERT];
    mesh = createMesh(maxVerts);
  }

  /**
   * Packs world-space segments and draws once.
   *
   * @param xyzxyz world segment endpoints [x1,y1,z1,x2,y2,z2] × count
   * @param lineWidthPx stroke width in screen pixels (≤1 → hairline)
   * @param camPos camera world position (used for thick camera-facing quads)
   * @param worldUnitsPerPixel converts pixel width to world half-extent
   * @return true if a draw was issued
   */
  public boolean draw(
      float[] xyzxyz,
      int[] argb,
      int count,
      float lineWidthPx,
      Matrix4 projView,
      Vector3 camPos,
      float worldUnitsPerPixel) {
    if (xyzxyz == null || argb == null || count <= 0 || projView == null) {
      return false;
    }
    float width = Math.max(0.1f, lineWidthPx);
    shader.bind();
    shader.setUniformMatrix("u_projView", projView);
    if (width <= GeometryBatch2D.HAIRLINE_MAX_PX || camPos == null || worldUnitsPerPixel <= 0f) {
      ensureVertCapacity(count * 2);
      int floatCount = packSegments(xyzxyz, argb, count, verts, tmpRgba);
      mesh.setVertices(verts, 0, floatCount);
      mesh.render(shader, GL20.GL_LINES, 0, count * 2);
    } else {
      ensureVertCapacity(count * 6);
      float halfWorld = Math.max(1e-4f, width * 0.5f * worldUnitsPerPixel);
      int floatCount =
          packThickSegments(
              xyzxyz, argb, count, halfWorld, camPos, verts, tmpRgba, tmpDir, tmpSide, tmpToCam,
              tmpAlt);
      mesh.setVertices(verts, 0, floatCount);
      mesh.render(shader, GL20.GL_TRIANGLES, 0, count * 6);
    }
    return true;
  }

  /**
   * Packs world-space line segments into interleaved xyz+rgba floats. Package visible for tests.
   *
   * @return number of floats written
   */
  static int packSegments(float[] xyzxyz, int[] argb, int count, float[] out, float[] tmpRgba) {
    int o = 0;
    for (int i = 0; i < count; i++) {
      int s = i * 6;
      InstanceTransform.unpackArgb(argb[i], tmpRgba, 0);
      out[o++] = xyzxyz[s];
      out[o++] = xyzxyz[s + 1];
      out[o++] = xyzxyz[s + 2];
      out[o++] = tmpRgba[0];
      out[o++] = tmpRgba[1];
      out[o++] = tmpRgba[2];
      out[o++] = tmpRgba[3];
      out[o++] = xyzxyz[s + 3];
      out[o++] = xyzxyz[s + 4];
      out[o++] = xyzxyz[s + 5];
      out[o++] = tmpRgba[0];
      out[o++] = tmpRgba[1];
      out[o++] = tmpRgba[2];
      out[o++] = tmpRgba[3];
    }
    return o;
  }

  /**
   * Packs camera-facing thick segments as two triangles each. Package-visible for tests.
   *
   * @return number of floats written
   */
  static int packThickSegments(
      float[] xyzxyz,
      int[] argb,
      int count,
      float halfWidthWorld,
      Vector3 camPos,
      float[] out,
      float[] tmpRgba,
      Vector3 tmpDir,
      Vector3 tmpSide,
      Vector3 tmpToCam,
      Vector3 tmpAlt) {
    int o = 0;
    for (int i = 0; i < count; i++) {
      int s = i * 6;
      float x0 = xyzxyz[s];
      float y0 = xyzxyz[s + 1];
      float z0 = xyzxyz[s + 2];
      float x1 = xyzxyz[s + 3];
      float y1 = xyzxyz[s + 4];
      float z1 = xyzxyz[s + 5];
      InstanceTransform.unpackArgb(argb[i], tmpRgba, 0);
      tmpDir.set(x1 - x0, y1 - y0, z1 - z0);
      float len2 = tmpDir.len2();
      if (len2 < 1e-12f) {
        tmpDir.set(1f, 0f, 0f);
      } else {
        tmpDir.scl(1f / (float) Math.sqrt(len2));
      }
      tmpToCam.set(camPos.x - x0, camPos.y - y0, camPos.z - z0);
      tmpSide.set(tmpDir).crs(tmpToCam);
      if (tmpSide.len2() < 1e-12f) {
        tmpAlt.set(Math.abs(tmpDir.y) < 0.9f ? 0f : 1f, Math.abs(tmpDir.y) < 0.9f ? 1f : 0f, 0f);
        tmpSide.set(tmpDir).crs(tmpAlt);
      }
      tmpSide.nor().scl(halfWidthWorld);
      float sx = tmpSide.x;
      float sy = tmpSide.y;
      float sz = tmpSide.z;
      // A = p0+side, B = p0-side, C = p1+side, D = p1-side
      o = putVert(out, o, x0 + sx, y0 + sy, z0 + sz, tmpRgba);
      o = putVert(out, o, x0 - sx, y0 - sy, z0 - sz, tmpRgba);
      o = putVert(out, o, x1 + sx, y1 + sy, z1 + sz, tmpRgba);
      o = putVert(out, o, x0 - sx, y0 - sy, z0 - sz, tmpRgba);
      o = putVert(out, o, x1 - sx, y1 - sy, z1 - sz, tmpRgba);
      o = putVert(out, o, x1 + sx, y1 + sy, z1 + sz, tmpRgba);
    }
    return o;
  }

  private static int putVert(float[] out, int o, float x, float y, float z, float[] rgba) {
    out[o++] = x;
    out[o++] = y;
    out[o++] = z;
    out[o++] = rgba[0];
    out[o++] = rgba[1];
    out[o++] = rgba[2];
    out[o++] = rgba[3];
    return o;
  }

  private void ensureVertCapacity(int vertsNeeded) {
    if (vertsNeeded <= maxVerts) {
      return;
    }
    int next = maxVerts;
    while (next < vertsNeeded) {
      next *= 2;
    }
    maxVerts = next;
    verts = new float[maxVerts * FLOATS_PER_VERT];
    mesh.dispose();
    mesh = createMesh(maxVerts);
  }

  private static Mesh createMesh(int maxVerts) {
    return new Mesh(
        false,
        maxVerts,
        0,
        new VertexAttributes(
            new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(Usage.ColorUnpacked, 4, "a_color")));
  }

  @Override
  public void dispose() {
    if (mesh != null) {
      mesh.dispose();
      mesh = null;
    }
    if (shader != null) {
      shader.dispose();
    }
  }
}
