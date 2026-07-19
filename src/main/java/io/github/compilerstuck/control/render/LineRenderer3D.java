package io.github.compilerstuck.control.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.github.compilerstuck.control.config.SettingsDefaults;

/**
 * World-space 3D line segments as a single {@link Mesh} {@code GL_LINES} draw. Does not use
 * ShapeRenderer or ModelBatch.
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
  private int maxSegments;
  private final float[] tmpRgba = new float[4];

  public LineRenderer3D() {
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
    }
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("line3d shader: " + shader.getLog());
    }
    maxSegments = INITIAL_SEGMENTS;
    verts = new float[maxSegments * 2 * FLOATS_PER_VERT];
    mesh = createMesh(maxSegments);
  }

  /**
   * Packs world-space segments and draws once.
   *
   * @param xyzxyz world segment endpoints [x1,y1,z1,x2,y2,z2] × count
   * @return true if a draw was issued
   */
  public boolean draw(float[] xyzxyz, int[] argb, int count, float lineWidthPx, Matrix4 projView) {
    if (xyzxyz == null || argb == null || count <= 0) {
      return false;
    }
    ensureCapacity(count);
    int floatCount = packSegments(xyzxyz, argb, count, verts, tmpRgba);
    mesh.setVertices(verts, 0, floatCount);
    Gdx.gl.glLineWidth(Math.max(0.1f, lineWidthPx));
    shader.bind();
    shader.setUniformMatrix("u_projView", projView);
    mesh.render(shader, GL20.GL_LINES, 0, count * 2);
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

  private void ensureCapacity(int segments) {
    if (segments <= maxSegments) {
      return;
    }
    int next = maxSegments;
    while (next < segments) {
      next *= 2;
    }
    maxSegments = next;
    verts = new float[maxSegments * 2 * FLOATS_PER_VERT];
    mesh.dispose();
    mesh = createMesh(maxSegments);
  }

  private static Mesh createMesh(int maxSegments) {
    return new Mesh(
        false,
        maxSegments * 2,
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
