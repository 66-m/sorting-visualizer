package io.github._66_m.control.render.mesh;

import io.github._66_m.control.render.media.PixelFrame;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** CPU texture (ARGB, top row first) that can also be uploaded as a {@link PixelFrame}. */
public final class TextureImage implements PixelFrame {
  private final int width;
  private final int height;
  private final int[] argb;
  private ByteBuffer rgba;

  public TextureImage(int width, int height, int[] argb) {
    if (width <= 0 || height <= 0 || argb.length < width * height) {
      throw new IllegalArgumentException("bad texture size");
    }
    this.width = width;
    this.height = height;
    this.argb = argb;
  }

  @Override
  public int width() {
    return width;
  }

  @Override
  public int height() {
    return height;
  }

  public int[] argb() {
    return argb;
  }

  /**
   * Bilinear-free nearest sample with wrap-around; {@code v = 0} is the bottom row (OBJ
   * convention).
   */
  public int sample(float u, float v) {
    float fu = u - (float) Math.floor(u);
    float fv = v - (float) Math.floor(v);
    int x = Math.min(width - 1, (int) (fu * width));
    int y = Math.min(height - 1, (int) ((1f - fv) * height));
    return argb[y * width + x] | 0xFF000000;
  }

  @Override
  public synchronized ByteBuffer rgba() {
    if (rgba == null) {
      ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
      for (int i = 0; i < width * height; i++) {
        int c = argb[i];
        buf.put((byte) (c >> 16)).put((byte) (c >> 8)).put((byte) c).put((byte) 0xFF);
      }
      buf.flip();
      rgba = buf;
    }
    return rgba;
  }

  @Override
  public long revision() {
    return 1;
  }
}
