package io.github.compilerstuck.control.config;

import java.util.prefs.Preferences;

/**
 * Persists user settings via {@link Preferences}. Node: {@code
 * io/github/compilerstuck/sorting-visualizer}.
 */
public final class UserPreferences {

  private static final String NODE = "io/github/compilerstuck/sorting-visualizer";
  private static final String KEY_ALGORITHM = "algorithmId";
  private static final String KEY_VISUALIZATION = "visualizationId";
  private static final String KEY_ARRAY_SIZE = "arraySize";
  private static final String KEY_SPEED = "speedLevel";
  private static final String KEY_MUTED = "muted";
  private static final String KEY_STEP_ENGINE = "useStepEngine";
  private static final String KEY_DEFAULTS_VERSION = "defaultsVersion";
  private static final int CURRENT_DEFAULTS_VERSION = 1;

  public static final String DEFAULT_ALGORITHM_ID = "quicksort-middle";
  public static final String DEFAULT_VISUALIZATION_ID = "bars";
  public static final int DEFAULT_ARRAY_SIZE = MainControllerConfig.DEFAULT_ARRAY_SIZE;
  public static final int DEFAULT_SPEED_LEVEL = 3;
  public static final boolean DEFAULT_USE_STEP_ENGINE = false;

  private String algorithmId = DEFAULT_ALGORITHM_ID;
  private String visualizationId = DEFAULT_VISUALIZATION_ID;
  private int arraySize = DEFAULT_ARRAY_SIZE;
  private int speedLevel = DEFAULT_SPEED_LEVEL;
  private boolean muted = false;
  private boolean useStepEngine = DEFAULT_USE_STEP_ENGINE;

  public static UserPreferences load() {
    UserPreferences prefs = new UserPreferences();
    Preferences node = Preferences.userRoot().node(NODE);
    prefs.algorithmId = node.get(KEY_ALGORITHM, DEFAULT_ALGORITHM_ID);
    prefs.visualizationId = node.get(KEY_VISUALIZATION, DEFAULT_VISUALIZATION_ID);
    prefs.arraySize = clampSize(node.getInt(KEY_ARRAY_SIZE, DEFAULT_ARRAY_SIZE));
    prefs.speedLevel = clampSpeed(node.getInt(KEY_SPEED, DEFAULT_SPEED_LEVEL));
    prefs.muted = node.getBoolean(KEY_MUTED, false);
    if (node.getInt(KEY_DEFAULTS_VERSION, 0) < CURRENT_DEFAULTS_VERSION) {
      // The step engine used to default to true. Migrate existing installs once so sound returns to
      // legacy per-step timing; an explicit choice made after migration remains persistent.
      prefs.useStepEngine = DEFAULT_USE_STEP_ENGINE;
      node.putBoolean(KEY_STEP_ENGINE, prefs.useStepEngine);
      node.putInt(KEY_DEFAULTS_VERSION, CURRENT_DEFAULTS_VERSION);
    } else {
      prefs.useStepEngine = node.getBoolean(KEY_STEP_ENGINE, DEFAULT_USE_STEP_ENGINE);
    }
    return prefs;
  }

  public void save() {
    Preferences node = Preferences.userRoot().node(NODE);
    node.put(KEY_ALGORITHM, algorithmId);
    node.put(KEY_VISUALIZATION, visualizationId);
    node.putInt(KEY_ARRAY_SIZE, arraySize);
    node.putInt(KEY_SPEED, speedLevel);
    node.putBoolean(KEY_MUTED, muted);
    node.putBoolean(KEY_STEP_ENGINE, useStepEngine);
    node.putInt(KEY_DEFAULTS_VERSION, CURRENT_DEFAULTS_VERSION);
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

  public boolean isUseStepEngine() {
    return useStepEngine;
  }

  public void setUseStepEngine(boolean useStepEngine) {
    this.useStepEngine = useStepEngine;
  }

  private static int clampSize(int size) {
    return Math.max(3, Math.min(20_000, size));
  }

  private static int clampSpeed(int level) {
    return Math.max(1, Math.min(5, level));
  }
}
