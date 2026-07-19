package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DebugViewModelTest {

  private AppContextTestFixture fx;
  private DebugViewModel vm;

  @BeforeEach
  void setUp() {
    fx = new AppContextTestFixture();
    vm = new DebugViewModel(fx.app);
  }

  @Test
  void defaultsOff() {
    assertFalse(vm.isPerfStats());
    assertFalse(fx.app.isPerfStatsEnabled());
    assertFalse(fx.preferences.isPerfStats());
  }

  @Test
  void toggleUpdatesAppContextAndPreferences() {
    vm.setPerfStats(true);
    assertTrue(vm.isPerfStats());
    assertTrue(fx.app.isPerfStatsEnabled());
    assertTrue(fx.preferences.isPerfStats());

    vm.setPerfStats(false);
    assertFalse(vm.isPerfStats());
    assertFalse(fx.app.isPerfStatsEnabled());
    assertFalse(fx.preferences.isPerfStats());
  }
}
