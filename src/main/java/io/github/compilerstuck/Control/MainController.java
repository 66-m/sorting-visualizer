package io.github.compilerstuck.Control;

import com.formdev.flatlaf.FlatDarculaLaf;
import io.github.compilerstuck.SortingAlgorithms.QuickSortMiddlePivot;
import io.github.compilerstuck.SortingAlgorithms.SortingAlgorithm;
import io.github.compilerstuck.Sound.MidiSys;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Bars;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import javafx.util.Pair;
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
    private final ArrayList<Integer> timestamps = new ArrayList<>();

    private static boolean start = false;
    private static boolean running = false;
    //private final boolean[] keys = new boolean[3];
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
    private static boolean portrait = false;


    public static void main(String[] passedArgs) {
        //Changing UI Theme
        FlatDarculaLaf.setup();
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }


        fullScreen = passedArgs.length != 0 && passedArgs[0].equalsIgnoreCase("fullscreen");
        portrait = passedArgs.length != 0 && passedArgs[0].equalsIgnoreCase("portrait");

        String[] appletArgs = new String[]{"io.github.compilerstuck.Control.MainController"};
        PApplet.main(concat(appletArgs, passedArgs));
    }

    @Override
    public void settings() {
        if (fullScreen) {
            fullScreen(P3D);
        }
        else if(portrait){
            this.size(576 , 1024 , P3D);
        }
        else {
            this.size(1280, 720, P3D);
        }
        noSmooth();
    }


    @Override
    public void setup() {
        if (!fullScreen) {
            surface.setLocation(605, 10);
            surface.setResizable(false);
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
        visualization = new Bars(arrayController, colorGradient, sound); //Standard visual

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

                printTimestampsToConsole();

                arrayController.resetMeasurements();
                comparisons.clear();
                realTime.clear();
                swaps.clear();
                writesMain.clear();
                writesAux.clear();
                timestamps.clear();
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

            int startTime = (int) (System.currentTimeMillis() / 1000L);

            for (SortingAlgorithm algorithm : algorithms) {
                if (!SortingAlgorithm.isRun()) {
                    break;
                }
                
                sound.mute(true);
                sound.mute(false);

                timestamps.add((int) (System.currentTimeMillis() / 1000L) - startTime);

                arrayController.shuffle();
                if (!SortingAlgorithm.isRun()) {
                    break;
                }
                sound.mute(true);
                delay(500);
                sound.mute(false);
                arrayController.resetMeasurements();

                //start sorting
                algorithm.sort();
                if (!SortingAlgorithm.isRun()) {
                    break;
                }
                comparisons.add(Long.toString(arrayController.getComparisons()));
                realTime.add(Double.toString(arrayController.getRealTime()));
                swaps.add(Long.toString(arrayController.getSwaps()));
                writesMain.add(Long.toString(arrayController.getWrites()));
                writesAux.add(Long.toString(arrayController.getWritesAux()));
                sound.mute(true);
                delay(1500);
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

    public void printTimestampsToConsole() {
        System.out.println("\n\nTimestamps:\n");
        for (int i = 0; i < algorithms.size(); i++) {
            int minutes = timestamps.get(i) / 60;
            int seconds = timestamps.get(i) % 60;
            String time = String.format("%02d:%02d", minutes, seconds);
            System.out.println(time + " " + algorithms.get(i).getName());
        }
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
            text(String.format("%,d", (Long.parseLong(comparisons.get(i)))), (int) (width / 7. * 2) + 10, (int) (10 + 50 + (height - 50.) / algorithms.size() * (i) + (height - 50.) / algorithms.size() / 2));
            text("~" + String.valueOf(Math.floor(Double.parseDouble(realTime.get(i)) / 10000.) / 100).replace(".", ",") + "ms", (int) (width / 7. * 3) + 10, (int) (10 + 50 + (height - 50.) / algorithms.size() * (i) + (height - 50.) / algorithms.size() / 2));
            text(String.format("%,d", Long.parseLong(swaps.get(i))), (int) (width / 7. * 4) + 10, (int) (10 + 50 + (height - 50.) / algorithms.size() * (i) + (height - 50.) / algorithms.size() / 2));
            text(String.format("%,d", Long.parseLong(writesMain.get(i))), (int) (width / 7. * 5) + 10, (int) (10 + 50 + (height - 50.) / algorithms.size() * (i) + (height - 50.) / algorithms.size() / 2));
            text(String.format("%,d", Long.parseLong(writesAux.get(i))), (int) (width / 7. * 6) + 10, (int) (10 + 50 + (height - 50.) / algorithms.size() * (i) + (height - 50.) / algorithms.size() / 2));
        }
    }

    private void printMeasurements() {
        stroke(255);
        fill(255);

        int textSize = (int) (23. / 1280 * width);
        int textXPosition = (int) (5. / 1280 * width);
        int lineHeight = (int) (20. / 1280 * width);
        textSize(textSize);

        String[] labels = {
                currentOperation,
                (int) (arrayController.getSortedPercentage() * 100) + "% Sorted (" + arrayController.getSegments() + " Segments)",
                String.format("%,d", arrayController.getComparisons()) + " Comparisons",
                "Estimated time: ~" + String.valueOf(Math.floor(arrayController.getRealTime() / 10000.) / 100).replace(".", ",") + "ms",
                String.format("%,d",  arrayController.getSwaps()) + " Swaps",
                String.format("%,d", arrayController.getWrites()) + " Writes to main array",
                String.format("%,d", arrayController.getWritesAux()) + " Writes to auxiliary array",
                arrayController.getLength() + " Elements"
        };

        for (int i = 0; i < labels.length; i++) {
            text(labels[i], textXPosition, lineHeight * (i + 1));
        }
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
        MainController.algorithms.clear();
        for (SortingAlgorithm alg : algorithms){
            if (alg.isSelected()){
                MainController.algorithms.add(alg);
            }
        }

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