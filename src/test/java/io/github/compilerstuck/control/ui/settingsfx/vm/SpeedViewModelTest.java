package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.config.SettingsDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpeedViewModelTest {

  private AppContextTestFixture fx;
  private SpeedViewModel vm;

  @BeforeEach
  void setUp() {
    fx = new AppContextTestFixture();
    vm = new SpeedViewModel(fx.app);
  }

  @Test
  void initFromPrefs() {
    assertEquals(SettingsDefaults.DEFAULT_SPEED_LEVEL, vm.getSpeedLevel());
    assertEquals(SettingsDefaults.DEFAULT_USE_STEP_ENGINE, vm.isUseStepEngine());
  }

  @Test
  void setSpeedLevelPropagates() {
    vm.setSpeedLevel(5);
    assertEquals(5, vm.getSpeedLevel());
    assertEquals(5, fx.app.getSpeedLevel());
  }

  @Test
  void setStepEnginePropagates() {
    vm.setUseStepEngine(true);
    assertTrue(vm.isUseStepEngine());
    assertTrue(fx.app.isUseStepEngine());
  }

  @Test
  void inputsEnabledDisablesBothLevelAndStepEngine() {
    vm.setInputsEnabled(false);
    vm.setSpeedLevel(1);
    vm.setUseStepEngine(true);
    assertEquals(SettingsDefaults.DEFAULT_SPEED_LEVEL, vm.getSpeedLevel());
    assertFalse(vm.isUseStepEngine());
  }
}
