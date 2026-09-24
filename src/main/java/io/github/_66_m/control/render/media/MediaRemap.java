package io.github._66_m.control.render.media;

/**
 * One frame's remap request: destination piece {@code i} shows source piece {@code indices[i]}.
 * Owned and reused by the visualization; the renderer re-uploads indices only when {@link
 * #indexRevision} changes and pixels only when the frame revision changes.
 */
public final class MediaRemap {
  public PixelFrame frame;
  public int[] indices;
  public boolean[] highlight;
  public int length;
  public RemapLayout layout = RemapLayout.COLUMNS;
  public int cols = 1;
  public int rows = 1;
  public long indexRevision;
  public float highlightStrength = 1f;

  /** {@code true}: letterbox to keep the frame's aspect ratio; {@code false}: fill the window. */
  public boolean contain = true;

  /** Draw thin dark separators between grid tiles. */
  public boolean gridLines;
}
