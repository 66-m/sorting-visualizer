package io.github.compilerstuck.Control.config;

/**
 * Configuration constants for MainController.
 * Centralizes all magic numbers and configuration values for easier maintenance.
 */
public final class MainControllerConfig {
    private MainControllerConfig() {
        // Utility class
    }

    // Window dimensions
    public static final int STANDARD_WIDTH = 1280;
    public static final int STANDARD_HEIGHT = 720;
    public static final int PORTRAIT_WIDTH = 576;
    public static final int PORTRAIT_HEIGHT = 1024;
    public static final int WINDOW_X_POSITION = 605;
    public static final int WINDOW_Y_POSITION = 10;

    // Frame rate and rendering
    public static final int TARGET_FRAME_RATE = 1000;
    public static final int MAX_TEXT_SIZE = 50;

    // Timing (milliseconds)
    public static final int DELAY_BETWEEN_ALGORITHMS = 500;
    public static final int DELAY_AFTER_SORT_RESULT = 1500;
    public static final int SETUP_DELAY = 500;

    // Visualization
    public static final int RESULTS_TABLE_BACKGROUND = 15;
    public static final int RESULTS_TABLE_TEXT_COLOR = 255;
    public static final int FONT_SIZE_RATIO = 20;
    public static final int WINDOW_RATIO_WIDTH = 1280;

    // Text positioning
    public static final float TEXT_X_OFFSET = 5.0f;
    public static final float TEXT_Y_OFFSET = 23.0f;
    public static final float LINE_HEIGHT_OFFSET = 20.0f;
    public static final float TABLE_COLUMN_WIDTH_RATIO = 1.0f / 7.0f;
    public static final float TABLE_TOP_ROW = 50.0f;

    // Default configuration
    public static final int DEFAULT_ARRAY_SIZE = 1280;
}
