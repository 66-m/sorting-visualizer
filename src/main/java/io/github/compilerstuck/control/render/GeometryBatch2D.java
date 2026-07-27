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
 * lines/ellipses. Hairlines use {@code GL_LINES}; widths {@code > 1} use triangle quads (portable —
 * {@code glLineWidth} is ignored on many GL cores).
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

  /** Widths at or below this use {@code GL_LINES}; above use thick quads. */
  static final float HAIRLINE_MAX_PX = 1f;

  private final ShaderProgram shader;
  private Mesh quadMesh;
  private float[] quadVerts;
  private int maxQuads;

  private Mesh lineMesh;
  private float[] lineVerts;
  private int maxLineSegments;

  private final float[] tmpRgba = new float[4];

  public GeometryBatch2D() {
    boolean prevPedantic = ShaderProgram.pedantic;
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
      ShaderProgram.pedantic = prevPedantic;
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
   * Draws 2D line segments. {@code xyxy} = [x1,y1,x2,y2] × count. Hairlines use {@code GL_LINES};
   * thicker strokes use triangle quads.
   *
   * @return true if a draw was issued
   */
  public boolean drawLines(
      float[] xyxy, int[] argb, int count, float lineWidthPx, Matrix4 projView) {
    if (xyxy == null || argb == null || count <= 0 || projView == null) {
      return false;
    }
    float width = Math.max(0.1f, lineWidthPx);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shader.bind();
    shader.setUniformMatrix("u_projView", projView);
    shader.setUniformf("u_circleMask", 0f);
    if (width <= HAIRLINE_MAX_PX) {
      ensureLineCapacity(count);
      int floats = packLineSegments(xyxy, argb, count, lineVerts, tmpRgba);
      lineMesh.setVertices(lineVerts, 0, floats);
      lineMesh.render(shader, GL20.GL_LINES, 0, count * 2);
    } else {
      ensureQuadCapacity(count);
      int floats = packThickLineQuads(xyxy, argb, count, width * 0.5f, quadVerts, tmpRgba);
      quadMesh.setVertices(quadVerts, 0, floats);
      quadMesh.render(shader, GL20.GL_TRIANGLES, 0, count * 6);
    }
    return true;
  }

  /**
   * Draws stroked ellipses as {@link #ELLIPSE_SEGMENTS}-segment polylines. {@code xywh} =
   * [cx,cy,w,h] × count. Thick strokes expand each segment to a quad.
   *
   * @return true if a draw was issued
   */
  public boolean drawEllipsesStroke(
      float[] xywh, int[] argb, int count, float lineWidthPx, Matrix4 projView) {
    if (xywh == null || argb == null || count <= 0 || projView == null) {
      return false;
    }
    float width = Math.max(0.1f, lineWidthPx);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shader.bind();
    shader.setUniformMatrix("u_projView", projView);
    shader.setUniformf("u_circleMask", 0f);
    if (width <= HAIRLINE_MAX_PX) {
      int segments = count * ELLIPSE_SEGMENTS;
      ensureLineCapacity(segments);
      int floats = packEllipseLines(xywh, argb, count, lineVerts, tmpRgba);
      lineMesh.setVertices(lineVerts, 0, floats);
      lineMesh.render(shader, GL20.GL_LINES, 0, segments * 2);
    } else {
      int quads = count * ELLIPSE_SEGMENTS;
      ensureQuadCapacity(quads);
      int floats = packEllipseThickQuads(xywh, argb, count, width * 0.5f, quadVerts, tmpRgba);
      quadMesh.setVertices(quadVerts, 0, floats);
      quadMesh.render(shader, GL20.GL_TRIANGLES, 0, quads * 6);
    }
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
   * Packs 2D hairline segments as {@code GL_LINES}. Package-visible for tests.
   *
   * @return number of floats written
   */
  static int packLineSegments(float[] xyxy, int[] argb, int count, float[] out, float[] tmpRgba) {
    int o = 0;
    for (int i = 0; i < count; i++) {
      int s = i * 4;
      InstanceTransform.unpackArgb(argb[i], tmpRgba, 0);
      o = putVert(out, o, xyxy[s], xyxy[s + 1], tmpRgba, 0f, 0f);
      o = putVert(out, o, xyxy[s + 2], xyxy[s + 3], tmpRgba, 0f, 0f);
    }
    return o;
  }

  /**
   * Packs thick 2D segments as two triangles each, expanded by {@code halfWidthPx} along the
   * perpendicular. Degenerate (zero-length) segments become axis-aligned squares. Package-visible
   * for tests.
   *
   * @return number of floats written
   */
  static int packThickLineQuads(
      float[] xyxy, int[] argb, int count, float halfWidthPx, float[] out, float[] tmpRgba) {
    int o = 0;
    float hw = Math.max(0.05f, halfWidthPx);
    for (int i = 0; i < count; i++) {
      int s = i * 4;
      float x0 = xyxy[s];
      float y0 = xyxy[s + 1];
      float x1 = xyxy[s + 2];
      float y1 = xyxy[s + 3];
      InstanceTransform.unpackArgb(argb[i], tmpRgba, 0);
      o = putThickSegment(out, o, x0, y0, x1, y1, hw, tmpRgba);
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

  /**
   * Packs ellipse outlines as thick quads ({@link #ELLIPSE_SEGMENTS} per ellipse). Package-visible
   * for tests.
   *
   * @return number of floats written
   */
  static int packEllipseThickQuads(
      float[] xywh, int[] argb, int count, float halfWidthPx, float[] out, float[] tmpRgba) {
    int o = 0;
    float strokeHw = Math.max(0.05f, halfWidthPx);
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
        float x0 = cx + (float) Math.cos(a0) * hw;
        float y0 = cy + (float) Math.sin(a0) * hh;
        float x1 = cx + (float) Math.cos(a1) * hw;
        float y1 = cy + (float) Math.sin(a1) * hh;
        o = putThickSegment(out, o, x0, y0, x1, y1, strokeHw, tmpRgba);
      }
    }
    return o;
  }

  private static int putThickSegment(
      float[] out, int o, float x0, float y0, float x1, float y1, float halfWidth, float[] rgba) {
    float dx = x1 - x0;
    float dy = y1 - y0;
    float len = (float) Math.sqrt(dx * dx + dy * dy);
    float nx;
    float ny;
    if (len < 1e-6f) {
      nx = halfWidth;
      ny = 0f;
    } else {
      float inv = halfWidth / len;
      nx = -dy * inv;
      ny = dx * inv;
    }
    float ax = x0 + nx;
    float ay = y0 + ny;
    float bx = x0 - nx;
    float by = y0 - ny;
    float cx = x1 + nx;
    float cy = y1 + ny;
    float dxv = x1 - nx;
    float dyv = y1 - ny;
    // Triangle 1: A, B, C
    o = putVert(out, o, ax, ay, rgba, 0f, 0f);
    o = putVert(out, o, bx, by, rgba, 0f, 0f);
    o = putVert(out, o, cx, cy, rgba, 0f, 0f);
    // Triangle 2: B, D, C
    o = putVert(out, o, bx, by, rgba, 0f, 0f);
    o = putVert(out, o, dxv, dyv, rgba, 0f, 0f);
    o = putVert(out, o, cx, cy, rgba, 0f, 0f);
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
