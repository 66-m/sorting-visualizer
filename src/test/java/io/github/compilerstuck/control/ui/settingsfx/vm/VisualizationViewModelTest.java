package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.config.SettingsDefaults;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VisualizationViewModelTest {

  private AppContextTestFixture fx;
  private VisualizationViewModel vm;

  @BeforeEach
  void setUp() {
    fx = new AppContextTestFixture();
    vm = new VisualizationViewModel(fx.app);
  }

  @Test
  void initSelectsDefaultBars() {
    assertEquals(SettingsDefaults.DEFAULT_VISUALIZATION_ID, vm.getSelectedId());
    assertFalse(vm.needsImage());
  }

  @Test
  void selectBarsKeepsId() {
    vm.selectVisualization("bars");
    assertEquals("bars", vm.getSelectedId());
    assertEquals("bars", fx.app.getPreferences().getVisualizationId());
  }

  @Test
  void invalidImagePathSetsErrorAndDoesNotClearPriorViz(@TempDir Path dir) {
    vm.selectVisualization("image-vertical");
    assertTrue(vm.needsImage());
    String priorName = fx.app.getVisualization().getName();

    assertFalse(vm.setImagePath(dir.resolve("missing.png")));
    assertFalse(vm.getImageError().isEmpty());
    assertEquals(priorName, fx.app.getVisualization().getName());
  }

  @Test
  void validImagePathClearsError(@TempDir Path dir) throws Exception {
    vm.selectVisualization("image-vertical");
    Path png = dir.resolve("tiny.png");
    // Minimal valid PNG (1x1) — HeadlessRenderContext.loadImage accepts any path that reaches it.
    Files.write(
        png,
        new byte[] {
          (byte) 0x89,
          0x50,
          0x4E,
          0x47,
          0x0D,
          0x0A,
          0x1A,
          0x0A,
          0x00,
          0x00,
          0x00,
          0x0D,
          0x49,
          0x48,
          0x44,
          0x52,
          0x00,
          0x00,
          0x00,
          0x01,
          0x00,
          0x00,
          0x00,
          0x01,
          0x08,
          0x02,
          0x00,
          0x00,
          0x00,
          (byte) 0x90,
          0x77,
          0x53,
          (byte) 0xDE,
          0x00,
          0x00,
          0x00,
          0x0C,
          0x49,
          0x44,
          0x41,
          0x54,
          0x08,
          (byte) 0xD7,
          0x63,
          (byte) 0xF8,
          (byte) 0xCF,
          (byte) 0xC0,
          0x00,
          0x00,
          0x00,
          0x03,
          0x00,
          0x01,
          0x00,
          0x05,
          (byte) 0xFE,
          (byte) 0xD4,
          (byte) 0xEF,
          0x00,
          0x00,
          0x00,
          0x00,
          0x49,
          0x45,
          0x4E,
          0x44,
          (byte) 0xAE,
          0x42,
          0x60,
          (byte) 0x82
        });

    assertTrue(vm.setImagePath(png));
    assertTrue(vm.getImageError().isEmpty());
    assertEquals(png.toAbsolutePath().toString(), vm.getImagePath());
  }

  @Test
  void disabledInputsRejectSelection() {
    vm.setInputsEnabled(false);
    vm.selectVisualization("circle");
    assertEquals(SettingsDefaults.DEFAULT_VISUALIZATION_ID, vm.getSelectedId());
  }
}
