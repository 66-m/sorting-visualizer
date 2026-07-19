package io.github.compilerstuck.control.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LineSegmentPackTest {

  @Test
  void packSegmentsUsesWorldCoordsAsIs() {
    float[] xyzxyz = {1f, 2f, 3f, 4f, 5f, 6f};
    int[] argb = {0x80FF00AA};
    float[] out = new float[14];
    float[] tmpRgba = new float[4];
    int floats = LineRenderer3D.packSegments(xyzxyz, argb, 1, out, tmpRgba);
    assertEquals(14, floats);
    assertEquals(1f, out[0], 1e-5f);
    assertEquals(2f, out[1], 1e-5f);
    assertEquals(3f, out[2], 1e-5f);
    assertEquals(1f, out[3], 1e-5f);
    assertEquals(0f, out[4], 1e-5f);
    assertEquals(170f / 255f, out[5], 1e-5f);
    assertEquals(128f / 255f, out[6], 1e-5f);
    assertEquals(4f, out[7], 1e-5f);
    assertEquals(5f, out[8], 1e-5f);
    assertEquals(6f, out[9], 1e-5f);
  }
}
