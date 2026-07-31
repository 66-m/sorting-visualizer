package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  }

  @Test
  void setSpeedLevelPropagates() {
    vm.setSpeedLevel(10);
    assertEquals(10, vm.getSpeedLevel());
    assertEquals(10, fx.app.getSpeedLevel());
  }

  @Test
  void inputsEnabledDisablesSpeedLevel() {
    vm.setInputsEnabled(false);
    vm.setSpeedLevel(1);
    assertEquals(SettingsDefaults.DEFAULT_SPEED_LEVEL, vm.getSpeedLevel());
  }
}
