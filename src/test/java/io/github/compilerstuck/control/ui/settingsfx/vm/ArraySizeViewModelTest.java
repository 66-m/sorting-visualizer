package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.catalog.VisualConstraints;
import io.github.compilerstuck.control.config.SettingsDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArraySizeViewModelTest {

  private AppContextTestFixture fx;
  private ArraySizeViewModel vm;

  @BeforeEach
  void setUp() {
    fx = new AppContextTestFixture();
    vm = new ArraySizeViewModel(fx.app, () -> VisualConstraints.NONE);
  }

  @Test
  void initFromAppSize() {
    assertEquals(fx.app.getSize(), vm.getSize());
    assertTrue(vm.isTextValid());
    assertTrue(vm.canRun());
  }

  @Test
  void invalidTextRejected() {
    vm.setText("12a");
    assertFalse(vm.isTextValid());
    assertFalse(vm.applyText());
    assertEquals(fx.app.getSize(), vm.getSize());
  }

  @Test
  void validTextApplies() {
    vm.setText("500");
    assertTrue(vm.applyText());
    assertEquals(500, vm.getSize());
    assertEquals(500, fx.app.getSize());
  }

  @Test
  void squareConstraintsFitSize() {
    ArraySizeViewModel squareVm = new ArraySizeViewModel(fx.app, () -> VisualConstraints.SQUARE);
    squareVm.setSizeFromSlider(50);
    assertTrue(VisualConstraints.isPerfectSquare(squareVm.getSize()));
    assertEquals(squareVm.getSize(), fx.app.getSize());
  }

  @Test
  void disabledWhileRunningRejectsSlider() {
    int before = vm.getSize();
    vm.setInputsEnabled(false);
    vm.setSizeFromSlider(999);
    assertEquals(before, vm.getSize());
  }

  @Test
  void updateArraySizeNoOpWhileAppRunning() {
    fx.setRunning(true);
    int before = fx.app.getSize();
    vm.setSizeFromSlider(800);
    // VM may update local state, but AppContext refuses resize while running
    assertEquals(before, fx.app.getSize());
  }

  @Test
  void canRunFalseAtMinSize() {
    vm.setSizeFromSlider(SettingsDefaults.ARRAY_SIZE_MIN);
    assertFalse(vm.canRun());
  }

  @Test
  void fpsWarningWhenIdlePreviewBelowThreshold() {
    fx.renderSystem.setFramesPerSecond(20);
    vm.refreshFpsWarning();
    assertTrue(vm.isFpsWarning());

    fx.renderSystem.setFramesPerSecond(30);
    vm.refreshFpsWarning();
    assertFalse(vm.isFpsWarning());
  }

  @Test
  void fpsWarningClearedWhileInputsDisabled() {
    fx.renderSystem.setFramesPerSecond(15);
    vm.refreshFpsWarning();
    assertTrue(vm.isFpsWarning());

    vm.setInputsEnabled(false);
    assertFalse(vm.isFpsWarning());
    vm.refreshFpsWarning();
    assertFalse(vm.isFpsWarning());
  }

  @Test
  void fpsWarningIgnoresUnknownZeroFps() {
    fx.renderSystem.setFramesPerSecond(0);
    vm.refreshFpsWarning();
    assertFalse(vm.isFpsWarning());
  }

  @Test
  void acceptsMaxArraySizeText() {
    vm.setText(String.valueOf(SettingsDefaults.ARRAY_SIZE_MAX));
    assertTrue(vm.isTextValid());
    assertTrue(vm.applyText());
    assertEquals(SettingsDefaults.ARRAY_SIZE_MAX, vm.getSize());
  }

  @Test
  void highSizeWarningOnCrossingAboveThreshold() {
    vm.setSizeFromSlider(10_000);
    assertFalse(vm.isHighSizeWarning());

    vm.setSizeFromSlider(25_000);
    assertTrue(vm.isHighSizeWarning());

    vm.setSizeFromSlider(40_000);
    assertTrue(vm.isHighSizeWarning());

    vm.setSizeFromSlider(15_000);
    assertFalse(vm.isHighSizeWarning());
  }

  @Test
  void highSizeWarningNotShownWhenStartingAlreadyAbove() {
    fx.app.updateArraySize(30_000);
    ArraySizeViewModel highVm = new ArraySizeViewModel(fx.app, () -> VisualConstraints.NONE);
    assertEquals(30_000, highVm.getSize());
    assertFalse(highVm.isHighSizeWarning());

    highVm.setSizeFromSlider(40_000);
    assertFalse(highVm.isHighSizeWarning());
  }
}
