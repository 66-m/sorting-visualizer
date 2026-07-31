package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DisplayViewModelTest {

  private AppContextTestFixture fx;
  private DisplayViewModel vm;

  @BeforeEach
  void setUp() {
    fx = new AppContextTestFixture();
    vm = new DisplayViewModel(fx.app);
  }

  @Test
  void togglesUpdateStateManager() {
    assertTrue(vm.isPrintMeasurements());
    assertFalse(vm.isShowComparisonTable());
    assertFalse(vm.isFiveSecondStartDelay());

    vm.setPrintMeasurements(false);
    assertFalse(fx.app.getStateManager().shouldPrintMeasurements());

    vm.setShowComparisonTable(true);
    assertTrue(fx.app.getStateManager().shouldShowComparisonTable());

    vm.setFiveSecondStartDelay(true);
    assertTrue(fx.app.isFiveSecondStartDelay());
  }

  @Test
  void exportWithoutResultsReturnsFalse(@TempDir Path dir) {
    assertFalse(vm.canExport());
    assertFalse(vm.exportCsv(dir.resolve("out.csv")));
  }

  @Test
  void disabledInputsRejectToggle() {
    vm.setInputsEnabled(false);
    vm.setShowComparisonTable(true);
    assertFalse(vm.isShowComparisonTable());
    vm.setFiveSecondStartDelay(true);
    assertFalse(vm.isFiveSecondStartDelay());
  }
}
