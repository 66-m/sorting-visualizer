package io.github.compilerstuck.control.render.asset;

import com.badlogic.gdx.utils.Disposable;

/** Loads and resizes user images for Overlay image visualizations. */
public interface ImageRepository extends Disposable {

  /**
   * Load and stretch an image to {@code targetW}×{@code targetH}. Disposes the previous handle
   * owned by this repository when a new path is loaded.
   *
   * @return handle or {@code null} on failure
   */
  ImageHandle load(String path, int targetW, int targetH);

  /**
   * Load scaled to fit inside {@code maxW}×{@code maxH} while preserving aspect ratio (no letterbox
   * padding — the returned handle is smaller than the window when needed).
   */
  default ImageHandle loadContained(String path, int maxW, int maxH) {
    return load(path, maxW, maxH);
  }

  /** Blank opaque buffer (tests / missing image). */
  ImageHandle blank(int width, int height);

  /** Release a handle's GPU resources if any; safe if unknown. */
  void release(ImageHandle handle);
}
