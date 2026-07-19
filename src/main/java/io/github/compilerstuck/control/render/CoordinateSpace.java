package io.github.compilerstuck.control.render;

/**
 * Authoring space for world geometry. World2D is bottom-left Y-up; World3D is center Y-up. Overlay
 * draws ({@code drawText}, {@code drawArgbPixels}) always use screen Y-down regardless.
 */
public enum CoordinateSpace {
  WORLD_YUP
}
