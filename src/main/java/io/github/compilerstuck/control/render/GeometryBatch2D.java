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

/**
 * World2D colored geometry without ShapeRenderer tessellation: filled circle/rect quads and stroked
 * ellipse outlines as {@code GL_LINES}.
 */
public final class GeometryBatch2D implements Disposable {
  private static final String VERT_PATH = "shaders/geo2d.vert";
  private static final String FRAG_PATH = "shaders/geo2d.frag";

  /** Floats per vertex: xy + rgba + uv. */
  static final int FLOATS_PER_VERT = 8;

  private static final int INITIAL_QUADS = 4096;

  /** Segments per ellipse outline — high enough that large rings read as circles, not polygons. */
  public static final int ELLIPSE_SEGMENTS = 64;

  private static final int INITIAL_LINE_SEGMENTS = 4096;

  private final ShaderProgram shader;
  private Mesh quadMesh;
  private float[] quadVerts;
  private int maxQuads;

  private Mesh lineMesh;
  private float[] lineVerts;
  private int maxLineSegments;

  private final float[] tmpRgba = new float[4];

  public GeometryBatch2D() {
    ShaderProgram.pedantic = false;
    String prevVert = ShaderProgram.prependVertexCode;
    String prevFrag = ShaderProgram.prependFragmentCode;
    ShaderProgram.prependVertexCode =
        """
        #version 300 es
        """;
    ShaderProgram.prependFragmentCode =
        """
        #version 300 es
        """;
    try {
      shader = new ShaderProgram(Gdx.files.internal(VERT_PATH), Gdx.files.internal(FRAG_PATH));
    } finally {
      ShaderProgram.prependVertexCode = prevVert;
      ShaderProgram.prependFragmentCode = prevFrag;
    }
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("geo2d shader: " + shader.getLog());
    }
    maxQuads = INITIAL_QUADS;
    quadVerts = new float[maxQuads * 6 * FLOATS_PER_VERT];
    quadMesh = createQuadMesh(maxQuads);

    maxLineSegments = INITIAL_LINE_SEGMENTS;
    lineVerts = new float[maxLineSegments * 2 * FLOATS_PER_VERT];
    lineMesh = createLineMesh(maxLineSegments);
  }

  /**
   * Draws filled circles as axis-aligned quads with a unit-disk fragment mask. {@code xyd} =
   * [x,y,diameter] × count.
   *
   * @return true if a draw was issued
   */
  public boolean drawCircles(float[] xyd, int[] argb, int count, Matrix4 projView) {
    if (xyd == null || argb == null || count <= 0 || projView == null) {
      return false;
    }
    ensureQuadCapacity(count);
    int floats = packCircleQuads(xyd, argb, count, quadVerts, tmpRgba);
    quadMesh.setVertices(quadVerts, 0, floats);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shader.bind();
    shader.setUniformMatrix("u_projView", projView);
    shader.setUniformf("u_circleMask", 1f);
    quadMesh.render(shader, GL20.GL_TRIANGLES, 0, count * 6);
    return true;
  }

  /**
   * Draws filled axis-aligned rects. {@code xywh} = [x,y,w,h] × count.
   *
   * @return true if a draw was issued
   */
  public boolean drawRects(float[] xywh, int[] argb, int count, Matrix4 projView) {
    if (xywh == null || argb == null || count <= 0 || projView == null) {
      return false;
    }
    ensureQuadCapacity(count);
    int floats = packRectQuads(xywh, argb, count, quadVerts, tmpRgba);
    quadMesh.setVertices(quadVerts, 0, floats);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shader.bind();
    shader.setUniformMatrix("u_projView", projView);
    shader.setUniformf("u_circleMask", 0f);
    quadMesh.render(shader, GL20.GL_TRIANGLES, 0, count * 6);
    return true;
  }

  /**
   * Draws stroked ellipses as {@link #ELLIPSE_SEGMENTS}-segment polylines. {@code xywh} =
   * [cx,cy,w,h] × count.
   *
   * @return true if a draw was issued
   */
  public boolean drawEllipsesStroke(
      float[] xywh, int[] argb, int count, float lineWidthPx, Matrix4 projView) {
    if (xywh == null || argb == null || count <= 0 || projView == null) {
      return false;
    }
    int segments = count * ELLIPSE_SEGMENTS;
    ensureLineCapacity(segments);
    int floats = packEllipseLines(xywh, argb, count, lineVerts, tmpRgba);
    lineMesh.setVertices(lineVerts, 0, floats);
    Gdx.gl.glLineWidth(Math.max(0.1f, lineWidthPx));
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shader.bind();
    shader.setUniformMatrix("u_projView", projView);
    shader.setUniformf("u_circleMask", 0f);
    lineMesh.render(shader, GL20.GL_LINES, 0, segments * 2);
    return true;
  }

  /**
   * Packs circle quads as two triangles (6 verts) each with local UV in {@code [-1,1]}.
   * Package-visible for tests.
   *
   * @return number of floats written
   */
  static int packCircleQuads(float[] xyd, int[] argb, int count, float[] out, float[] tmpRgba) {
    int o = 0;
    for (int i = 0; i < count; i++) {
      int s = i * 3;
      float cx = xyd[s];
      float cy = xyd[s + 1];
      float r = xyd[s + 2] * 0.5f;
      InstanceTransform.unpackArgb(argb[i], tmpRgba, 0);
      // Triangle 1: BL, BR, TL
      o = putVert(out, o, cx - r, cy - r, tmpRgba, -1f, -1f);
      o = putVert(out, o, cx + r, cy - r, tmpRgba, 1f, -1f);
      o = putVert(out, o, cx - r, cy + r, tmpRgba, -1f, 1f);
      // Triangle 2: BR, TR, TL
      o = putVert(out, o, cx + r, cy - r, tmpRgba, 1f, -1f);
      o = putVert(out, o, cx + r, cy + r, tmpRgba, 1f, 1f);
      o = putVert(out, o, cx - r, cy + r, tmpRgba, -1f, 1f);
    }
    return o;
  }

  /**
   * Packs rect quads as two triangles (6 verts) each. UV unused ({@code 0,0}). Package-visible for
   * tests.
   *
   * @return number of floats written
   */
  static int packRectQuads(float[] xywh, int[] argb, int count, float[] out, float[] tmpRgba) {
    int o = 0;
    for (int i = 0; i < count; i++) {
      int s = i * 4;
      float x = xywh[s];
      float y = xywh[s + 1];
      float w = xywh[s + 2];
      float h = xywh[s + 3];
      float x1 = x + w;
      float y1 = y + h;
      InstanceTransform.unpackArgb(argb[i], tmpRgba, 0);
      // Triangle 1: BL, BR, TL
      o = putVert(out, o, x, y, tmpRgba, 0f, 0f);
      o = putVert(out, o, x1, y, tmpRgba, 0f, 0f);
      o = putVert(out, o, x, y1, tmpRgba, 0f, 0f);
      // Triangle 2: BR, TR, TL
      o = putVert(out, o, x1, y, tmpRgba, 0f, 0f);
      o = putVert(out, o, x1, y1, tmpRgba, 0f, 0f);
      o = putVert(out, o, x, y1, tmpRgba, 0f, 0f);
    }
    return o;
  }

  /**
   * Packs ellipse outlines as GL_LINES segments. Package-visible for tests.
   *
   * @return number of floats written
   */
  static int packEllipseLines(float[] xywh, int[] argb, int count, float[] out, float[] tmpRgba) {
    int o = 0;
    for (int i = 0; i < count; i++) {
      int s = i * 4;
      float cx = xywh[s];
      float cy = xywh[s + 1];
      float hw = xywh[s + 2] * 0.5f;
      float hh = xywh[s + 3] * 0.5f;
      InstanceTransform.unpackArgb(argb[i], tmpRgba, 0);
      for (int seg = 0; seg < ELLIPSE_SEGMENTS; seg++) {
        float a0 = (float) (Math.PI * 2.0 * seg / ELLIPSE_SEGMENTS);
        float a1 = (float) (Math.PI * 2.0 * (seg + 1) / ELLIPSE_SEGMENTS);
        o =
            putVert(
                out,
                o,
                cx + (float) Math.cos(a0) * hw,
                cy + (float) Math.sin(a0) * hh,
                tmpRgba,
                0f,
                0f);
        o =
            putVert(
                out,
                o,
                cx + (float) Math.cos(a1) * hw,
                cy + (float) Math.sin(a1) * hh,
                tmpRgba,
                0f,
                0f);
      }
    }
    return o;
  }

  private static int putVert(float[] out, int o, float x, float y, float[] rgba, float u, float v) {
    out[o++] = x;
    out[o++] = y;
    out[o++] = rgba[0];
    out[o++] = rgba[1];
    out[o++] = rgba[2];
    out[o++] = rgba[3];
    out[o++] = u;
    out[o++] = v;
    return o;
  }

  private void ensureQuadCapacity(int quads) {
    if (quads <= maxQuads) {
      return;
    }
    int next = maxQuads;
    while (next < quads) {
      next *= 2;
    }
    maxQuads = next;
    quadVerts = new float[maxQuads * 6 * FLOATS_PER_VERT];
    quadMesh.dispose();
    quadMesh = createQuadMesh(maxQuads);
  }

  private void ensureLineCapacity(int segments) {
    if (segments <= maxLineSegments) {
      return;
    }
    int next = maxLineSegments;
    while (next < segments) {
      next *= 2;
    }
    maxLineSegments = next;
    lineVerts = new float[maxLineSegments * 2 * FLOATS_PER_VERT];
    lineMesh.dispose();
    lineMesh = createLineMesh(maxLineSegments);
  }

  private static Mesh createQuadMesh(int maxQuads) {
    return new Mesh(
        false,
        maxQuads * 6,
        0,
        new VertexAttributes(
            new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"),
            new VertexAttribute(Usage.TextureCoordinates, 2, "a_uv")));
  }

  private static Mesh createLineMesh(int maxSegments) {
    return new Mesh(
        false,
        maxSegments * 2,
        0,
        new VertexAttributes(
            new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"),
            new VertexAttribute(Usage.TextureCoordinates, 2, "a_uv")));
  }

  @Override
  public void dispose() {
    if (quadMesh != null) {
      quadMesh.dispose();
      quadMesh = null;
    }
    if (lineMesh != null) {
      lineMesh.dispose();
      lineMesh = null;
    }
    if (shader != null) {
      shader.dispose();
    }
  }
}
