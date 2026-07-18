package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
}
