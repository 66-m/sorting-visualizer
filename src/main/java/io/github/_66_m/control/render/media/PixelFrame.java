package io.github._66_m.control.render.media;

import java.nio.ByteBuffer;

/**
 * A CPU image in tightly packed RGBA8888 (top row first) that the render system uploads to a
 * texture. {@link #revision()} changes whenever the pixels change, so unchanged frames are not
 * uploaded again.
 */
public interface PixelFrame {
  int width();

  int height();

  /** Direct buffer of {@code width·height·4} bytes, position 0. Read-only for the renderer. */
  ByteBuffer rgba();

  long revision();
}
