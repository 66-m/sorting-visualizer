package io.github.compilerstuck.control.render.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.render.FakeRenderSystem;
import io.github.compilerstuck.sound.HeadlessSound;
import io.github.compilerstuck.visual.ImageHorizontal;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;
import org.junit.jupiter.api.Test;

class ImagePipelineTest {

  @Test
  void fakeRepositoryBlankProvidesHandle() {
    FakeImageRepository repo = new FakeImageRepository();
    ImageHandle handle = repo.blank(40, 30);
    assertNotNull(handle);
    assertEquals(40, handle.width());
    assertEquals(30, handle.height());
    assertEquals(40 * 30, handle.argb().length);
  }

  @Test
  void stripRemapHorizontalMovesBand() {
    int w = 4;
    int h = 4;
    int length = 2;
    int[] src = new int[w * h];
    // top band (src strip 0) = 1, bottom band (src strip 1) = 2
    for (int y = 0; y < 2; y++) {
      for (int x = 0; x < w; x++) {
        src[x + y * w] = 1;
      }
    }
    for (int y = 2; y < 4; y++) {
      for (int x = 0; x < w; x++) {
        src[x + y * w] = 2;
      }
    }
    int[] dst = new int[w * h];
    int[] indices = {1, 0}; // dest 0 from src 1, dest 1 from src 0
    ImageStripRemap.remap(src, dst, w, h, indices, null, length, true);
    assertEquals(2, dst[0]);
    assertEquals(1, dst[2 * w]);
  }

  @Test
  void imageHorizontalSkipsRemapWhenRevisionUnchanged() {
    ArrayController controller = new ArrayController(16);
    FakeRenderSystem rs = new FakeRenderSystem(64, 48);
    HeadlessSound sound = new HeadlessSound(controller);
    ColorGradient gradient = new ColorGradient(Color.BLACK, Color.WHITE, Color.RED, "img-test", 16);
    ImageHorizontal viz = new ImageHorizontal(controller, gradient, sound, rs);
    FakeImageRepository repo = new FakeImageRepository();
    viz.bindRepository(repo);
    viz.setImage(repo.blank(64, 48));

    rs.resetCounts();
    viz.render(1f / 60f);
    assertTrue(rs.pixelUploadCount() >= 1);
    assertEquals(1, rs.imageRemapCount());
    int uploadsAfterFirst = rs.pixelUploadCount();

    viz.render(1f / 60f);
    assertEquals(2, rs.imageRemapCount());
    assertEquals(uploadsAfterFirst, rs.pixelUploadCount());
  }
}
