package io.github.compilerstuck.control.ui.settingsfx;

/** UI strings for the JavaFX Settings shell (i18n-ready constants; English only for v1). */
public final class SettingsStrings {

  public static final String WINDOW_TITLE = "Sorting Algorithm Visualizer - Settings";
  public static final String TITLE = "Settings";
  public static final String LOADING = "Loading…";

  public static final String SECTION_ARRAY_SIZE = "ARRAY SIZE";
  public static final String SECTION_SORTING = "SORTING";
  public static final String SECTION_SPEED = "SPEED";
  public static final String SECTION_VISUALIZATION = "VISUALIZATION";
  public static final String SECTION_APPEARANCE = "APPEARANCE";
  public static final String SECTION_OPTIONS = "OPTIONS";

  public static final String OPTIONS_GROUP_DISPLAY = "Display";
  public static final String OPTIONS_GROUP_AUDIO = "Audio";
  public static final String OPTIONS_GROUP_DEBUG = "Debug";

  public static final String ALGORITHM = "Algorithm";
  public static final String SIZE = "Size";
  public static final String ARRAY_SIZE_FPS_WARNING =
      "Preview is below 24 FPS at this size.\nSorting will be even heavier, consider fewer elements.";
  public static final String ARRAY_SIZE_HIGH_WARNING =
      "Large array sizes can lag the preview and sorting.\nConsider staying at 20,000 or fewer if performance matters.";
  public static final String ARRAY_SIZE_WHOLE_NUMBER = "Enter a whole number";
  public static final String SPEED_HEADER = "Speed";
  public static final String SPEED_DURATION_HEADER = "Target duration";
  public static final String PRESET = "Preset";
  public static final String IMAGE = "Image";
  public static final String COLORS = "Colors";
  public static final String SPEED_STEPS_VALUE_FORMAT = "%d steps/frame";
  public static final String SPEED_DURATION_VALUE_FORMAT = "~%d s";

  public static final String RUN = "Run";
  public static final String CANCEL = "Cancel";
  public static final String CLOSE = "Close";
  public static final String APPLY = "Apply";
  public static final String CUSTOMIZE = "Customize";
  public static final String CUSTOMIZE_TITLE = "Customize visualization";
  public static final String CUSTOMIZE_UNAVAILABLE_TOOLTIP =
      "No customization options for this visualization.";
  public static final String CUSTOMIZE_BUSY_TOOLTIP = "Unavailable while sorting.";
  public static final String RESET_ALL_VISUALS = "Reset all";
  public static final String RESET_ALL_VISUALS_TITLE = "Reset all customizations?";
  public static final String RESET_ALL_VISUALS_MESSAGE =
      "Reset every visualization's customization to its defaults? Saved settings will be cleared.";
  public static final String RESET_ALL_VISUALS_CONFIRM = "Reset all";
  public static final String RESET_ALL_VISUALS_TOOLTIP =
      "Clear customizations for every visualization.";
  public static final String RESET_ALL_VISUALS_BUSY_TOOLTIP = "Unavailable while sorting.";
  public static final String CONFIGURE_ORDER_UNAVAILABLE_TOOLTIP =
      "Enable Run all to configure the algorithm order.";
  public static final String CONFIGURE_ORDER_BUSY_TOOLTIP = "Unavailable while sorting.";
  public static final String RESET_ALL = "Reset all";
  public static final String RESET_SETTING = "Reset";
  public static final String IMPORT = "Import";
  public static final String EXPORT = "Export";
  public static final String CUSTOMIZE_EXPORT_COPIED = "Settings copied to clipboard.";
  public static final String CUSTOMIZE_IMPORT_TITLE = "Import settings";
  public static final String CUSTOMIZE_IMPORT_HINT =
      "Paste an exported visualization config below.";
  public static final String CUSTOMIZE_IMPORT_PLACEHOLDER =
      "{\"schemaVersion\":1,\"visualizationId\":\"…\",\"settings\":{…}}";
  public static final String CUSTOMIZE_IMPORT_PASTE = "Paste from clipboard";
  public static final String CUSTOMIZE_IMPORT_LOAD = "Import";
  public static final String CUSTOMIZE_IMPORT_SUCCESS = "Settings imported.";
  public static final String CUSTOMIZE_IMPORT_EMPTY = "Paste a config to import.";
  public static final String CUSTOMIZE_IMPORT_INVALID = "This is not a valid visualization config.";
  public static final String CUSTOMIZE_IMPORT_WRONG_VIZ =
      "Config is for a different visualization.";
  public static final String CUSTOMIZE_IMPORT_CLAMPED =
      "Settings imported. Some values were outside the allowed range and were clamped.";
  public static final String CUSTOMIZE_UNSAVED_TITLE = "Unsaved changes";
  public static final String CUSTOMIZE_UNSAVED_MESSAGE =
      "Save your changes before closing, or discard them to revert the live preview.";
  public static final String CUSTOMIZE_SAVE_AND_CLOSE = "Save and close";
  public static final String CUSTOMIZE_DISCARD = "Discard changes";
  public static final String CUSTOMIZE_KEEP_EDITING = "Keep editing";
  public static final String CUBE_SECTION_MOTION = "MOTION";
  public static final String CUBE_SECTION_APPEARANCE = "APPEARANCE";
  public static final String CUBE_SECTION_FRAME = "LAYOUT";
  public static final String CUBE_ROTATION_SPEED = "Rotation speed";
  public static final String CUBE_FILL_OPACITY = "Fill opacity";
  public static final String CUBE_SCENE_SCALE = "Scene scale";
  public static final String CUBE_WIREFRAME = "Wireframe";
  public static final String RESET_SETTING_TOOLTIP = "Reset to default";
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
  public static final String RUN_ALL_TOOLTIP =
      "Run the selected algorithms in order instead of a single algorithm.";
  public static final String SOUND_EFFECTS = "Sound effects";
  public static final String SOUND_EFFECTS_TOOLTIP =
      "Play pitch cues as the array is accessed during sorting.";
  public static final String SHOW_MEASUREMENTS = "Show measurements";
  public static final String SHOW_MEASUREMENTS_TOOLTIP =
      "Show comparison, swap, and write counters on the visualization.";
  public static final String SHOW_COMPARISON_TABLE = "Show comparison table when finished";
  public static final String SHOW_COMPARISON_TABLE_TOOLTIP =
      "Overlay a results table after sorting finishes.";
  public static final String FIVE_SECOND_START_DELAY = "5 second start delay";
  public static final String FIVE_SECOND_START_DELAY_TOOLTIP =
      "Pause before the shuffle so you can switch to the visualization window.";
  public static final String EQUALIZE_SORT_DURATION = "Equalize sort duration";
  public static final String EQUALIZE_SORT_DURATION_TOOLTIP =
      "Pace each sort to the speed slider's target time. Frame-synced steps (e.g. Gravity Sort) may take longer.";
  public static final String SHOW_PERF_STATS = "Show render performance stats";
  public static final String SHOW_PERF_STATS_TOOLTIP =
      "Show FPS and render timing on the visualization (available while sorting).";
  public static final String SHUFFLE = "Shuffle";
  public static final String SWATCH_HINT = "Click a swatch to edit";
  public static final String SPEED_SLOW = "Slow";
  public static final String SPEED_DEFAULT = "Default";
  public static final String SPEED_FAST = "Fast";
  public static final String SPEED_EQUALIZE_SLOW = "Longer";
  public static final String SPEED_EQUALIZE_FAST = "Faster";
  public static final String IMAGE_PATH_PROMPT = "Image path";

  private SettingsStrings() {}
}
