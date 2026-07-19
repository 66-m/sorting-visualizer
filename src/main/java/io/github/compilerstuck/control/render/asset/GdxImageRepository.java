package io.github.compilerstuck.control.render.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import java.util.logging.Level;
import java.util.logging.Logger;

/** LibGDX Pixmap-backed {@link ImageRepository}. Texture upload is deferred until first GL use. */
public final class GdxImageRepository implements ImageRepository {
  private static final Logger LOGGER = Logger.getLogger(GdxImageRepository.class.getName());

  private ImageHandle current;
  private Texture sourceTexture;
  private boolean pendingUpload;

  @Override
  public ImageHandle load(String path, int targetW, int targetH) {
    if (path == null || path.isBlank()) {
      return null;
    }
    int w = Math.max(1, targetW);
    int h = Math.max(1, targetH);
    try {
      FileHandle handle = Gdx.files.absolute(path);
      if (!handle.exists()) {
        handle = Gdx.files.internal(path);
      }
      if (!handle.exists()) {
        return null;
      }
      Pixmap loaded = new Pixmap(handle);
      Pixmap resized = new Pixmap(w, h, Pixmap.Format.RGBA8888);
      resized.drawPixmap(loaded, 0, 0, loaded.getWidth(), loaded.getHeight(), 0, 0, w, h);
      loaded.dispose();
      int[] argb = copyPixmapToArgb(resized);
      resized.dispose();
      ImageHandle next = new ImageHandle(path, w, h, argb);
      release(current);
      current = next;
      pendingUpload = true;
      return next;
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to load image: " + path, e);
      return null;
    }
  }

  @Override
  public ImageHandle blank(int width, int height) {
    int w = Math.max(1, width);
    int h = Math.max(1, height);
    int[] argb = new int[w * h];
    ImageHandle next = new ImageHandle("", w, h, argb);
    release(current);
    current = next;
    disposeSourceTexture();
    pendingUpload = false;
    return next;
  }

  @Override
  public void release(ImageHandle handle) {
    if (handle == null) {
      return;
    }
    if (current == handle) {
      current = null;
      disposeSourceTexture();
      pendingUpload = false;
    }
  }

  /** Source texture for GPU remap; uploads on the GL thread when needed. */
  public Texture sourceTexture() {
    ensureUploaded();
    return sourceTexture;
  }

  public ImageHandle current() {
    return current;
  }

  private void ensureUploaded() {
    if (!pendingUpload || current == null) {
      return;
    }
    uploadSourceTexture(current);
    pendingUpload = false;
  }

  private void uploadSourceTexture(ImageHandle handle) {
    disposeSourceTexture();
    Pixmap pm = new Pixmap(handle.width(), handle.height(), Pixmap.Format.RGBA8888);
    java.nio.ByteBuffer pixels = pm.getPixels();
    pixels.clear();
    int[] argb = handle.argb();
    int n = handle.width() * handle.height();
    for (int i = 0; i < n; i++) {
      int c = argb[i];
      int a = (c >>> 24) & 0xFF;
      if (a == 0) {
        a = 0xFF;
      }
      pixels.put((byte) ((c >>> 16) & 0xFF));
      pixels.put((byte) ((c >>> 8) & 0xFF));
      pixels.put((byte) (c & 0xFF));
      pixels.put((byte) a);
    }
    pixels.flip();
    sourceTexture = new Texture(pm);
    sourceTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    pm.dispose();
  }

  private void disposeSourceTexture() {
    if (sourceTexture != null) {
      sourceTexture.dispose();
      sourceTexture = null;
    }
  }

  private static int[] copyPixmapToArgb(Pixmap pixmap) {
    int w = pixmap.getWidth();
    int h = pixmap.getHeight();
    int[] argb = new int[w * h];
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int rgba = pixmap.getPixel(x, y);
        argb[x + y * w] = ((rgba & 0xFF) << 24) | (rgba >>> 8);
      }
    }
    return argb;
  }

  @Override
  public void dispose() {
    release(current);
    disposeSourceTexture();
  }
}
