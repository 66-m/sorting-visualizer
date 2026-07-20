package io.github.compilerstuck.control.config;

import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettingsCodec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Persists user settings via {@link Preferences}. Node: {@code
 * io/github/compilerstuck/sorting-visualizer}.
 *
 * <p>Phase 4 adds keys additively (shuffle, run-all, gradient, display, image path). Missing keys
 * resolve to {@link SettingsDefaults}; {@code defaultsVersion} stays at 1 (no semantic migration).
 */
public final class UserPreferences {

  private static final Logger LOGGER = Logger.getLogger(UserPreferences.class.getName());

  private static final String NODE = "io/github/compilerstuck/sorting-visualizer";
  private static final String KEY_ALGORITHM = "algorithmId";
  private static final String KEY_VISUALIZATION = "visualizationId";
  private static final String KEY_ARRAY_SIZE = "arraySize";
  private static final String KEY_SPEED = "speedLevel";
  private static final String KEY_MUTED = "muted";
  private static final String KEY_DEFAULTS_VERSION = "defaultsVersion";
  // Phase 4 additive keys
  private static final String KEY_SHUFFLE = "shuffleType";
  private static final String KEY_PRINT_MEASUREMENTS = "printMeasurements";
  private static final String KEY_SHOW_COMPARISON = "showComparisonTable";
  private static final String KEY_IMAGE_PATH = "imagePath";
  private static final String KEY_GRADIENT_NAME = "gradientName";
  private static final String KEY_GRADIENT_COLOR1 = "gradientColor1";
  private static final String KEY_GRADIENT_COLOR2 = "gradientColor2";
  private static final String KEY_RUN_ALL = "runAll";
  private static final String KEY_RUN_ALL_ENTRIES = "runAllEntries";
  private static final String KEY_PERF_STATS = "perfStats";
  private static final String KEY_VISUAL_SETTINGS_BY_ID = "visualSettingsById";
  private static final int CURRENT_DEFAULTS_VERSION = 1;

  public static final String DEFAULT_ALGORITHM_ID = SettingsDefaults.DEFAULT_ALGORITHM_ID;
  public static final String DEFAULT_VISUALIZATION_ID = SettingsDefaults.DEFAULT_VISUALIZATION_ID;
  public static final int DEFAULT_ARRAY_SIZE = SettingsDefaults.DEFAULT_ARRAY_SIZE;
  public static final int DEFAULT_SPEED_LEVEL = SettingsDefaults.DEFAULT_SPEED_LEVEL;

  private String algorithmId = DEFAULT_ALGORITHM_ID;
  private String visualizationId = DEFAULT_VISUALIZATION_ID;
  private int arraySize = DEFAULT_ARRAY_SIZE;
  private int speedLevel = DEFAULT_SPEED_LEVEL;
  private boolean muted = SettingsDefaults.DEFAULT_MUTED;
  private ShuffleType shuffleType = SettingsDefaults.DEFAULT_SHUFFLE_TYPE;
  private boolean printMeasurements = SettingsDefaults.DEFAULT_PRINT_MEASUREMENTS;
  private boolean showComparisonTable = SettingsDefaults.DEFAULT_SHOW_COMPARISON_TABLE;
  private String imagePath = SettingsDefaults.DEFAULT_IMAGE_PATH;
  private String gradientName = SettingsDefaults.DEFAULT_GRADIENT_NAME;
  private int gradientColor1Rgb = SettingsDefaults.DEFAULT_GRADIENT_COLOR1_RGB;
  private int gradientColor2Rgb = SettingsDefaults.DEFAULT_GRADIENT_COLOR2_RGB;
  private boolean runAll = SettingsDefaults.DEFAULT_RUN_ALL;
  private String runAllEntries = SettingsDefaults.DEFAULT_RUN_ALL_ENTRIES;
  private boolean perfStats = SettingsDefaults.DEFAULT_PERF_STATS;
  private String visualSettingsById = SettingsDefaults.DEFAULT_VISUAL_SETTINGS_BY_ID;

  public static UserPreferences load() {
    return load(Preferences.userRoot().node(NODE));
  }

  /** Loads from an arbitrary prefs node (tests use an isolated child node). */
  public static UserPreferences load(Preferences node) {
    UserPreferences prefs = new UserPreferences();
    prefs.algorithmId = node.get(KEY_ALGORITHM, DEFAULT_ALGORITHM_ID);
    prefs.visualizationId = node.get(KEY_VISUALIZATION, DEFAULT_VISUALIZATION_ID);
    prefs.arraySize = clampSize(node.getInt(KEY_ARRAY_SIZE, DEFAULT_ARRAY_SIZE));
    prefs.speedLevel = clampSpeed(node.getInt(KEY_SPEED, DEFAULT_SPEED_LEVEL));
    prefs.muted = node.getBoolean(KEY_MUTED, SettingsDefaults.DEFAULT_MUTED);
    if (node.getInt(KEY_DEFAULTS_VERSION, 0) < CURRENT_DEFAULTS_VERSION) {
      node.putInt(KEY_DEFAULTS_VERSION, CURRENT_DEFAULTS_VERSION);
    }
    prefs.shuffleType =
        parseShuffleType(node.get(KEY_SHUFFLE, SettingsDefaults.DEFAULT_SHUFFLE_TYPE.name()));
    prefs.printMeasurements =
        node.getBoolean(KEY_PRINT_MEASUREMENTS, SettingsDefaults.DEFAULT_PRINT_MEASUREMENTS);
    prefs.showComparisonTable =
        node.getBoolean(KEY_SHOW_COMPARISON, SettingsDefaults.DEFAULT_SHOW_COMPARISON_TABLE);
    prefs.imagePath = node.get(KEY_IMAGE_PATH, SettingsDefaults.DEFAULT_IMAGE_PATH);
    if (prefs.imagePath == null) {
      prefs.imagePath = SettingsDefaults.DEFAULT_IMAGE_PATH;
    }
    prefs.gradientName = node.get(KEY_GRADIENT_NAME, SettingsDefaults.DEFAULT_GRADIENT_NAME);
    prefs.gradientColor1Rgb =
        node.getInt(KEY_GRADIENT_COLOR1, SettingsDefaults.DEFAULT_GRADIENT_COLOR1_RGB);
    prefs.gradientColor2Rgb =
        node.getInt(KEY_GRADIENT_COLOR2, SettingsDefaults.DEFAULT_GRADIENT_COLOR2_RGB);
    prefs.runAll = node.getBoolean(KEY_RUN_ALL, SettingsDefaults.DEFAULT_RUN_ALL);
    prefs.runAllEntries = node.get(KEY_RUN_ALL_ENTRIES, SettingsDefaults.DEFAULT_RUN_ALL_ENTRIES);
    if (prefs.runAllEntries == null) {
      prefs.runAllEntries = SettingsDefaults.DEFAULT_RUN_ALL_ENTRIES;
    }
    prefs.perfStats = node.getBoolean(KEY_PERF_STATS, SettingsDefaults.DEFAULT_PERF_STATS);
    prefs.visualSettingsById =
        node.get(KEY_VISUAL_SETTINGS_BY_ID, SettingsDefaults.DEFAULT_VISUAL_SETTINGS_BY_ID);
    if (prefs.visualSettingsById == null) {
      prefs.visualSettingsById = SettingsDefaults.DEFAULT_VISUAL_SETTINGS_BY_ID;
    }
    return prefs;
  }

  public void save() {
    save(Preferences.userRoot().node(NODE));
  }

  /** Saves to an arbitrary prefs node (tests use an isolated child node). */
  public void save(Preferences node) {
    node.put(KEY_ALGORITHM, algorithmId);
    node.put(KEY_VISUALIZATION, visualizationId);
    node.putInt(KEY_ARRAY_SIZE, arraySize);
    node.putInt(KEY_SPEED, speedLevel);
    node.putBoolean(KEY_MUTED, muted);
    node.putInt(KEY_DEFAULTS_VERSION, CURRENT_DEFAULTS_VERSION);
    node.put(KEY_SHUFFLE, shuffleType.name());
    node.putBoolean(KEY_PRINT_MEASUREMENTS, printMeasurements);
    node.putBoolean(KEY_SHOW_COMPARISON, showComparisonTable);
    node.put(KEY_IMAGE_PATH, imagePath != null ? imagePath : "");
    node.put(
        KEY_GRADIENT_NAME,
        gradientName != null ? gradientName : SettingsDefaults.DEFAULT_GRADIENT_NAME);
    node.putInt(KEY_GRADIENT_COLOR1, gradientColor1Rgb);
    node.putInt(KEY_GRADIENT_COLOR2, gradientColor2Rgb);
    node.putBoolean(KEY_RUN_ALL, runAll);
    node.put(KEY_RUN_ALL_ENTRIES, runAllEntries != null ? runAllEntries : "");
    node.putBoolean(KEY_PERF_STATS, perfStats);
    node.put(
        KEY_VISUAL_SETTINGS_BY_ID,
        visualSettingsById != null
            ? visualSettingsById
            : SettingsDefaults.DEFAULT_VISUAL_SETTINGS_BY_ID);
  }

  private static ShuffleType parseShuffleType(String raw) {
    if (raw == null || raw.isBlank()) {
      return SettingsDefaults.DEFAULT_SHUFFLE_TYPE;
    }
    try {
      return ShuffleType.valueOf(raw.trim());
    } catch (IllegalArgumentException ex) {
      LOGGER.log(Level.WARNING, "Unknown shuffleType ''{0}'', using default", raw);
      return SettingsDefaults.DEFAULT_SHUFFLE_TYPE;
    }
  }

  public String getAlgorithmId() {
    return algorithmId;
  }

  public void setAlgorithmId(String algorithmId) {
    this.algorithmId = algorithmId != null ? algorithmId : DEFAULT_ALGORITHM_ID;
  }

  public String getVisualizationId() {
    return visualizationId;
  }

  public void setVisualizationId(String visualizationId) {
    this.visualizationId = visualizationId != null ? visualizationId : DEFAULT_VISUALIZATION_ID;
  }

  public int getArraySize() {
    return arraySize;
  }

  public void setArraySize(int arraySize) {
    this.arraySize = clampSize(arraySize);
  }

  public int getSpeedLevel() {
    return speedLevel;
  }

  public void setSpeedLevel(int speedLevel) {
    this.speedLevel = clampSpeed(speedLevel);
  }

  public boolean isMuted() {
    return muted;
  }

  public void setMuted(boolean muted) {
    this.muted = muted;
  }

  public ShuffleType getShuffleType() {
    return shuffleType;
  }

  public void setShuffleType(ShuffleType shuffleType) {
    this.shuffleType = shuffleType != null ? shuffleType : SettingsDefaults.DEFAULT_SHUFFLE_TYPE;
  }

  public boolean isPrintMeasurements() {
    return printMeasurements;
  }

  public void setPrintMeasurements(boolean printMeasurements) {
    this.printMeasurements = printMeasurements;
  }

  public boolean isShowComparisonTable() {
    return showComparisonTable;
  }

  public void setShowComparisonTable(boolean showComparisonTable) {
    this.showComparisonTable = showComparisonTable;
  }

  public String getImagePath() {
    return imagePath;
  }

  public void setImagePath(String imagePath) {
    this.imagePath = imagePath != null ? imagePath : "";
  }

  public String getGradientName() {
    return gradientName;
  }

  public void setGradientName(String gradientName) {
    this.gradientName =
        gradientName != null && !gradientName.isBlank()
            ? gradientName
            : SettingsDefaults.DEFAULT_GRADIENT_NAME;
  }

  public int getGradientColor1Rgb() {
    return gradientColor1Rgb;
  }

  public void setGradientColor1Rgb(int gradientColor1Rgb) {
    this.gradientColor1Rgb = gradientColor1Rgb;
  }

  public int getGradientColor2Rgb() {
    return gradientColor2Rgb;
  }

  public void setGradientColor2Rgb(int gradientColor2Rgb) {
    this.gradientColor2Rgb = gradientColor2Rgb;
  }

  public boolean isRunAll() {
    return runAll;
  }

  public void setRunAll(boolean runAll) {
    this.runAll = runAll;
  }

  public String getRunAllEntries() {
    return runAllEntries;
  }

  public void setRunAllEntries(String runAllEntries) {
    this.runAllEntries = runAllEntries != null ? runAllEntries : "";
  }

  public void setRunAllEntries(List<RunAllEntryPref> entries) {
    this.runAllEntries = RunAllPreferencesCodec.encode(entries);
  }

  public List<RunAllEntryPref> getRunAllEntriesList() {
    return RunAllPreferencesCodec.decode(runAllEntries);
  }

  public boolean isPerfStats() {
    return perfStats;
  }

  public void setPerfStats(boolean perfStats) {
    this.perfStats = perfStats;
  }

  public String getVisualSettingsById() {
    return visualSettingsById;
  }

  public void setVisualSettingsById(String visualSettingsById) {
    this.visualSettingsById =
        visualSettingsById != null && !visualSettingsById.isBlank()
            ? visualSettingsById
            : SettingsDefaults.DEFAULT_VISUAL_SETTINGS_BY_ID;
  }

  public Map<String, VisualizationSettings> getVisualSettingsMap() {
    return VisualizationSettingsCodec.decodeStore(visualSettingsById);
  }

  public void putVisualSettings(VisualizationSettings settings) {
    if (settings == null) {
      return;
    }
    Map<String, VisualizationSettings> map = new LinkedHashMap<>(getVisualSettingsMap());
    map.put(settings.visualizationId(), settings);
    visualSettingsById = VisualizationSettingsCodec.encodeStore(map);
  }

  /** Clears persisted per-visualization settings (all vizs revert to code defaults on load). */
  public void clearVisualSettings() {
    visualSettingsById = SettingsDefaults.DEFAULT_VISUAL_SETTINGS_BY_ID;
  }

  private static int clampSize(int size) {
    return SettingsDefaults.clampArraySize(size);
  }

  private static int clampSpeed(int level) {
    return SettingsDefaults.clampSpeedLevel(level);
  }
}
