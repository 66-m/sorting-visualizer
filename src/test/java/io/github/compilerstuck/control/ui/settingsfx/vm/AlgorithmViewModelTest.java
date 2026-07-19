package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.control.config.ShuffleType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlgorithmViewModelTest {

  private AppContextTestFixture fx;
  private AlgorithmViewModel vm;

  @BeforeEach
  void setUp() {
    fx = new AppContextTestFixture();
    vm = new AlgorithmViewModel(fx.app);
  }

  @Test
  void initSelectsDefaultAlgorithm() {
    assertEquals(SettingsDefaults.DEFAULT_ALGORITHM_ID, vm.getSelectedId());
  }

  @Test
  void selectAlgorithmPropagates() {
    vm.selectAlgorithm("bubble-sort");
    assertEquals("bubble-sort", vm.getSelectedId());
    assertEquals("bubble-sort", fx.app.getPreferences().getAlgorithmId());
    assertEquals(1, fx.app.getAlgorithms().size());
  }

  @Test
  void toggleRunAll() {
    assertFalse(vm.isRunAll());
    vm.setRunAll(true);
    assertTrue(vm.isRunAll());
    // Single-select ignored while run-all is on
    String before = vm.getSelectedId();
    vm.selectAlgorithm("bubble-sort");
    assertEquals(before, vm.getSelectedId());
  }

  @Test
  void canStartRequiresSelectedRunAllEntry() {
    assertTrue(vm.canStart());
    vm.setRunAll(true);
    for (int i = 0; i < vm.getEntries().size(); i++) {
      vm.setEntrySelected(i, false);
    }
    assertFalse(vm.canStart());
    vm.setEntrySelected(0, true);
    assertTrue(vm.canStart());
  }

  @Test
  void reorderSelectionModel() {
    String firstId = vm.getEntries().get(0).getId();
    String secondId = vm.getEntries().get(1).getId();
    vm.moveEntry(0, 1);
    assertEquals(secondId, vm.getEntries().get(0).getId());
    assertEquals(firstId, vm.getEntries().get(1).getId());
  }

  @Test
  void applyRunAllOrderReordersAndSelects() {
    String firstId = vm.getEntries().get(0).getId();
    String secondId = vm.getEntries().get(1).getId();
    List<String> ordered = new ArrayList<>();
    for (var entry : vm.getEntries()) {
      ordered.add(entry.getId());
    }
    ordered.set(0, secondId);
    ordered.set(1, firstId);
    vm.applyRunAllOrder(ordered, Set.of(secondId));
    assertEquals(secondId, vm.getEntries().get(0).getId());
    assertEquals(firstId, vm.getEntries().get(1).getId());
    assertTrue(vm.getEntries().get(0).isSelected());
    assertFalse(vm.getEntries().get(1).isSelected());
  }

  @Test
  void setEntrySelected() {
    vm.setEntrySelected(0, false);
    assertFalse(vm.getEntries().get(0).isSelected());
  }

  @Test
  void shuffleChangePropagates() {
    vm.setShuffleType(ShuffleType.REVERSE);
    assertEquals(ShuffleType.REVERSE, vm.getShuffleType());
    assertEquals(ShuffleType.REVERSE, fx.app.getArrayController().getShuffleType());
  }

  @Test
  void disabledWhileRunningRejectsChanges() {
    vm.setInputsEnabled(false);
    vm.selectAlgorithm("bubble-sort");
    vm.setShuffleType(ShuffleType.SORTED);
    vm.setRunAll(true);
    assertEquals(SettingsDefaults.DEFAULT_ALGORITHM_ID, vm.getSelectedId());
    assertEquals(ShuffleType.RANDOM, vm.getShuffleType());
    assertFalse(vm.isRunAll());
  }
}
