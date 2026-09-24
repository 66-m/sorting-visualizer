package io.github._66_m.visual;

import io.github._66_m.control.config.MediaKind;

/**
 * A visualization fed from an external media file (video, 3D model, texture). Loading may finish
 * asynchronously; the visualization shows its own progress / error state and a built-in fallback
 * until a file is loaded.
 */
public interface MediaSourceVisualization {

  /** Kind of file this visualization accepts. */
  MediaKind mediaKind();

  /**
   * Starts loading {@code path}. May be called from any thread. Returns {@code false} when the file
   * is rejected immediately (unreadable / wrong type); later failures show on the canvas.
   */
  boolean loadMedia(String path);

  /** Path of the file currently loaded or loading; empty when using the built-in fallback. */
  String mediaPath();
}
