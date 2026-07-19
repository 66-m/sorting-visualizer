package io.github.compilerstuck.control.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GeometryBatch2DTest {

  @Test
  void packCircleQuadsWritesSixVertsPerCircle() {
    float[] xyd = {10f, 20f, 4f}; // diameter 4 → radius 2
    int[] argb = {0xFF00FF00};
    float[] out = new float[6 * 6];
    float[] rgba = new float[4];
    int floats = GeometryBatch2D.packCircleQuads(xyd, argb, 1, out, rgba);
    assertEquals(36, floats);
    // BL
    assertEquals(8f, out[0], 1e-4f);
    assertEquals(18f, out[1], 1e-4f);
    // BR of first triangle
    assertEquals(12f, out[6], 1e-4f);
    assertEquals(18f, out[7], 1e-4f);
  }

  @Test
  void packEllipseLinesWritesTwelveSegments() {
    float[] xywh = {0f, 0f, 2f, 4f};
    int[] argb = {0xFFFFFFFF};
    float[] out = new float[12 * 2 * 6];
    float[] rgba = new float[4];
    int floats = GeometryBatch2D.packEllipseLines(xywh, argb, 1, out, rgba);
    assertEquals(12 * 2 * 6, floats);
  }
}
