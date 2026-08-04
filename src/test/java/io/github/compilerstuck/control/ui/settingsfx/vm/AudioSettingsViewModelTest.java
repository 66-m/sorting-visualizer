package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.config.audio.AudioSettings;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AudioSettingsViewModelTest {

  private AppContextTestFixture fx;
  private AudioSettingsViewModel vm;

  @BeforeEach
  void setUp() {
    fx = new AppContextTestFixture();
    vm = new AudioSettingsViewModel(fx.app);
  }

  @Test
  void getSettingsDefaultsWhenNeverApplied() {
    assertEquals(AudioSettings.defaults(), vm.getSettings());
  }

  @Test
  void applySettingsPersistsToAppContextAndSound() {
    AudioSettings custom = new AudioSettings(80, 110, 100, 32, 20, 90, 10, 5, 70, 60, 90);
    vm.applySettings(custom);
    assertEquals(custom, vm.getSettings());
    assertEquals(custom, fx.app.getAudioSettings());
    assertEquals(custom, fx.sound.getSettings());
  }

  @Test
  void defaultsReturnsAudioSettingsDefaults() {
    assertEquals(AudioSettings.defaults(), vm.defaults());
  }

  @Test
  void previewShuffleOnSilentSoundStillCompletes() {
    AtomicBoolean finished = new AtomicBoolean(false);
    vm.previewShuffle(AudioSettings.defaults(), 5000, 500, () -> finished.set(true));
    assertTrue(finished.get());
  }

  @Test
  void previewPitchSweepOnSilentSoundStillCompletes() {
    AtomicBoolean finished = new AtomicBoolean(false);
    vm.previewPitchSweep(AudioSettings.defaults(), 5000, 500, () -> finished.set(true));
    assertTrue(finished.get());
  }

  @Test
  void inputsEnabledDefaultsTrueAndToggles() {
    assertTrue(vm.isInputsEnabled());
    vm.setInputsEnabled(false);
    assertEquals(false, vm.isInputsEnabled());
  }
}
