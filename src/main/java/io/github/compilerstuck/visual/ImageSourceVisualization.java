package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.render.asset.ImageHandle;
import io.github.compilerstuck.control.render.asset.ImageRepository;

/** Optional image source for image-remapping visualizations. */
public interface ImageSourceVisualization {

  /** Accept a loaded handle (from {@link ImageRepository} on the render thread). */
  void setImage(ImageHandle handle);

  /**
   * Load via a bound {@link ImageRepository}, or seed a blank buffer when none is bound (headless).
   * Returns false on failure.
   */
  boolean setImagePath(String path);

  /** Wire the shared repository for path loads and window-resize reloads. */
  default void bindRepository(ImageRepository repository) {}
}
