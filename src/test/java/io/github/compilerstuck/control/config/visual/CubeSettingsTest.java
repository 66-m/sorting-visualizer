package io.github.compilerstuck.control.config.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CubeSettingsTest {

  @Test
  void defaultsMatchLegacyCubeConstants() {
    CubeSettings d = CubeSettings.defaults();
    assertEquals(Math.PI / 10, d.rotationSpeedRadPerSec(), 1e-9);
    assertEquals(120, d.fillOpacity());
    assertTrue(d.wireframeEnabled());
    assertEquals(3.5, d.sceneScaleDivisor(), 1e-9);
    assertEquals("cube", d.visualizationId());
  }

  @Test
  void constructorClampsOutOfRange() {
    CubeSettings s = new CubeSettings(-1, 999, true, 0.1);
    assertEquals(CubeSettings.ROTATION_SPEED_MIN, s.rotationSpeedRadPerSec(), 1e-9);
    assertEquals(CubeSettings.FILL_OPACITY_MAX, s.fillOpacity());
    assertEquals(CubeSettings.SCENE_SCALE_DIVISOR_MIN, s.sceneScaleDivisor(), 1e-9);
  }
}
