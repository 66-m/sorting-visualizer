package io.github._66_m.control.render.media;

/** How a source frame is cut into array elements for strip / tile remapping. */
public enum RemapLayout {
  /** N vertical strips, left to right. */
  COLUMNS,
  /** N horizontal strips, top to bottom. */
  ROWS,
  /** N tiles in a {@code cols × rows} grid, row-major from the top-left. */
  GRID
}
