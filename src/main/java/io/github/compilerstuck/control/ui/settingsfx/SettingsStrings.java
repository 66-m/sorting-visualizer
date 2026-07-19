package io.github.compilerstuck.control.ui.settingsfx;

/** UI strings for the JavaFX Settings shell (i18n-ready constants; English only for v1). */
public final class SettingsStrings {

  public static final String WINDOW_TITLE = "Sorting Algorithm Visualizer - Settings";
  public static final String TITLE = "Settings";
  public static final String LOADING = "Loading..";

  public static final String SECTION_ARRAY_SIZE = "ARRAY SIZE";
  public static final String SECTION_SORTING = "SORTING";
  public static final String SECTION_SPEED = "SPEED";
  public static final String SECTION_VISUALIZATION = "VISUALIZATION";
  public static final String SECTION_APPEARANCE = "APPEARANCE";
  public static final String SECTION_DISPLAY = "DISPLAY";
  public static final String SECTION_SOUND = "SOUND";
  public static final String SECTION_DEBUG = "DEBUG";

  public static final String ALGORITHM = "Algorithm";
  public static final String SIZE = "Size";
  public static final String ARRAY_SIZE_FPS_WARNING =
      "Preview is below 24 FPS at this size.\nSorting will be even heavier, consider fewer elements.";
  public static final String ARRAY_SIZE_HIGH_WARNING =
      "Large array sizes can lag the preview and sorting.\nConsider staying at 20,000 or fewer if performance matters.";
  public static final String LEVEL = "Level";
  public static final String PRESET = "Preset";
  public static final String IMAGE = "Image";
  public static final String COLORS = "Colors";
  public static final String SPEED_LEVEL_FORMAT = "%d / %d";

  public static final String RUN = "Run";
  public static final String CANCEL = "Cancel";
  public static final String CLOSE = "Close";
  public static final String APPLY = "Apply";
  public static final String CUSTOMIZE = "Customize";
  public static final String CUSTOMIZE_TITLE = "Customize visualization";
  public static final String RESET_ALL = "Reset all";
  public static final String RESET_SETTING = "Reset";
  public static final String IMPORT = "Import";
  public static final String EXPORT = "Export";
  public static final String CUSTOMIZE_EXPORT_COPIED =
      "Settings copied to clipboard.";
  public static final String CUSTOMIZE_IMPORT_TITLE = "Import settings";
  public static final String CUSTOMIZE_IMPORT_HINT =
      "Paste an exported visualization config below.";
  public static final String CUSTOMIZE_IMPORT_PLACEHOLDER =
      "{\"schemaVersion\":1,\"visualizationId\":\"…\",\"settings\":{…}}";
  public static final String CUSTOMIZE_IMPORT_PASTE = "Paste from clipboard";
  public static final String CUSTOMIZE_IMPORT_LOAD = "Import";
  public static final String CUSTOMIZE_IMPORT_SUCCESS = "Settings imported.";
  public static final String CUSTOMIZE_IMPORT_EMPTY = "Paste a config to import.";
  public static final String CUSTOMIZE_IMPORT_INVALID =
      "This is not a valid visualization config.";
  public static final String CUSTOMIZE_IMPORT_WRONG_VIZ =
      "Config is for a different visualization.";
  public static final String CUSTOMIZE_IMPORT_CLAMPED =
      "Settings imported. Some values were outside the allowed range and were clamped.";
  public static final String CUSTOMIZE_DISCARD_TITLE = "Discard changes?";
  public static final String CUSTOMIZE_DISCARD_MESSAGE =
      "You have unsaved changes. Discard them and close? The live preview will revert.";
  public static final String CUSTOMIZE_DISCARD = "Discard";
  public static final String CUSTOMIZE_KEEP_EDITING = "Keep editing";
  public static final String CUBE_SECTION_MOTION = "MOTION";
  public static final String CUBE_SECTION_APPEARANCE = "APPEARANCE";
  public static final String CUBE_SECTION_FRAME = "FRAME";
  public static final String CUBE_ROTATION_SPEED = "Rotation speed";
  public static final String CUBE_FILL_OPACITY = "Fill opacity";
  public static final String CUBE_SCENE_SCALE = "Scene scale";
  public static final String CUBE_WIREFRAME = "Wireframe";
  public static final String RESET_SETTING_TOOLTIP = "Reset to default";
  public static final String EXPORT_CSV = "Export CSV…";
  public static final String BROWSE = "Browse…";
  public static final String CONFIGURE_ORDER = "Configure order…";
  public static final String RUN_ALL_ORDER_TITLE = "Configure run-all";
  public static final String RUN_ALL_ORDER_HINT =
      "Check algorithms to include, then drag the handle to set the order they will run.";
  public static final String RUN_ALL_ORDER_SECTION = "ALGORITHMS";
  public static final String RUN_ALL_ORDER_COUNT = "%d selected";
  public static final String RUN_ALL_ORDER_COUNT_ONE = "1 selected";
  public static final String RUN_ALL_ORDER_EMPTY = "Select at least one algorithm to run.";
  public static final String RUN_ALL_ORDER_SKIPPED = "-";
  public static final String RUN_ALL_ORDER_DRAG_TOOLTIP = "Drag to reorder";
  public static final String DONE = "Done";
  public static final String SELECT_ALL = "Select all";
  public static final String CLEAR_SELECTION = "Clear";
  public static final String DRAG_HANDLE = "⠿";
  public static final String RUN_ALL = "Run all";
  public static final String SOUND_EFFECTS = "Sound effects";
  public static final String SHOW_MEASUREMENTS = "Show measurements";
  public static final String SHOW_COMPARISON_TABLE = "Show comparison table";
  public static final String SHOW_PERF_STATS = "Show render performance stats";
  public static final String SHUFFLE = "Shuffle";
  public static final String SWATCH_HINT = "Click a swatch to edit";
  public static final String SPEED_SLOW = "Very Slow";
  public static final String SPEED_FAST = "Max Fast";
  public static final String IMAGE_PATH_PROMPT = "Image path";

  private SettingsStrings() {}
}
