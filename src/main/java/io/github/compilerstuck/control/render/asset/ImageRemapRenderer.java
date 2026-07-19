package io.github.compilerstuck.control.render.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Overlay fullscreen remap: samples {@code u_source} using a 1D index strip texture ({@code
 * u_index}). Horizontal mode remaps bands by Y; vertical by X.
 */
public final class ImageRemapRenderer implements Disposable {

  private final ShaderProgram shader;
  private final Mesh quad;
  private Texture indexTexture;
  private int indexLength = -1;
  private int lastIndexRevision = Integer.MIN_VALUE;
  private boolean lastCallUploaded;

  public ImageRemapRenderer() {
    ShaderProgram.pedantic = false;
    String prevV = ShaderProgram.prependVertexCode;
    String prevF = ShaderProgram.prependFragmentCode;
    ShaderProgram.prependVertexCode = "#version 300 es\n";
    ShaderProgram.prependFragmentCode = "#version 300 es\n";
    try {
      shader =
          new ShaderProgram(
              Gdx.files.internal("shaders/image_remap.vert"),
              Gdx.files.internal("shaders/image_remap.frag"));
    } finally {
      ShaderProgram.prependVertexCode = prevV;
      ShaderProgram.prependFragmentCode = prevF;
    }
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("image_remap: " + shader.getLog());
    }
    float[] verts = {
      -1, -1, 0, 1,
      1, -1, 1, 1,
      -1, 1, 0, 0,
      1, 1, 1, 0
    };
    quad =
        new Mesh(
            true,
            4,
            0,
            new VertexAttributes(
                new VertexAttribute(
                    VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(
                    VertexAttributes.Usage.TextureCoordinates,
                    2,
                    ShaderProgram.TEXCOORD_ATTRIBUTE + "0")));
    quad.setVertices(verts);
  }

  /**
   * Upload strip indices when {@code contentRevision} changes. Highlight strips use alpha=0 (shader
   * draws white).
   *
   * @return {@code true} if GPU index data was rewritten
   */
  public boolean uploadIndicesIfNeeded(
      int[] indices, boolean[] highlight, int length, int contentRevision) {
    lastCallUploaded = false;
    if (indices == null || length <= 0) {
      return false;
    }
    if (indexTexture != null && indexLength == length && lastIndexRevision == contentRevision) {
      return false;
    }
    if (indexTexture == null || indexLength != length) {
      if (indexTexture != null) {
        indexTexture.dispose();
      }
      indexTexture = new Texture(length, 1, Pixmap.Format.RGBA8888);
      indexTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
      indexLength = length;
    }
    Pixmap pm = new Pixmap(length, 1, Pixmap.Format.RGBA8888);
    for (int i = 0; i < length; i++) {
      if (highlight != null && highlight[i]) {
        pm.drawPixel(i, 0, 0x00000000);
        continue;
      }
      int idx = indices[i];
      if (idx < 0) {
        idx = 0;
      } else if (idx >= length) {
        idx = length - 1;
      }
      int r = (idx >> 8) & 0xFF;
      int g = idx & 0xFF;
      pm.drawPixel(i, 0, (r << 24) | (g << 16) | 0x000000FF);
    }
    indexTexture.draw(pm, 0, 0);
    pm.dispose();
    lastIndexRevision = contentRevision;
    lastCallUploaded = true;
    return true;
  }

  public void draw(Texture source, boolean horizontal, int length) {
    if (source == null || indexTexture == null || length <= 0) {
      return;
    }
    Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shader.bind();
    source.bind(0);
    indexTexture.bind(1);
    shader.setUniformi("u_source", 0);
    shader.setUniformi("u_index", 1);
    shader.setUniformf("u_length", length);
    shader.setUniformi("u_horizontal", horizontal ? 1 : 0);
    quad.render(shader, GL20.GL_TRIANGLE_STRIP);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
  }

  public boolean lastCallUploaded() {
    return lastCallUploaded;
  }

  @Override
  public void dispose() {
    if (indexTexture != null) {
      indexTexture.dispose();
      indexTexture = null;
    }
    quad.dispose();
    shader.dispose();
  }
}
