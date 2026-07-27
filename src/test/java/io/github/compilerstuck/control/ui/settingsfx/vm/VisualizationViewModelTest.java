package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.control.config.visual.CubeSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettingsCodec;
import io.github.compilerstuck.visual.Cube;
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
  void unknownVisualizationIdFallsBackToBars() {
    vm.selectVisualization("does-not-exist");
    assertEquals("bars", vm.getSelectedId());
    assertFalse(vm.needsImage());
  }

  @Test
  void setImagePathIgnoredWhenVisualizationDoesNotNeedImage(@TempDir Path dir) throws Exception {
    assertFalse(vm.needsImage());
    Path png = dir.resolve("tiny.png");
    Files.write(png, new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47});
    assertFalse(vm.setImagePath(png));
    assertTrue(vm.getImageError().isEmpty());
  }

  @Test
  void disabledInputsRejectSelection() {
    vm.setInputsEnabled(false);
    vm.selectVisualization("circle");
    assertEquals(SettingsDefaults.DEFAULT_VISUALIZATION_ID, vm.getSelectedId());
  }

  @Test
  void cubeIsConfigurableBarsIsNot() {
    assertFalse(vm.isConfigurable());
    vm.selectVisualization("cube");
    assertTrue(vm.isConfigurable());
    assertInstanceOf(CubeSettings.class, vm.getCurrentCustomization());
  }

  @Test
  void applyCustomizationUpdatesCubeAndPrefs() {
    vm.selectVisualization("cube");
    CubeSettings custom = new CubeSettings(0.2, 80, false, 5.0);
    assertTrue(vm.applyCustomization(custom));
    assertEquals(custom, vm.getCurrentCustomization());
    assertEquals(custom, fx.app.getPreferences().getVisualSettingsMap().get(CubeSettings.ID));
    assertInstanceOf(Cube.class, fx.app.getVisualization());
    assertEquals(custom, ((Cube) fx.app.getVisualization()).currentSettings());
  }

  @Test
  void previewCustomizationUpdatesLiveVizWithoutPersisting() {
    vm.selectVisualization("cube");
    CubeSettings saved = CubeSettings.defaults();
    assertTrue(vm.applyCustomization(saved));
    CubeSettings draft = new CubeSettings(0.2, 80, false, 5.0);
    assertTrue(vm.previewCustomization(draft));
    assertEquals(draft, vm.getCurrentCustomization());
    assertEquals(draft, ((Cube) fx.app.getVisualization()).currentSettings());
    assertEquals(saved, fx.app.getPreferences().getVisualSettingsMap().get(CubeSettings.ID));
  }

  @Test
  void applyCustomizationRejectsWrongVisualizationId() {
    vm.selectVisualization("cube");
    // Envelope for cube settings but applied while pretending wrong id via bars selection.
    vm.selectVisualization("bars");
    assertFalse(vm.applyCustomization(CubeSettings.defaults()));
  }

  @Test
  void loadsPersistedCubeSettingsOnInit() {
    CubeSettings custom = new CubeSettings(0.25, 90, false, 4.5);
    fx.preferences.putVisualSettings(custom);
    VisualizationViewModel reloaded = new VisualizationViewModel(fx.app);
    reloaded.selectVisualization("cube");
    assertEquals(custom, reloaded.getCurrentCustomization());
    assertFalse(
        VisualizationSettingsCodec.decodeStore(fx.preferences.getVisualSettingsById()).isEmpty());
  }
}
