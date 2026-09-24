package io.github._66_m.control.render.mesh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class EarthTextureTest {

  @Test
  void bundledEarthIsATwoToOneEquirectangularImage() {
    TextureImage earth = EarthTexture.loadNow();
    assertNotNull(earth);
    assertEquals(4096, earth.width());
    assertEquals(2048, earth.height());
  }
}
