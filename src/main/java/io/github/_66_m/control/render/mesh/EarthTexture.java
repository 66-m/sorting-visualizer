package io.github._66_m.control.render.mesh;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * The Globe's built-in texture: NASA Blue Marble (topography + bathymetry, December 2004; public
 * domain, NASA Visible Earth), bundled at 4096×2048. Decoded once on a background thread; until
 * then {@link #get()} returns a plain ocean-blue placeholder.
 */
public final class EarthTexture {

  static final String RESOURCE = "/textures/earth.jpg";

  private static final Logger LOGGER = Logger.getLogger(EarthTexture.class.getName());
  private static final TextureImage PLACEHOLDER = new TextureImage(1, 1, new int[] {0xFF0B2A5A});

  private static volatile TextureImage earth;
  private static boolean loading;

  private EarthTexture() {}

  /** The Earth texture, or the placeholder while it is still being decoded. */
  public static TextureImage get() {
    TextureImage t = earth;
    if (t != null) {
      return t;
    }
    startLoading();
    return PLACEHOLDER;
  }

  private static synchronized void startLoading() {
    if (loading) {
      return;
    }
    loading = true;
    Thread thread = new Thread(EarthTexture::load, "earth-texture");
    thread.setDaemon(true);
    thread.start();
  }

  private static void load() {
    try (InputStream in = EarthTexture.class.getResourceAsStream(RESOURCE)) {
      if (in == null) {
        throw new IOException("missing resource " + RESOURCE);
      }
      BufferedImage img = ImageIO.read(in);
      if (img == null) {
        throw new IOException("cannot decode " + RESOURCE);
      }
      earth = ObjLoader.fromImage(img, 8192);
    } catch (IOException | RuntimeException e) {
      LOGGER.log(Level.WARNING, "Cannot load built-in Earth texture", e);
    }
  }

  /** Decodes synchronously (tests). */
  static TextureImage loadNow() {
    load();
    return earth;
  }
}
