package io.github.compilerstuck.control.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.List;
import java.util.UUID;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserPreferencesTest {

  private Preferences node;

  @BeforeEach
  void setUp() throws Exception {
    node =
        Preferences.userRoot()
            .node("io/github/compilerstuck/sorting-visualizer-test/" + UUID.randomUUID());
    node.clear();
  }

  @AfterEach
  void tearDown() throws Exception {
    node.removeNode();
  }

  @Test
  void missingKeysResolveToDefaults() {
    UserPreferences prefs = UserPreferences.load(node);
    assertEquals(SettingsDefaults.DEFAULT_SHUFFLE_TYPE, prefs.getShuffleType());
    assertEquals(SettingsDefaults.DEFAULT_PRINT_MEASUREMENTS, prefs.isPrintMeasurements());
    assertEquals(SettingsDefaults.DEFAULT_SHOW_COMPARISON_TABLE, prefs.isShowComparisonTable());
    assertEquals(SettingsDefaults.DEFAULT_IMAGE_PATH, prefs.getImagePath());
    assertEquals(SettingsDefaults.DEFAULT_GRADIENT_NAME, prefs.getGradientName());
    assertEquals(SettingsDefaults.DEFAULT_GRADIENT_COLOR1_RGB, prefs.getGradientColor1Rgb());
    assertEquals(SettingsDefaults.DEFAULT_GRADIENT_COLOR2_RGB, prefs.getGradientColor2Rgb());
    assertFalse(prefs.isRunAll());
    assertEquals("", prefs.getRunAllEntries());
    assertTrue(prefs.getRunAllEntriesList().isEmpty());
  }

  @Test
  void roundTripAllNewKeys() {
    UserPreferences prefs = new UserPreferences();
    prefs.setShuffleType(ShuffleType.REVERSE);
    prefs.setPrintMeasurements(false);
    prefs.setShowComparisonTable(false);
    prefs.setImagePath("/tmp/demo.png");
    prefs.setGradientName("Custom Gradient");
    prefs.setGradientColor1Rgb(Color.RED.getRGB());
    prefs.setGradientColor2Rgb(Color.BLUE.getRGB());
    prefs.setRunAll(true);
    prefs.setRunAllEntries(
        List.of(
            new RunAllEntryPref("bubble-sort", true),
            new RunAllEntryPref("quicksort-middle", false)));
    prefs.save(node);

    UserPreferences loaded = UserPreferences.load(node);
    assertEquals(ShuffleType.REVERSE, loaded.getShuffleType());
    assertFalse(loaded.isPrintMeasurements());
    assertFalse(loaded.isShowComparisonTable());
    assertEquals("/tmp/demo.png", loaded.getImagePath());
    assertEquals("Custom Gradient", loaded.getGradientName());
    assertEquals(Color.RED.getRGB(), loaded.getGradientColor1Rgb());
    assertEquals(Color.BLUE.getRGB(), loaded.getGradientColor2Rgb());
    assertTrue(loaded.isRunAll());
    List<RunAllEntryPref> entries = loaded.getRunAllEntriesList();
    assertEquals(2, entries.size());
    assertEquals("bubble-sort", entries.get(0).id());
    assertTrue(entries.get(0).selected());
    assertEquals("quicksort-middle", entries.get(1).id());
    assertFalse(entries.get(1).selected());
  }

  @Test
  void malformedShuffleAndRunAllFallBackSafely() {
    node.put("shuffleType", "NOT_A_REAL_SHUFFLE");
    node.put("runAllEntries", "bad-token,ok-id:1,:0,nope:");
    UserPreferences prefs = UserPreferences.load(node);
    assertEquals(SettingsDefaults.DEFAULT_SHUFFLE_TYPE, prefs.getShuffleType());
    List<RunAllEntryPref> entries = prefs.getRunAllEntriesList();
    assertEquals(1, entries.size());
    assertEquals("ok-id", entries.get(0).id());
    assertTrue(entries.get(0).selected());
  }
}
