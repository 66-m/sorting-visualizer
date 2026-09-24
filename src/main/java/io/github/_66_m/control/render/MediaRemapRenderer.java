package io.github._66_m.control.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.github._66_m.control.render.media.MediaRemap;
import io.github._66_m.control.render.media.PixelFrame;
import io.github._66_m.control.render.media.RemapLayout;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Draws a {@link MediaRemap}: uploads the frame into a streaming RGBA texture (only when its
 * revision changes) and the piece indices into a 2D index texture (only when the index revision
 * changes), then remaps columns / rows / grid tiles in {@code media_remap.frag}.
 */
final class MediaRemapRenderer implements Disposable {

  static final String VERT_PATH = "shaders/media_remap.vert";
  static final String FRAG_PATH = "shaders/media_remap.frag";

  /** Index texture width; lengths above it wrap into more rows. */
  static final int INDEX_WIDTH = 1024;

  private final ShaderProgram shader;
  private final Mesh quad;
  private final IntBuffer idBuf = BufferUtils.newIntBuffer(1);

  private int sourceTex;
  private int sourceW = -1;
  private int sourceH = -1;
  private PixelFrame lastFrame;
  private long lastFrameRevision = Long.MIN_VALUE;

  private int indexTex;
  private int indexRows = -1;
  private ByteBuffer indexBytes;
  private int lastIndexLength = -1;
  private long lastIndexRevision = Long.MIN_VALUE;

  MediaRemapRenderer() {
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
      throw new GdxRuntimeException("media_remap: " + shader.getLog());
    }
    quad =
        new Mesh(
            true,
            4,
            0,
            new VertexAttributes(
                new VertexAttribute(
                    VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE)));
    quad.setVertices(new float[] {0, 0, 1, 0, 0, 1, 1, 1});
  }

  /**
   * Draws {@code remap} into the current (full-window) viewport.
   *
   * @return number of texture uploads this call made (0–2), for frame stats
   */
  int draw(MediaRemap remap, int screenW, int screenH) {
    PixelFrame frame = remap.frame;
    if (frame == null || remap.indices == null || remap.length <= 0) {
      return 0;
    }
    int uploads = 0;
    if (uploadFrameIfNeeded(frame)) {
      uploads++;
    }
    if (uploadIndicesIfNeeded(remap)) {
      uploads++;
    }

    float[] rect = targetRect(frame.width(), frame.height(), screenW, screenH, remap.contain);
    Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    Gdx.gl.glDisable(GL20.GL_BLEND);
    shader.bind();
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, sourceTex);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
    Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, indexTex);
    shader.setUniformi("u_source", 0);
    shader.setUniformi("u_index", 1);
    shader.setUniformi("u_length", remap.length);
    shader.setUniformi("u_mode", modeOf(remap.layout));
    shader.setUniformi("u_cols", Math.max(1, remap.cols));
    shader.setUniformi("u_rows", Math.max(1, remap.rows));
    shader.setUniformi("u_indexWidth", INDEX_WIDTH);
    shader.setUniformf("u_highlightStrength", clamp01(remap.highlightStrength));
    shader.setUniformi("u_gridLines", remap.gridLines ? 1 : 0);
    shader.setUniformf(
        "u_rectPx", (rect[2] - rect[0]) * 0.5f * screenW, (rect[3] - rect[1]) * 0.5f * screenH);
    shader.setUniformf("u_rect", rect[0], rect[1], rect[2], rect[3]);
    quad.render(shader, GL20.GL_TRIANGLE_STRIP);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    return uploads;
  }

  /** NDC rect {x0, y0, x1, y1} for the frame: letterboxed when {@code contain}, else full. */
  static float[] targetRect(int frameW, int frameH, int screenW, int screenH, boolean contain) {
    if (!contain || frameW <= 0 || frameH <= 0 || screenW <= 0 || screenH <= 0) {
      return new float[] {-1f, -1f, 1f, 1f};
    }
    double scale = Math.min(screenW / (double) frameW, screenH / (double) frameH);
    float halfW = (float) (frameW * scale / screenW);
    float halfH = (float) (frameH * scale / screenH);
    return new float[] {-halfW, -halfH, halfW, halfH};
  }

  static int modeOf(RemapLayout layout) {
    if (layout == null) {
      return 0;
    }
    return switch (layout) {
      case COLUMNS -> 0;
      case ROWS -> 1;
      case GRID -> 2;
    };
  }

  private boolean uploadFrameIfNeeded(PixelFrame frame) {
    if (frame == lastFrame && frame.revision() == lastFrameRevision && sourceTex != 0) {
      return false;
    }
    int w = frame.width();
    int h = frame.height();
    ByteBuffer pixels = frame.rgba().duplicate();
    pixels.position(0);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
    if (sourceTex == 0 || w != sourceW || h != sourceH) {
      if (sourceTex == 0) {
        sourceTex = genTexture();
      }
      Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, sourceTex);
      setFilter(GL20.GL_LINEAR);
      Gdx.gl.glTexImage2D(
          GL20.GL_TEXTURE_2D,
          0,
          GL20.GL_RGBA,
          w,
          h,
          0,
          GL20.GL_RGBA,
          GL20.GL_UNSIGNED_BYTE,
          pixels);
      sourceW = w;
      sourceH = h;
    } else {
      Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, sourceTex);
      Gdx.gl.glTexSubImage2D(
          GL20.GL_TEXTURE_2D, 0, 0, 0, w, h, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixels);
    }
    Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 4);
    lastFrame = frame;
    lastFrameRevision = frame.revision();
    return true;
  }

  private boolean uploadIndicesIfNeeded(MediaRemap remap) {
    int length = remap.length;
    if (indexTex != 0 && length == lastIndexLength && remap.indexRevision == lastIndexRevision) {
      return false;
    }
    int rows = (length + INDEX_WIDTH - 1) / INDEX_WIDTH;
    int bytes = INDEX_WIDTH * rows * 4;
    if (indexBytes == null || indexBytes.capacity() < bytes) {
      indexBytes = BufferUtils.newByteBuffer(bytes);
    }
    indexBytes.clear();
    for (int i = 0; i < INDEX_WIDTH * rows; i++) {
      if (i < length) {
        int idx = Math.max(0, Math.min(length - 1, remap.indices[i]));
        boolean hl = remap.highlight != null && i < remap.highlight.length && remap.highlight[i];
        indexBytes.put((byte) (idx >> 16));
        indexBytes.put((byte) (idx >> 8));
        indexBytes.put((byte) idx);
        indexBytes.put((byte) (hl ? 0 : 0xFF));
      } else {
        indexBytes.put((byte) 0).put((byte) 0).put((byte) 0).put((byte) 0xFF);
      }
    }
    indexBytes.flip();
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
    Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
    if (indexTex == 0) {
      indexTex = genTexture();
    }
    Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, indexTex);
    if (rows != indexRows) {
      setFilter(GL20.GL_NEAREST);
      Gdx.gl.glTexImage2D(
          GL20.GL_TEXTURE_2D,
          0,
          GL20.GL_RGBA,
          INDEX_WIDTH,
          rows,
          0,
          GL20.GL_RGBA,
          GL20.GL_UNSIGNED_BYTE,
          indexBytes);
      indexRows = rows;
    } else {
      Gdx.gl.glTexSubImage2D(
          GL20.GL_TEXTURE_2D,
          0,
          0,
          0,
          INDEX_WIDTH,
          rows,
          GL20.GL_RGBA,
          GL20.GL_UNSIGNED_BYTE,
          indexBytes);
    }
    Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 4);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    lastIndexLength = length;
    lastIndexRevision = remap.indexRevision;
    return true;
  }

  private int genTexture() {
    idBuf.clear();
    Gdx.gl.glGenTextures(1, idBuf);
    return idBuf.get(0);
  }

  private static void setFilter(int filter) {
    Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, filter);
    Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, filter);
    Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
    Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
  }

  private static float clamp01(float v) {
    return Math.max(0f, Math.min(1f, v));
  }

  @Override
  public void dispose() {
    if (sourceTex != 0) {
      Gdx.gl.glDeleteTexture(sourceTex);
      sourceTex = 0;
    }
    if (indexTex != 0) {
      Gdx.gl.glDeleteTexture(indexTex);
      indexTex = 0;
    }
    quad.dispose();
    shader.dispose();
  }
}
