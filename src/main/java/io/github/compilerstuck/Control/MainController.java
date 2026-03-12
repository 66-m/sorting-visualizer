package io.github.compilerstuck.Control;

import com.formdev.flatlaf.FlatDarculaLaf;
import io.github.compilerstuck.SortingAlgorithms.QuickSortMiddlePivot;
import io.github.compilerstuck.SortingAlgorithms.SortingAlgorithm;
import io.github.compilerstuck.Sound.MidiSys;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Bars;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import io.github.compilerstuck.Visual.Visualization;
import processing.core.PApplet;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main controller for the sorting algorithm visualizer.
 * 
 * Manages the Processing display loop, user interaction, and coordinates between
 * visualization, sound, algorithm execution, and UI settings.
 * 
 * Note: This class maintains some static fields for backwards compatibility with
 * the Settings UI and algorithm infrastructure. Access to these is coordinated
 * through synchronized accessors.
 */
public class MainController extends PApplet implements RenderContext {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    // Static reference for backwards compatibility with Settings UI
    public static ProcessingContext processing;
    public static Sound sound;

    // Instance fields (preferred pattern)
    private int size;
    private ArrayController arrayController;
    private List<SortingAlgorithm> algorithms;
    private Visualization visualization;
    private ColorGradient colorGradient;
    private Settings settings;

    private SortingStateManager stateManager;
    private SortingSessionManager sessionManager;

    private boolean fullScreen = false;
    private boolean portrait = false;

    /**
     * Entry point for the sorting visualizer application.
     * 
     * @param passedArgs command line arguments: "fullscreen" or "portrait"
     */
    public static void main(String[] passedArgs) {
        setupUITheme();

        String[] appletArgs = new String[]{"io.github.compilerstuck.Control.MainController"};
        PApplet.main(concat(appletArgs, passedArgs));
    }

    /**
     * Configures the application's UI theme using FlatLaf dark theme.
     */
    private static void setupUITheme() {
        FlatDarculaLaf.setup();
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            LOGGER.log(Level.WARNING, "Failed to set FlatDarculaLaf look and feel", e);
        }
    }

    @Override
    public void delay(int ms) {
        super.delay(ms);
    }

    /**
     * Processing settings hook. Configures window size and rendering mode.
     */
    @Override
    public void settings() {
        if (fullScreen) {
            fullScreen(P3D);
        } else if (portrait) {
            this.size(MainControllerConfig.PORTRAIT_WIDTH, MainControllerConfig.PORTRAIT_HEIGHT, P3D);
        } else {
            this.size(MainControllerConfig.STANDARD_WIDTH, MainControllerConfig.STANDARD_HEIGHT, P3D);
        }
        noSmooth();
    }


    /**
     * Processing setup hook. Initializes all components including visualization,
     * sound, algorithms, and settings UI.
     */
    @Override
    public void setup() {
        configureWindow();
        
        processing = this; // Static reference for backwards compatibility
        
        initializeComponents();
        initializeState();
        
        try {
            settings = new Settings();
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | 
                 InstantiationException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Settings UI", e);
        }
    }

    /**
     * Configures window size, position, and title.
     */
    private void configureWindow() {
        if (!fullScreen) {
            surface.setLocation(MainControllerConfig.WINDOW_X_POSITION, 
                              MainControllerConfig.WINDOW_Y_POSITION);
            surface.setResizable(false);
        } else {
            surface.setLocation(0, 0);
        }

        surface.setTitle("Sorting Algorithm Visualizer");
        frameRate(MainControllerConfig.TARGET_FRAME_RATE);
        textSize(MainControllerConfig.MAX_TEXT_SIZE); // Processing workaround
    }

    /**
     * Initializes visualization, sound, color gradient, and algorithms.
     */
    private void initializeComponents() {
        size = MainControllerConfig.DEFAULT_ARRAY_SIZE;
        arrayController = new ArrayController(size);

        // Initialize sound system
        try {
            sound = new MidiSys(arrayController);
        } catch (MidiUnavailableException e) {
            LOGGER.log(Level.WARNING, "Sound system unavailable, running without audio", e);
            sound = null;
        }

        // Initialize visualization
        colorGradient = new ColorGradient(Color.BLACK, Color.RED, Color.WHITE, "Black -> Red");
        visualization = new Bars(arrayController, colorGradient, sound, this);

        // Initialize algorithms
        algorithms = new ArrayList<>();
        algorithms.add(new QuickSortMiddlePivot(arrayController));
    }

    /**
     * Initializes state managers for thread coordination.
     */
    private void initializeState() {
        stateManager = new SortingStateManager();
        sessionManager = new SortingSessionManager(arrayController, sound, stateManager);
    }

    /**
     * Processing draw hook. Called repeatedly to update and render the visualization.
     * Handles state transitions and coordinates between simulation and rendering.
     */
    @Override
    public void draw() {
        try {
            if (stateManager.shouldShowResults()) {
                handleResultsDisplay();
            } else if (stateManager.shouldRestart()) {
                handleRestart();
            } else if (stateManager.isRunning()) {
                handleActiveSort();
            } else {
                handleIdleState();
            }

            if (exitCalled()) {
                shutdown();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during draw loop", e);
        }
    }

    /**
     * Handles displaying results table after sorting completes.
     */
    private void handleResultsDisplay() {
        if (stateManager.shouldShowComparisonTable() && SortingAlgorithm.isRun()) {
            printResults();
        }
    }

    /**
     * Handles restart/reset state after algorithms complete.
     */
    private void handleRestart() {
        stateManager.setRunning(false);
        stateManager.setStartRequested(false);
        stateManager.setRestart(false);
        stateManager.setShowResults(false);

        if (sound != null) {
            sound.mute(true);
            sound.mute(false);
        }

        sessionManager.printTimestampsToConsole(new ArrayList<>(algorithms));
        arrayController.resetMeasurements();
        stateManager.setCurrentOperation("Waiting");

        SortingAlgorithm.setRun(true);
        arrayController.resetArray();

        if (settings != null) {
            settings.setProgressBar(100);
            settings.setEnableInputs(true);
            settings.setEnableCancelButton(false);
        }
    }

    /**
     * Handles rendering and updates during active sorting.
     */
    private void handleActiveSort() {
        visualization.update();
        arrayController.update();
        
        if (stateManager.shouldPrintMeasurements()) {
            printMeasurements();
        }
        
        if (settings != null) {
            settings.setProgressBar((int) (arrayController.getSortedPercentage() * 100));
        }
    }

    /**
     * Handles idle state or starting new sort session.
     */
    private void handleIdleState() {
        if (stateManager.requestedStart()) {
            startSortingSession();
        } else {
            visualization.update();
            if (stateManager.shouldPrintMeasurements()) {
                printMeasurements();
            }
        }
    }

    /**
     * Initiates a new sorting session.
     */
    private void startSortingSession() {
        if (stateManager.shouldPrintMeasurements()) {
            printMeasurements();
        }
        
        stateManager.setShowResults(false);
        
        try {
            Thread.sleep(MainControllerConfig.SETUP_DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Thread interrupted during setup delay", e);
        }
        
        stateManager.setRunning(true);
        
        if (settings != null) {
            settings.setEnableInputs(false);
        }
        
        arrayController.resetArray();
        sessionManager.startSortingSession(algorithms);
    }

    /**
     * Handles ESC key press to gracefully shutdown the application.
     */
    @Override
    public void keyPressed() {
        if (keyCode == ESC) {
            if (sound != null) {
                sound.mute(true);
            }
            shutdown();
        }
    }

    /**
     * Gracefully shuts down the application.
     * Sets the run flag and exits the Processing loop and JVM.
     */
    public static void shutdown() {
        SortingAlgorithm.setRun(false);
        
        if (processing instanceof PApplet p) {
            p.noLoop();
            p.exit();
        }
    }

    /**
     * Displays a table of algorithm comparison results on screen.
     * Shows metrics like comparisons, swaps, writes, and execution time.
     */
    private void printResults() {
        textSize((int) (MainControllerConfig.FONT_SIZE_RATIO / MainControllerConfig.WINDOW_RATIO_WIDTH * width));
        background(MainControllerConfig.RESULTS_TABLE_BACKGROUND);
        fill(MainControllerConfig.RESULTS_TABLE_TEXT_COLOR);
        stroke(MainControllerConfig.RESULTS_TABLE_TEXT_COLOR);

        drawResultsTableGrid();
        drawResultsTableHeaders();
        drawResultsTableData();
    }

    /**
     * Draws the grid lines for the results table.
     */
    private void drawResultsTableGrid() {
        float columnWidth = width * MainControllerConfig.TABLE_COLUMN_WIDTH_RATIO;
        
        line((int) (columnWidth + columnWidth / 2), 0, 
             (int) (columnWidth + columnWidth / 2), height);
        
        for (int i = 2; i < 7; i++) {
            line((int) (columnWidth * i), 0, (int) (columnWidth * i), height);
        }
    }

    /**
     * Draws the header row of the results table.
     */
    private void drawResultsTableHeaders() {
        float columnWidth = width * MainControllerConfig.TABLE_COLUMN_WIDTH_RATIO;
        int textY = (int) (MainControllerConfig.FONT_SIZE_RATIO / MainControllerConfig.WINDOW_RATIO_WIDTH * width);
        
        text("Alg. name", columnWidth * 0 + 10, textY);
        text("Elements", columnWidth * 1 + columnWidth / 2 + 5, textY);
        text("Comparisons", columnWidth * 2 + 10, textY);
        text("Est. time", columnWidth * 3 + 10, textY);
        text("Swaps", columnWidth * 4 + 10, textY);
        text("Writes main", columnWidth * 5 + 10, textY);
        text("Writes aux", columnWidth * 6 + 10, textY);
    }

    /**
     * Draws the data rows of the results table.
     */
    private void drawResultsTableData() {
        if (algorithms.isEmpty()) {
            return;
        }

        float columnWidth = width * MainControllerConfig.TABLE_COLUMN_WIDTH_RATIO;
        float rowHeight = (height - MainControllerConfig.TABLE_TOP_ROW) / algorithms.size();
        float textY = 20.0f / MainControllerConfig.WINDOW_RATIO_WIDTH * width;

        List<String> comparisons = sessionManager.getComparisons();
        List<String> realTime = sessionManager.getRealTime();
        List<String> swaps = sessionManager.getSwaps();
        List<String> writesMain = sessionManager.getWritesMain();
        List<String> writesAux = sessionManager.getWritesAux();

        for (int i = 0; i < algorithms.size(); i++) {
            float rowY = MainControllerConfig.TABLE_TOP_ROW + rowHeight * i;
            line(0, (int) rowY, width, (int) rowY);

            drawResultsRow(i, columnWidth, rowY, textY, 
                          comparisons, realTime, swaps, writesMain, writesAux);
        }
    }

    /**
     * Draws a single data row in the results table.
     */
    private void drawResultsRow(int index, float columnWidth, float rowY, float textY,
                               List<String> comparisons, List<String> realTime,
                               List<String> swaps, List<String> writesMain,
                               List<String> writesAux) {
        SortingAlgorithm alg = algorithms.get(index);
        float rowCenterY = rowY + 10 + (height - MainControllerConfig.TABLE_TOP_ROW) 
                          / algorithms.size() / 2;

        text(alg.getName(), columnWidth * 0 + 10, (int) rowCenterY);
        text(String.valueOf(alg.getAlternativeSize()), 
             (int) (columnWidth * 1 + columnWidth / 2) + 10, (int) rowCenterY);

        if (index < comparisons.size()) {
            text(String.format("%,d", Long.parseLong(comparisons.get(index))), 
                 (int) (columnWidth * 2) + 10, (int) rowCenterY);
        }

        if (index < realTime.size()) {
            String timeStr = "~" + formatTimeEstimate(Double.parseDouble(realTime.get(index))) + "ms";
            text(timeStr, (int) (columnWidth * 3) + 10, (int) rowCenterY);
        }

        if (index < swaps.size()) {
            text(String.format("%,d", Long.parseLong(swaps.get(index))), 
                 (int) (columnWidth * 4) + 10, (int) rowCenterY);
        }

        if (index < writesMain.size()) {
            text(String.format("%,d", Long.parseLong(writesMain.get(index))), 
                 (int) (columnWidth * 5) + 10, (int) rowCenterY);
        }

        if (index < writesAux.size()) {
            text(String.format("%,d", Long.parseLong(writesAux.get(index))), 
                 (int) (columnWidth * 6) + 10, (int) rowCenterY);
        }
    }

    /**
     * Formats time estimate from raw measurement to readable format.
     */
    private String formatTimeEstimate(double rawTime) {
        double ms = Math.floor(rawTime / 10000.0) / 100;
        return String.valueOf(ms).replace(".", ",");
    }

    /**
     * Prints live measurements during sorting (comparisons, swaps, time, etc).
     */
    private void printMeasurements() {
        stroke(255);
        fill(255);

        int textSize = (int) (MainControllerConfig.TEXT_Y_OFFSET / MainControllerConfig.WINDOW_RATIO_WIDTH * width);
        int textXPosition = (int) (MainControllerConfig.TEXT_X_OFFSET / MainControllerConfig.WINDOW_RATIO_WIDTH * width);
        int lineHeight = (int) (MainControllerConfig.LINE_HEIGHT_OFFSET / MainControllerConfig.WINDOW_RATIO_WIDTH * width);
        textSize(textSize);

        String[] labels = {
                stateManager.getCurrentOperation(),
                (int) (arrayController.getSortedPercentage() * 100) + "% Sorted (" + arrayController.getSegments() + " Segments)",
                String.format("%,d", arrayController.getComparisons()) + " Comparisons",
                "Estimated time: ~" + formatTimeEstimate(arrayController.getRealTime()) + "ms",
                String.format("%,d", arrayController.getSwaps()) + " Swaps",
                String.format("%,d", arrayController.getWrites()) + " Writes to main array",
                String.format("%,d", arrayController.getWritesAux()) + " Writes to auxiliary array",
                arrayController.getLength() + " Elements"
        };

        for (int i = 0; i < labels.length; i++) {
            text(labels[i], textXPosition, lineHeight * (i + 1));
        }
    }

    /**
     * Sets the current operation name for display.
     * @param operation the operation name to display
     */
    public static void setCurrentOperation(String operation) {
        if (processing instanceof MainController controller) {
            controller.stateManager.setCurrentOperation(operation);
        }
    }

    // Static accessor methods for backwards compatibility with Settings UI
    // These delegate to instance state or maintain static references

    /**
     * Sets the color gradient for all visualizations.
     * @param newColorGradient the new color gradient to apply
     */
    public static void setColorGradient(ColorGradient newColorGradient) {
        if (processing instanceof MainController controller) {
            controller.colorGradient = newColorGradient;
            controller.colorGradient.updateGradient(controller.size);
            controller.visualization.updateColorGradient(newColorGradient);
        }
    }

    /**
     * Updates the array size and resizes all related components.
     * @param newSize the new array size
     */
    public static void updateArraySize(int newSize) {
        if (processing instanceof MainController controller) {
            controller.size = newSize;
            controller.colorGradient.updateGradient(newSize);
            controller.visualization.updateColorGradient(controller.colorGradient);
            
            for (SortingAlgorithm alg : controller.algorithms) {
                if (alg.getAlternativeSize() == controller.arrayController.getLength()) {
                    alg.setAlternativeSize(newSize);
                }
            }
            controller.arrayController.resize(newSize);
        }
    }

    /**
     * Sets the visualization implementation.
     * @param viz the new visualization to use
     */
    public static void setVisualization(Visualization viz) {
        if (processing instanceof MainController controller) {
            controller.visualization = viz;
            viz.updateColorGradient(controller.colorGradient);
        }
    }

    /**
     * Gets the current array size.
     * @return the array size
     */
    public static int getSize() {
        if (processing instanceof MainController controller) {
            return controller.size;
        }
        return 0;
    }

    /**
     * Sets the array size.
     * @param newSize the new array size
     */
    public static void setSize(int newSize) {
        if (processing instanceof MainController controller) {
            controller.size = newSize;
        }
    }

    /**
     * Directfieldaccess to array controller.
     * @return the array controller instance
     */
    public static ArrayController getArrayController() {
        if (processing instanceof MainController controller) {
            return controller.arrayController;
        }
        return null;
    }

    /**
     * Gets the list of registered algorithms.
     * @return the algorithms list
     */
    public static ArrayList<SortingAlgorithm> getAlgorithms() {
        if (processing instanceof MainController controller) {
            return new ArrayList<>(controller.algorithms);
        }
        return new ArrayList<>();
    }

    /**
     * Sets which algorithms to run (selects only those marked as selected).
     * @param algorithmList list of algorithms to consider
     */
    public static void setAlgorithms(ArrayList<SortingAlgorithm> algorithmList) {
        if (processing instanceof MainController controller) {
            controller.algorithms.clear();
            for (SortingAlgorithm alg : algorithmList) {
                if (alg.isSelected()) {
                    controller.algorithms.add(alg);
                }
            }
        }
    }

    /**
     * Sets a single algorithm to run.
     * @param algorithm the algorithm to run
     */
    public static void setAlgorithm(SortingAlgorithm algorithm) {
        if (processing instanceof MainController controller) {
            controller.algorithms.clear();
            controller.algorithms.add(algorithm);
        }
    }

    /**
     * Requests to start the sorting process.
     * @param shouldStart true to request start
     */
    public static void setStart(boolean shouldStart) {
        if (processing instanceof MainController controller) {
            controller.stateManager.setStartRequested(shouldStart);
        }
    }

    /**
     * Checks if sorting is currently running.
     * @return true if active
     */
    public static boolean isRunning() {
        if (processing instanceof MainController controller) {
            return controller.stateManager.isRunning();
        }
        return false;
    }

    /**
     * Gets sound system.
     * @return the sound instance
     */
    public static Sound getSound() {
        return sound;
    }

    /**
     * Sets the sound instance.
     * @param soundSystem the sound to use
     */
    public static void setSound(Sound soundSystem) {
        sound = soundSystem;
    }

    /**
     * Gets current color gradient.
     * @return the color gradient
     */
    public static ColorGradient getColorGradient() {
        if (processing instanceof MainController controller) {
            return controller.colorGradient;
        }
        return null;
    }

    /**
     * Sets whether to show the comparison table.
     * @param show true to show table
     */
    public static void setShowComparisonTable(boolean show) {
        if (processing instanceof MainController controller) {
            controller.stateManager.setShowComparisonTable(show);
        }
    }

    /**
     * Sets whether to print on-screen measurements.
     * @param print true to print
     */
    public static void setPrintMeasurements(boolean print) {
        if (processing instanceof MainController controller) {
            controller.stateManager.setPrintMeasurements(print);
        }
    }

    /**
     * Sets the animation delay factor for all algorithms.
     * A value of 1.0 means every step fires a delay; lower values reduce frame rate.
     * @param factor the delay factor (0 < factor <= 1)
     */
    public static void setDelayFactor(double factor) {
        if (processing instanceof MainController controller) {
            for (SortingAlgorithm alg : controller.algorithms) {
                alg.setDelayFactor(factor);
            }
        }
    }

    /**
     * Sets the animation delay time in milliseconds for all algorithms.
     * @param ms the delay in milliseconds
     */
    public static void setDelayTime(int ms) {
        if (processing instanceof MainController controller) {
            for (SortingAlgorithm alg : controller.algorithms) {
                alg.setDelayTime(ms);
            }
        }
    }

    // RenderContext implementation - delegates to Processing PApplet methods
    
    @Override
    public void background(int rgb) {
        super.background(rgb);
    }

    @Override
    public void fill(int rgb) {
        super.fill(rgb);
    }

    @Override
    public void textSize(int size) {
        super.textSize(size);
    }

    @Override
    public void text(String str, float x, float y) {
        super.text(str, x, y);
    }

    @Override
    public void stroke(int rgb) {
        super.stroke(rgb);
    }

    @Override
    public void rect(float x, float y, float w, float h) {
        super.rect(x, y, w, h);
    }

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        super.line(x1, y1, x2, y2);
    }

    @Override
    public void ellipse(float x, float y, float w, float h) {
        super.ellipse(x, y, w, h);
    }

    @Override
    public int getWidth() {
        return super.width;
    }

    @Override
    public int getHeight() {
        return super.height;
    }
}