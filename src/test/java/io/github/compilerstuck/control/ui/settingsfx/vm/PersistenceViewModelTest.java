package io.github.compilerstuck.control.ui.settingsfx.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.config.RunAllEntryPref;
import io.github.compilerstuck.control.config.ShuffleType;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** AppContext / VM persistence hooks (in-memory UserPreferences; save hits test-safe setters). */
class PersistenceViewModelTest {

  private AppContextTestFixture fx;

  @BeforeEach
  void setUp() {
    fx = new AppContextTestFixture();
  }

  @Test
  void displayTogglesPersistOnAppContext() {
    fx.app.setPrintMeasurements(false);
    fx.app.setShowComparisonTable(true);
    fx.app.setFiveSecondStartDelay(true);
    assertFalse(fx.app.getPreferences().isPrintMeasurements());
    assertTrue(fx.app.getPreferences().isShowComparisonTable());
    assertTrue(fx.app.getPreferences().isFiveSecondStartDelay());
  }

  @Test
  void shufflePersistsViaAppContext() {
    fx.app.setShuffleType(ShuffleType.SORTED);
    assertEquals(ShuffleType.SORTED, fx.app.getPreferences().getShuffleType());
    assertEquals(ShuffleType.SORTED, fx.app.getArrayController().getShuffleType());
  }

  @Test
  void gradientPersistsOnSetColorGradient() {
    ColorGradient custom =
        new ColorGradient(Color.YELLOW, Color.CYAN, Color.WHITE, "Custom Gradient");
    fx.app.setColorGradient(custom);
    assertEquals("Custom Gradient", fx.app.getPreferences().getGradientName());
    assertEquals(Color.YELLOW.getRGB(), fx.app.getPreferences().getGradientColor1Rgb());
    assertEquals(Color.CYAN.getRGB(), fx.app.getPreferences().getGradientColor2Rgb());
  }

  @Test
  void imagePathPersistsViaAppContext() {
    fx.app.setImagePath("/tmp/viz.png");
    assertEquals("/tmp/viz.png", fx.app.getPreferences().getImagePath());
  }

  @Test
  void algorithmViewModelPersistsRunAllAndRestores() {
    AlgorithmViewModel vm = new AlgorithmViewModel(fx.app);
    vm.setRunAll(true);
    vm.setEntrySelected(0, false);
    String firstId = vm.getEntries().get(0).getId();
    String secondId = vm.getEntries().get(1).getId();
    vm.moveEntry(0, 1);

    assertTrue(fx.app.getPreferences().isRunAll());
    List<RunAllEntryPref> saved = fx.app.getPreferences().getRunAllEntriesList();
    assertFalse(saved.isEmpty());
    assertEquals(secondId, saved.get(0).id());
    assertEquals(firstId, saved.get(1).id());

    AlgorithmViewModel restored = new AlgorithmViewModel(fx.app);
    assertTrue(restored.isRunAll());
    assertEquals(secondId, restored.getEntries().get(0).getId());
    assertEquals(firstId, restored.getEntries().get(1).getId());
  }

  @Test
  void algorithmViewModelPersistsShuffle() {
    AlgorithmViewModel vm = new AlgorithmViewModel(fx.app);
    vm.setShuffleType(ShuffleType.ALMOST_SORTED);
    assertEquals(ShuffleType.ALMOST_SORTED, fx.app.getPreferences().getShuffleType());
  }
}
