package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SoundViewModelTest {

  private AppContextTestFixture fx;
  private SoundViewModel vm;

  @BeforeEach
  void setUp() {
    fx = new AppContextTestFixture();
    vm = new SoundViewModel(fx.app);
  }

  @Test
  void initFromUnmutedSound() {
    assertTrue(vm.isSoundEnabled());
    assertFalse(fx.app.getSound().isMuted());
  }

  @Test
  void togglePropagatesToAppContext() {
    vm.setSoundEnabled(false);
    assertFalse(vm.isSoundEnabled());
    assertTrue(fx.app.getSound().isMuted());

    vm.setSoundEnabled(true);
    assertTrue(vm.isSoundEnabled());
    assertFalse(fx.app.getSound().isMuted());
  }

  @Test
  void disabledInputsRejectMutation() {
    vm.setInputsEnabled(false);
    vm.setSoundEnabled(false);
    assertTrue(vm.isSoundEnabled());
    assertFalse(fx.app.getSound().isMuted());
  }

  @Test
  void initRespectsMutedSound() {
    fx.app.setMuted(true);
    SoundViewModel mutedVm = new SoundViewModel(fx.app);
    assertFalse(mutedVm.isSoundEnabled());
    assertEquals(true, fx.app.getSound().isMuted());
  }
}
