package io.github.compilerstuck.control.render.asset;

/** Headless {@link ImageRepository} for tests (no Pixmap / GDX). */
public final class FakeImageRepository implements ImageRepository {
  private ImageHandle current;

  @Override
  public ImageHandle load(String path, int targetW, int targetH) {
    return blank(targetW, targetH);
  }

  @Override
  public ImageHandle blank(int width, int height) {
    int w = Math.max(1, width);
    int h = Math.max(1, height);
    current = new ImageHandle("", w, h, new int[w * h]);
    return current;
  }

  @Override
  public void release(ImageHandle handle) {
    if (current == handle) {
      current = null;
    }
  }

  @Override
  public void dispose() {
    current = null;
  }
}
