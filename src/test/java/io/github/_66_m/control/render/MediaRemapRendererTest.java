package io.github._66_m.control.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github._66_m.control.render.media.RemapLayout;
import org.junit.jupiter.api.Test;

class MediaRemapRendererTest {

  @Test
  void containLetterboxesToTheFrameAspect() {
    // 4:3 frame on a 16:9 screen: full height, pillarboxed width.
    float[] rect = MediaRemapRenderer.targetRect(640, 480, 1920, 1080, true);
    assertEquals(-1f, rect[1], 1e-6f);
    assertEquals(1f, rect[3], 1e-6f);
    assertEquals(0.75f, rect[2], 1e-4f);
  }

  @Test
  void stretchFillsTheWindow() {
    assertArrayEquals(
        new float[] {-1f, -1f, 1f, 1f},
        MediaRemapRenderer.targetRect(640, 480, 1920, 1080, false),
        1e-6f);
  }

  @Test
  void layoutsMapToShaderModes() {
    assertEquals(0, MediaRemapRenderer.modeOf(RemapLayout.COLUMNS));
    assertEquals(1, MediaRemapRenderer.modeOf(RemapLayout.ROWS));
    assertEquals(2, MediaRemapRenderer.modeOf(RemapLayout.GRID));
  }
}
