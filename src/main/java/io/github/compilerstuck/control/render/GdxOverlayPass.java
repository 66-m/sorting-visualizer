package io.github.compilerstuck.control.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import io.github.compilerstuck.control.render.asset.AppAssets;
import io.github.compilerstuck.control.render.asset.GdxImageRepository;
import io.github.compilerstuck.control.render.asset.ImageHandle;
import io.github.compilerstuck.control.render.asset.ImageRemapRenderer;
import io.github.compilerstuck.control.render.asset.ImageRepository;
import io.github.compilerstuck.control.render.asset.ImageStripRemap;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Overlay pass: text, ARGB pixel blits, and image-strip remap (GPU or CPU fallback), with pixel
 * texture cache.
 */
final class GdxOverlayPass implements Disposable {
  private static final Logger LOGGER = Logger.getLogger(GdxOverlayPass.class.getName());

  private final GdxRenderSystem host;
  private final AppAssets assets;
  private final GlyphLayout glyphLayout = new GlyphLayout();

  private ImageRepository imageRepository;
  private ImageRemapRenderer imageRemapRenderer;
  private int[] imageRemapScratch;
  private int imageRemapRevision = Integer.MIN_VALUE;

  private Pixmap pixelPixmap;
  private Texture pixelTexture;
  private int pixelTexW = -1;
  private int pixelTexH = -1;

  /** Cached remapped overlay texture (Wave D); reused when ARGB unchanged. */
  private Texture overlayArgbTexture;

  private int overlayArgbW = -1;
  private int overlayArgbH = -1;
  private int overlayArgbRevision = Integer.MIN_VALUE;

  GdxOverlayPass(GdxRenderSystem host, AppAssets assets) {
    this.host = host;
    this.assets = Objects.requireNonNull(assets, "assets");

    ImageRemapRenderer remap = null;
    if (Gdx.gl30 != null) {
      try {
        remap = new ImageRemapRenderer();
      } catch (RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Failed to init ImageRemapRenderer; using CPU image remap", e);
      }
    }
    imageRemapRenderer = remap;
  }

  void setImageRepository(ImageRepository imageRepository) {
    this.imageRepository = imageRepository;
  }

  void enterOverlayPass() {
    if (host.pipeline().inWorld3D()) {
      host.world3d().end3D();
    }
    GdxWorld2DPass world2d = host.world2d();
    world2d.endShapes();
    world2d.endSprites();
    host.pipeline().enterOverlay();
    host.applyOverlayViewport();
  }

  float measureTextWidth(String text, float sizePx) {
    if (text == null || text.isEmpty()) {
      return 0f;
    }
    glyphLayout.setText(assets.font(sizePx), text);
    return glyphLayout.width;
  }

  void drawText(String text, float x, float y, float sizePx) {
    if (text == null) {
      return;
    }
    GdxWorld2DPass world2d = host.world2d();
    world2d.endShapes();
    enterOverlayPass();
    world2d.beginSprites();
    host.frameStats().textDraws++;
    BitmapFont font = assets.font(sizePx);
    font.setColor(host.overlayTextR(), host.overlayTextG(), host.overlayTextB(), 1f);
    float drawY = y - font.getCapHeight();
    font.draw(world2d.sprites(), text, x, drawY);
  }

  void drawTexts(String[] texts, float x, float[] ys, float sizePx, int count) {
    if (texts == null || ys == null || count <= 0) {
      return;
    }
    GdxWorld2DPass world2d = host.world2d();
    world2d.endShapes();
    enterOverlayPass();
    world2d.beginSprites();
    BitmapFont font = assets.font(sizePx);
    font.setColor(host.overlayTextR(), host.overlayTextG(), host.overlayTextB(), 1f);
    float cap = font.getCapHeight();
    SpriteBatch sprites = world2d.sprites();
    FrameStats frameStats = host.frameStats();
    int n = Math.min(count, Math.min(texts.length, ys.length));
    for (int i = 0; i < n; i++) {
      String text = texts[i];
      if (text == null) {
        continue;
      }
      frameStats.textDraws++;
      font.draw(sprites, text, x, ys[i] - cap);
    }
  }

  void drawTexts(String[] texts, float[] xs, float[] ys, float sizePx, int count) {
    if (texts == null || xs == null || ys == null || count <= 0) {
      return;
    }
    GdxWorld2DPass world2d = host.world2d();
    world2d.endShapes();
    enterOverlayPass();
    world2d.beginSprites();
    BitmapFont font = assets.font(sizePx);
    font.setColor(host.overlayTextR(), host.overlayTextG(), host.overlayTextB(), 1f);
    float cap = font.getCapHeight();
    SpriteBatch sprites = world2d.sprites();
    FrameStats frameStats = host.frameStats();
    int n = Math.min(count, Math.min(texts.length, Math.min(xs.length, ys.length)));
    for (int i = 0; i < n; i++) {
      String text = texts[i];
      if (text == null) {
        continue;
      }
      frameStats.textDraws++;
      font.draw(sprites, text, xs[i], ys[i] - cap);
    }
  }

  void drawImageRemap(
      ImageHandle image,
      int[] stripIndices,
      boolean[] stripHighlight,
      int length,
      boolean horizontal,
      int contentRevision) {
    drawImageRemap(image, stripIndices, stripHighlight, length, horizontal, contentRevision, 1f);
  }

  void drawImageRemap(
      ImageHandle image,
      int[] stripIndices,
      boolean[] stripHighlight,
      int length,
      boolean horizontal,
      int contentRevision,
      float highlightStrength) {
    if (image == null || stripIndices == null || length <= 0) {
      return;
    }
    Texture source = null;
    if (imageRepository instanceof GdxImageRepository gdxRepo && gdxRepo.current() == image) {
      source = gdxRepo.sourceTexture();
    }
    if (source != null && imageRemapRenderer != null) {
      GdxWorld2DPass world2d = host.world2d();
      world2d.endShapes();
      world2d.endSprites();
      enterOverlayPass();
      boolean uploaded =
          imageRemapRenderer.uploadIndicesIfNeeded(
              stripIndices, stripHighlight, length, contentRevision);
      if (uploaded) {
        host.frameStats().pixelUploads++;
      }
      imageRemapRenderer.draw(source, horizontal, length, highlightStrength);
      return;
    }
    // CPU fallback (no GL30 / missing source texture)
    int w = image.width();
    int h = image.height();
    int need = w * h;
    if (imageRemapScratch == null || imageRemapScratch.length < need) {
      imageRemapScratch = new int[need];
    }
    if (imageRemapRevision != contentRevision) {
      ImageStripRemap.remap(
          image.argb(),
          imageRemapScratch,
          w,
          h,
          stripIndices,
          stripHighlight,
          length,
          horizontal,
          highlightStrength);
      imageRemapRevision = contentRevision;
    }
    drawArgbPixels(imageRemapScratch, w, h, contentRevision);
  }

  void drawArgbPixels(int[] argb, int width, int height) {
    drawArgbPixels(argb, width, height, System.identityHashCode(argb));
  }

  /**
   * Overlay blit. When {@code contentRevision} matches the last upload for this size, skips CPU→GPU
   * upload and redraws the cached texture ({@code pixelUploads} stays unchanged).
   */
  void drawArgbPixels(int[] argb, int width, int height, int contentRevision) {
    if (argb == null || width <= 0 || height <= 0 || argb.length < width * height) {
      return;
    }
    GdxWorld2DPass world2d = host.world2d();
    world2d.endShapes();
    enterOverlayPass();
    boolean needUpload =
        overlayArgbTexture == null
            || overlayArgbW != width
            || overlayArgbH != height
            || overlayArgbRevision != contentRevision;
    if (needUpload) {
      ensurePixelSurface(width, height);
      ByteBuffer pixels = pixelPixmap.getPixels();
      pixels.clear();
      int count = width * height;
      for (int i = 0; i < count; i++) {
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
      pixelTexture.draw(pixelPixmap, 0, 0);
      if (overlayArgbTexture != null && overlayArgbTexture != pixelTexture) {
        overlayArgbTexture.dispose();
      }
      overlayArgbTexture = pixelTexture;
      overlayArgbW = width;
      overlayArgbH = height;
      overlayArgbRevision = contentRevision;
      host.frameStats().pixelUploads++;
    }
    world2d.beginSprites();
    Texture tex = overlayArgbTexture != null ? overlayArgbTexture : pixelTexture;
    world2d.sprites().draw(tex, 0, 0, width, height, 0, 0, width, height, false, true);
  }

  /** Draws a pre-uploaded Overlay texture (image remap GPU path). */
  void drawOverlayTexture(Texture texture, float x, float y, float w, float h) {
    if (texture == null) {
      return;
    }
    GdxWorld2DPass world2d = host.world2d();
    world2d.endShapes();
    enterOverlayPass();
    world2d.beginSprites();
    world2d
        .sprites()
        .draw(texture, x, y, w, h, 0, 0, texture.getWidth(), texture.getHeight(), false, true);
  }

  private void ensurePixelSurface(int width, int height) {
    if (pixelPixmap != null && pixelTexW == width && pixelTexH == height) {
      return;
    }
    if (pixelTexture != null) {
      if (overlayArgbTexture == pixelTexture) {
        overlayArgbTexture = null;
      }
      pixelTexture.dispose();
    }
    if (pixelPixmap != null) {
      pixelPixmap.dispose();
    }
    pixelPixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
    pixelTexture = new Texture(pixelPixmap);
    pixelTexW = width;
    pixelTexH = height;
    overlayArgbRevision = Integer.MIN_VALUE;
  }

  @Override
  public void dispose() {
    if (pixelTexture != null) {
      pixelTexture.dispose();
      pixelTexture = null;
    }
    if (pixelPixmap != null) {
      pixelPixmap.dispose();
      pixelPixmap = null;
    }
    overlayArgbTexture = null;
    if (imageRemapRenderer != null) {
      imageRemapRenderer.dispose();
      imageRemapRenderer = null;
    }
  }
}
