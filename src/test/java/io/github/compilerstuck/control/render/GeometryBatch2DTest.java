package io.github.compilerstuck.control.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GeometryBatch2DTest {

  private static final int F = GeometryBatch2D.FLOATS_PER_VERT;

  @Test
  void packCircleQuadsWritesSixVertsPerCircleWithUv() {
    float[] xyd = {10f, 20f, 4f}; // diameter 4 → radius 2
    int[] argb = {0xFF00FF00};
    float[] out = new float[6 * F];
    float[] rgba = new float[4];
    int floats = GeometryBatch2D.packCircleQuads(xyd, argb, 1, out, rgba);
    assertEquals(6 * F, floats);
    // BL
    assertEquals(8f, out[0], 1e-4f);
    assertEquals(18f, out[1], 1e-4f);
    assertEquals(-1f, out[6], 1e-4f);
    assertEquals(-1f, out[7], 1e-4f);
    // BR of first triangle
    assertEquals(12f, out[F], 1e-4f);
    assertEquals(18f, out[F + 1], 1e-4f);
    assertEquals(1f, out[F + 6], 1e-4f);
    assertEquals(-1f, out[F + 7], 1e-4f);
  }

  @Test
  void packRectQuadsWritesSixVertsPerRect() {
    float[] xywh = {10f, 20f, 4f, 6f};
    int[] argb = {0xFFFFFFFF};
    float[] out = new float[6 * F];
    float[] rgba = new float[4];
    int floats = GeometryBatch2D.packRectQuads(xywh, argb, 1, out, rgba);
    assertEquals(6 * F, floats);
    // BL
    assertEquals(10f, out[0], 1e-4f);
    assertEquals(20f, out[1], 1e-4f);
    // BR
    assertEquals(14f, out[F], 1e-4f);
    assertEquals(20f, out[F + 1], 1e-4f);
    // TR (second triangle, vert 1)
    assertEquals(14f, out[4 * F], 1e-4f);
    assertEquals(26f, out[4 * F + 1], 1e-4f);
  }

  @Test
  void packEllipseLinesWritesConfiguredSegments() {
    int segs = GeometryBatch2D.ELLIPSE_SEGMENTS;
    float[] xywh = {0f, 0f, 2f, 4f};
    int[] argb = {0xFFFFFFFF};
    float[] out = new float[segs * 2 * F];
    float[] rgba = new float[4];
    int floats = GeometryBatch2D.packEllipseLines(xywh, argb, 1, out, rgba);
    assertEquals(segs * 2 * F, floats);
  }
}
