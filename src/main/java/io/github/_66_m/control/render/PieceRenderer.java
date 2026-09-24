package io.github._66_m.control.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.github._66_m.control.render.mesh.PieceFrame;
import io.github._66_m.control.render.mesh.PieceMesh;
import io.github._66_m.control.render.mesh.TextureImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Draws a {@link PieceMesh} with one draw call: the static mesh carries a piece id per vertex and
 * every piece's matrix / color / UV remap comes from an {@code RGBA32F} data texture uploaded each
 * frame. Keeps the GPU copy of the last two meshes (shards ↔ globe switches stay cheap).
 */
final class PieceRenderer implements Disposable {

  static final String VERT_PATH = "shaders/pieces.vert";
  static final String FRAG_PATH = "shaders/pieces.frag";

  /** Pieces per data-texture row (5 texels each). */
  static final int PER_ROW = 256;

  private static final int TEXELS_PER_PIECE = 5;

  private final ShaderProgram shader;
  private final IntBuffer idBuf = BufferUtils.newIntBuffer(1);

  private final CachedMesh[] cache = new CachedMesh[2];

  private int dataTex;
  private int dataRows = -1;
  private FloatBuffer dataBuf;

  private final float[] ambient = {0.45f, 0.45f, 0.45f};
  private final float[] lightDir = {-0.4f, -0.7f, -0.6f};
  private final float[] lightColor = {0.75f, 0.75f, 0.75f};

  private static final class CachedMesh {
    PieceMesh source;
    Mesh mesh;
    int texture;
  }

  PieceRenderer() {
    boolean prevPedantic = ShaderProgram.pedantic;
    ShaderProgram.pedantic = false;
    String prevV = ShaderProgram.prependVertexCode;
    String prevF = ShaderProgram.prependFragmentCode;
    ShaderProgram.prependVertexCode = "#version 300 es\n";
    ShaderProgram.prependFragmentCode = "#version 300 es\n";
    try {
      shader = new ShaderProgram(Gdx.files.internal(VERT_PATH), Gdx.files.internal(FRAG_PATH));
    } finally {
      ShaderProgram.prependVertexCode = prevV;
      ShaderProgram.prependFragmentCode = prevF;
      ShaderProgram.pedantic = prevPedantic;
    }
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("pieces shader: " + shader.getLog());
    }
  }

  /** Draws {@code frame}'s pieces of {@code mesh}; caller has entered the World3D pass. */
  void draw(PieceMesh mesh, PieceFrame frame, Matrix4 projView) {
    if (mesh == null || frame == null || frame.count <= 0 || mesh.vertexCount() == 0) {
      return;
    }
    CachedMesh cached = meshFor(mesh);
    uploadData(frame, Math.min(frame.count, mesh.pieceCount));

    boolean cullWasEnabled = Gdx.gl.glIsEnabled(GL20.GL_CULL_FACE);
    Gdx.gl.glDisable(GL20.GL_CULL_FACE);
    Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    Gdx.gl.glDepthMask(true);
    shader.bind();
    shader.setUniformMatrix("u_projView", projView);
    shader.setUniformi("u_perRow", PER_ROW);
    shader.setUniform3fv("u_ambient", ambient, 0, 3);
    shader.setUniform3fv("u_lightDir", lightDir, 0, 3);
    shader.setUniform3fv("u_lightColor", lightColor, 0, 3);
    int mode =
        switch (frame.colorMode) {
          case PIECE -> 0;
          case VERTEX -> mesh.colors != null ? 1 : 0;
          case TEXTURE -> cached.texture != 0 ? 2 : (mesh.colors != null ? 1 : 0);
        };
    shader.setUniformi("u_colorMode", mode);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
    Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, dataTex);
    shader.setUniformi("u_pieces", 1);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, cached.texture);
    shader.setUniformi("u_texture", 0);
    cached.mesh.render(shader, GL20.GL_TRIANGLES);
    if (cullWasEnabled) {
      Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    }
  }

  private CachedMesh meshFor(PieceMesh mesh) {
    for (CachedMesh c : cache) {
      if (c != null && c.source == mesh) {
        return c;
      }
    }
    // Evict the older slot.
    if (cache[1] != null) {
      release(cache[1]);
    }
    cache[1] = cache[0];
    CachedMesh fresh = new CachedMesh();
    fresh.source = mesh;
    fresh.mesh = buildMesh(mesh);
    if (mesh.texture != null && mesh.uvs != null) {
      fresh.texture = uploadTexture(mesh.texture);
    }
    cache[0] = fresh;
    return fresh;
  }

  private static Mesh buildMesh(PieceMesh m) {
    int n = m.vertexCount();
    int stride = 3 + 3 + 2 + 4 + 1;
    float[] verts = new float[n * stride];
    for (int i = 0; i < n; i++) {
      int o = i * stride;
      verts[o] = m.positions[i * 3];
      verts[o + 1] = m.positions[i * 3 + 1];
      verts[o + 2] = m.positions[i * 3 + 2];
      verts[o + 3] = m.normals[i * 3];
      verts[o + 4] = m.normals[i * 3 + 1];
      verts[o + 5] = m.normals[i * 3 + 2];
      if (m.uvs != null) {
        verts[o + 6] = m.uvs[i * 2];
        verts[o + 7] = m.uvs[i * 2 + 1];
      }
      int c = m.colors != null ? m.colors[i] : 0xFFFFFFFF;
      verts[o + 8] = ((c >> 16) & 0xFF) / 255f;
      verts[o + 9] = ((c >> 8) & 0xFF) / 255f;
      verts[o + 10] = (c & 0xFF) / 255f;
      verts[o + 11] = 1f;
      verts[o + 12] = m.pieceOf[i];
    }
    Mesh mesh =
        new Mesh(
            true,
            n,
            0,
            new VertexAttributes(
                new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
                new VertexAttribute(
                    Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
                new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
                new VertexAttribute(Usage.Generic, 1, "a_piece")));
    mesh.setVertices(verts);
    return mesh;
  }

  private int uploadTexture(TextureImage image) {
    int tex = genTexture();
    ByteBuffer pixels = image.rgba().duplicate();
    pixels.position(0);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, tex);
    Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
    Gdx.gl.glTexImage2D(
        GL20.GL_TEXTURE_2D,
        0,
        GL20.GL_RGBA,
        image.width(),
        image.height(),
        0,
        GL20.GL_RGBA,
        GL20.GL_UNSIGNED_BYTE,
        pixels);
    Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 4);
    Gdx.gl.glGenerateMipmap(GL20.GL_TEXTURE_2D);
    Gdx.gl.glTexParameteri(
        GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR_MIPMAP_LINEAR);
    Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
    Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_REPEAT);
    Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
    return tex;
  }

  private void uploadData(PieceFrame frame, int pieces) {
    int rows = Math.max(1, (pieces + PER_ROW - 1) / PER_ROW);
    int width = PER_ROW * TEXELS_PER_PIECE;
    int floats = width * rows * 4;
    if (dataBuf == null || dataBuf.capacity() < floats) {
      dataBuf = BufferUtils.newFloatBuffer(floats);
    }
    dataBuf.clear();
    dataBuf.put(frame.data, 0, Math.min(frame.data.length, pieces * PieceFrame.FLOATS_PER_PIECE));
    while (dataBuf.position() < floats) {
      dataBuf.put(0f);
    }
    dataBuf.flip();
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
    if (dataTex == 0) {
      dataTex = genTexture();
    }
    Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, dataTex);
    if (rows != dataRows) {
      Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_NEAREST);
      Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_NEAREST);
      Gdx.gl.glTexImage2D(
          GL20.GL_TEXTURE_2D,
          0,
          GL30.GL_RGBA32F,
          width,
          rows,
          0,
          GL20.GL_RGBA,
          GL20.GL_FLOAT,
          dataBuf);
      dataRows = rows;
    } else {
      Gdx.gl.glTexSubImage2D(
          GL20.GL_TEXTURE_2D, 0, 0, 0, width, rows, GL20.GL_RGBA, GL20.GL_FLOAT, dataBuf);
    }
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
  }

  private int genTexture() {
    idBuf.clear();
    Gdx.gl.glGenTextures(1, idBuf);
    return idBuf.get(0);
  }

  private static void release(CachedMesh c) {
    c.mesh.dispose();
    if (c.texture != 0) {
      Gdx.gl.glDeleteTexture(c.texture);
    }
  }

  @Override
  public void dispose() {
    for (int i = 0; i < cache.length; i++) {
      if (cache[i] != null) {
        release(cache[i]);
        cache[i] = null;
      }
    }
    if (dataTex != 0) {
      Gdx.gl.glDeleteTexture(dataTex);
      dataTex = 0;
    }
    shader.dispose();
  }
}
