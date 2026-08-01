package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.compilerstuck.control.config.AppConfig;
import io.github.compilerstuck.control.config.CanvasBackground;
import io.github.compilerstuck.visual.Marker;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppearanceViewModelTest {

  private AppContextTestFixture fx;
  private AppearanceViewModel vm;

  @BeforeEach
  void setUp() {
    fx = new AppContextTestFixture();
    vm = new AppearanceViewModel(fx.app);
  }

  @Test
  void initFromAppContextGradientName() {
    assertEquals("Black -> Red", vm.getPresetNames().get(vm.getSelectedIndex()));
  }

  @Test
  void selectPresetUpdatesAppContext() {
    int redIndex = vm.getPresetNames().indexOf("Red");
    vm.selectPreset(redIndex);
    assertEquals("Red", fx.app.getColorGradient().getName());
    assertEquals(redIndex, vm.getSelectedIndex());
  }

  @Test
  void customColorsSelectCustomEntry() {
    Color c1 = new Color(10, 20, 30);
    Color c2 = new Color(40, 50, 60);
    vm.setCustomColors(c1, c2);
    assertEquals("Custom Gradient", fx.app.getColorGradient().getName());
    assertEquals(c1, vm.getColor1());
    assertEquals(c2, vm.getColor2());
    assertEquals(vm.getPresets().size() - 1, vm.getSelectedIndex());
  }

  @Test
  void disabledInputsRejectPresetChange() {
    int before = vm.getSelectedIndex();
    vm.setInputsEnabled(false);
    vm.selectPreset(0);
    assertEquals(before, vm.getSelectedIndex());
    assertFalse(vm.isInputsEnabled());
  }

  @Test
  void canvasBackgroundDefaultsToDark() {
    assertEquals(CanvasBackground.DARK, vm.getCanvasBackground());
  }

  @Test
  void setCanvasBackgroundUpdatesAppContextAndRemapsWhiteMarkers() {
    ColorGradient gradient = fx.app.getColorGradient();
    assertEquals(Color.WHITE, gradient.getMarkerColor(0, Marker.SET));

    vm.setCanvasBackground(CanvasBackground.WHITE);
    assertEquals(CanvasBackground.WHITE, fx.app.getCanvasBackground());
    assertEquals(CanvasBackground.WHITE, fx.app.getPreferences().getCanvasBackground());
    Color expected =
        new Color(
            AppConfig.CANVAS_BACKGROUND_DARK,
            AppConfig.CANVAS_BACKGROUND_DARK,
            AppConfig.CANVAS_BACKGROUND_DARK);
    assertEquals(expected, gradient.getMarkerColor(0, Marker.SET));

    vm.setCanvasBackground(CanvasBackground.DARK);
    assertEquals(Color.WHITE, gradient.getMarkerColor(0, Marker.SET));
  }
}
