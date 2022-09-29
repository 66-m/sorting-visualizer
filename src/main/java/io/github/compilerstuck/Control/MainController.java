package io.github.compilerstuck.Control;

import com.formdev.flatlaf.FlatDarculaLaf;
import io.github.compilerstuck.SortingAlgorithms.QuickSortMiddlePivot;
import io.github.compilerstuck.SortingAlgorithms.SortingAlgorithm;
import io.github.compilerstuck.Sound.MidiSys;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Classic;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import io.github.compilerstuck.Visual.Visualization;
import processing.core.PApplet;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainController extends PApplet {


    public static PApplet processing;

    private static int size;

    private static ArrayController arrayController;
    private static ArrayList<SortingAlgorithm> algorithms;

    private final ArrayList<String> comparisons = new ArrayList<>();
    private final ArrayList<String> realTime = new ArrayList<>();
    private final ArrayList<String> swaps = new ArrayList<>();
    private final ArrayList<String> writesMain = new ArrayList<>();
    private final ArrayList<String> writesAux = new ArrayList<>();

    private static boolean start = false;
    private static boolean running = false;
    private final boolean[] keys = new boolean[3];
    private boolean results = false;
    private static boolean showComparisonTable = false;
    private static boolean printMeasurements = true;
    private boolean restart = false;
    private static String currentOperation = "Waiting";

    private static Visualization visualization;
    public static Sound sound;
    private static ColorGradient colorGradient;

    private Settings settings;

    private static boolean fullScreen = false;


    public static void main(String[] passedArgs) {
        //Changing UI Theme
        FlatDarculaLaf.setup();
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }


        fullScreen = passedArgs.length != 0 && passedArgs[0].equalsIgnoreCase("fs");

        String[] appletArgs = new String[]{"io.github.compilerstuck.Control.MainController"};
        PApplet.main(concat(appletArgs, passedArgs));
    }

    @Override
    public void settings() {
        if (fullScreen) {
            fullScreen(P2D);
        } else {
            this.size(1280, 720, P2D);
        }
        smooth(8);
    }


    @Override
    public void setup() {
        if (!fullScreen) {
            surface.setLocation(555, 10);
            surface.setResizable(true);
        } else {
            surface.setLocation(0, 0);
        }

        surface.setTitle("Sorting Algorithm Visualizer");
        frameRate(1000);
        textSize(50);//Setting max text size - due to processing bug
        processing = this;

        size = 1280; //Standard size
        arrayController = new ArrayController(size); //Initialize ArrayController with the standard size

        //Standard Sound
        try {
            sound = new MidiSys(arrayController);
        } catch (MidiUnavailableException e) {
            System.out.println("Sound could not be loaded");
        }

        colorGradient = new ColorGradient(Color.BLACK, Color.RED, Color.WHITE, "Black -> Red"); //Standard gradient
        visualization = new Classic(arrayController, colorGradient, sound); //Standard visual

        algorithms = new ArrayList<>();
        algorithms.add(new QuickSortMiddlePivot(arrayController));

        try {
            settings = new Settings();
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw() {
        try {
            if (results && showComparisonTable && SortingAlgorithm.isRun()) {
                printResults();
            } else if (restart) {
                running = false;
                start = false;
                restart = false;
                results = false;

                sound.mute(true);
                sound.mute(false);

                arrayController.resetMeasurements();
                comparisons.clear();
                realTime.clear();
                swaps.clear();
                writesMain.clear();
                writesAux.clear();
                setCurrentOperation("Waiting");

                SortingAlgorithm.setRun(true);
                arrayController.resetArray();

                settings.setProgressBar(100);
                settings.setEnableInputs(true);
                settings.setEnableCancelButton(false);
            } else if (running) {
                visualization.update();
                arrayController.update();
                if (printMeasurements) printMeasurements();
                settings.setProgressBar((int) (arrayController.getSortedPercentage() * 100));
            } else {
                if (start) {
                    if (printMeasurements) printMeasurements();
                    results = false;
                    start = false;
                    delay(500);
                    running = true;
                    settings.setEnableInputs(false);
                    arrayController.resetArray();
                    startAlgorithmThread();
                } else {
                    visualization.update();
                    if (printMeasurements) printMeasurements();
                }
            }
            if (exitCalled()){
                shutdown();
            }
        } catch (Exception ignored) {}
    }

    private void startAlgorithmThread() {
        new Thread(() -> {
            for (SortingAlgorithm algorithm : algorithms) {
                if (!SortingAlgorithm.isRun()) {
                    break;
                }
//                if (alg.alternativeSize != arrayController.getLength()) {
//                    sound.mute(true);
//                    colorGradient.updateGradient(alg.alternativeSize);
//                    arrayController.resize(alg.alternativeSize);
//
//                    delay(1000);
//                    sound.mute(false);
//                }
                sound.mute(true);
                sound.mute(false);


                arrayController.shuffle();
                if (!SortingAlgorithm.isRun()) {
                    break;
                }
                sound.mute(true);
                delay(1000);
                sound.mute(false);
                arrayController.resetMeasurements();

                //start sorting
                algorithm.sort();
                if (!SortingAlgorithm.isRun()) {
                    break;
                }
                comparisons.add(Double.toString(arrayController.getComparisons()));
                realTime.add(Double.toString(arrayController.getRealTime()));
                swaps.add(Double.toString(arrayController.getSwaps()));
                writesMain.add(Double.toString(arrayController.getWrites()));
                writesAux.add(Double.toString(arrayController.getWritesAux()));
                sound.mute(true);
                delay(2000);
                sound.mute(false);
                arrayController.resetMeasurements();
            }
            if (showComparisonTable && SortingAlgorithm.isRun()) {
                results = true;
            }
            restart = true;
            running = false;

        }).start();
    }

    @Override
    public void keyPressed() {
        if (keyCode == ESC) {
            sound.mute(true);
            shutdown();
        }
    }

    public static void shutdown(){
        SortingAlgorithm.setRun(false);
        processing.noLoop();
        processing.exit();
    }

    private void printResults() {
        textSize((int) (20. / 1280 * width));
        background(15);
        fill(255);
        stroke(255);
        line((int) ((width / 7.) + (width / 7.) / 2), 0, (int) ((width / 7.) + (width / 7.) / 2), height);
        for (int i = 2; i < 7; i++) {
            line((int) (width / 7. * i), 0, (int) (width / 7. * i), height);
        }
        text("Alg. name", (int) (width / 7. * 0) + 10, (int) (20. / 1280 * width));
        text("Elements", (int) (width / 7. * 1 + (width / 7.) / 2) + 5, (int) (20. / 1280 * width));
        text("Comparisons", (int) (width / 7. * 2) + 10, (int) (20. / 1280 * width));
        text("Est. time", (int) (width / 7. * 3) + 10, (int) (20. / 1280 * width));
        text("Swaps", (int) (width / 7. * 4) + 10, (int) (20. / 1280 * width));
        text("Writes main", (int) (width / 7. * 5) + 10, (int) (20. / 1280 * width));
        text("Writes aux", (int) (width / 7. * 6) + 10, (int) (20. / 1280 * width));

        for (int i = 0; i < algorithms.size(); i++) {
            line(0, (int) (50 + (height - 50.) / algorithms.size() * (i)), width, (int) (50 + (height - 50.) / algorithms.size() * i));
            text(algorithms.get(i).getName(), (int) (width / 7. * 0) + 10, (int) (10 + 50 + (height - 50.) / algorithms.size() * (i) + (height - 50.) / algorithms.size() / 2));
            text(algorithms.get(i).getAlternativeSize(), (int) (width / 7. * 1 + (width / 7.) / 2) + 10, (int) (10 + 50 + (height - 50.) / algorithms.size() * (i) + (height - 50.) / algorithms.size() / 2));
            text(String.format("%,d", (int) (Double.parseDouble(comparisons.get(i)))), (int) (width / 7. * 2) + 10, (int) (10 + 50 + (height - 50.) / algorithms.size() * (i) + (height - 50.) / algorithms.size() / 2));
            text("~" + String.valueOf(Math.floor(Double.parseDouble(realTime.get(i)) / 10000.) / 100).replace(".", ",") + "ms", (int) (width / 7. * 3) + 10, (int) (10 + 50 + (height - 50.) / algorithms.size() * (i) + (height - 50.) / algorithms.size() / 2));
            text(String.format("%,d", (int) Double.parseDouble(swaps.get(i))), (int) (width / 7. * 4) + 10, (int) (10 + 50 + (height - 50.) / algorithms.size() * (i) + (height - 50.) / algorithms.size() / 2));
            text(String.format("%,d", (int) Double.parseDouble(writesMain.get(i))), (int) (width / 7. * 5) + 10, (int) (10 + 50 + (height - 50.) / algorithms.size() * (i) + (height - 50.) / algorithms.size() / 2));
            text(String.format("%,d", (int) Double.parseDouble(writesAux.get(i))), (int) (width / 7. * 6) + 10, (int) (10 + 50 + (height - 50.) / algorithms.size() * (i) + (height - 50.) / algorithms.size() / 2));
        }
    }

    private void printMeasurements() {
        stroke(255);
        fill(255);

        textSize((int) (23. / 1280 * width));
        text(currentOperation, (int) (5. / 1280 * width), (int) (20. / 1280 * width));
        text((int) (arrayController.getSortedPercentage() * 100) + "% Sorted (" + arrayController.getSegments() + " Segments" + ")", (int) (5. / 1280 * width), (int) (40. / 1280 * width));
        text(String.format("%,d", (int) arrayController.getComparisons()) + " Comparisons", (int) (5. / 1280 * width), (int) (60. / 1280 * width));
        text("Estimated time: ~" + String.valueOf(Math.floor(arrayController.getRealTime() / 10000.) / 100).replace(".", ",") + "ms", (int) (5. / 1280 * width), (int) (80. / 1280 * width));
        text(String.format("%,d", (int) arrayController.getSwaps()) + " Swaps", (int) (5. / 1280 * width), (int) (100. / 1280 * width));
        text(String.format("%,d", (int) arrayController.getWrites()) + " Writes to main array", (int) (5. / 1280 * width), (int) (120. / 1280 * width));
        text(String.format("%,d", (int) arrayController.getWritesAux()) + " Writes to auxiliary array", (int) (5. / 1280 * width), (int) (140. / 1280 * width));
        text(arrayController.getLength() + " Elements", (int) (5. / 1280 * width), (int) (160. / 1280 * width));
    }

    public static void setCurrentOperation(String operation) {
        currentOperation = operation;
    }


    public static void setColorGradient(ColorGradient newColorGradient) {
        colorGradient = newColorGradient;
        colorGradient.updateGradient(size);
        visualization.updateColorGradient(colorGradient);
    }

    public static void updateArraySize(int newSize) {
        size = newSize;
        colorGradient.updateGradient(size);
        visualization.updateColorGradient(colorGradient);
        for (SortingAlgorithm alg : algorithms) {
            if (alg.getAlternativeSize() == arrayController.getLength()) {
                alg.setAlternativeSize(newSize);
            }
        }
        arrayController.resize(size);
    }

    public static void setVisualization(Visualization visualization) {
        MainController.visualization = visualization;
        visualization.updateColorGradient(colorGradient);
    }

    public static int getSize() {
        return size;
    }

    public static void setSize(int size) {
        MainController.size = size;
    }

    public static ArrayController getArrayController() {
        return arrayController;
    }

    public static ArrayList<SortingAlgorithm> getAlgorithms() {
        return algorithms;
    }

    public static void setAlgorithms(ArrayList<SortingAlgorithm> algorithms) {
        MainController.algorithms = algorithms;
    }

    public static void setAlgorithm(SortingAlgorithm algorithm) {
        algorithms.clear();
        algorithms.add(algorithm);
    }

    public static void setStart(boolean start) {
        MainController.start = start;
    }

    public static boolean isRunning() {
        return running;
    }

    public static Sound getSound() {
        return sound;
    }

    public static void setSound(Sound sound) {
        MainController.sound = sound;
    }

    public static ColorGradient getColorGradient() {
        return colorGradient;
    }

    public static void setShowComparisonTable(boolean showComparisonTable) {
        MainController.showComparisonTable = showComparisonTable;
    }

    public static void setPrintMeasurements(boolean printMeasurements) {
        MainController.printMeasurements = printMeasurements;
    }
}