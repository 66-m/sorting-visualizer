package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import processing.core.PApplet;
import io.github.compilerstuck.Visual.Marker;


public abstract class SortingAlgorithm {
    protected PApplet proc;
    protected String name;
    protected boolean delay;
    protected int delayTime = 1; //ms
    protected int alternativeSize;
    protected static boolean run = true;
    protected boolean selected = true;
    protected long startTime;
    protected double delayFactor = 1.;
    private double elementsDelayThreshold = 2000;


    ArrayController arrayController;

    public SortingAlgorithm(ArrayController arrayController) {
        proc = MainController.processing;
        this.arrayController = arrayController;
        delay = true;
    }

    public abstract void sort();

    public void setDelay(boolean delay) {
        this.delay = delay;
    }

    public String getName() {
        return name;
    }

    public void setAlternativeSize(int alternativeSize) {
        this.alternativeSize = alternativeSize;
    }

    public int getAlternativeSize() {
        return alternativeSize;
    }


    public static boolean isRun() {
        return run;
    }

    public static void setRun(boolean run) {
        SortingAlgorithm.run = run;
    }

    public void setSelected(boolean selected){this.selected = selected;}

    public boolean isSelected(){return selected;}

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public void delay(int[] markers) {
        // Delay if: delay is enabled, the array is small enough or a random number is smaller than the probability of delaying, and the delay factor is 1 or a random number is smaller than the delay factor
        if (delay && (arrayController.getLength() <= elementsDelayThreshold || Math.random() < elementsDelayThreshold / arrayController.getLength()) && (delayFactor == 1 || Math.random() < delayFactor)) {
            arrayController.addRealTime(System.nanoTime() - startTime);

            for (int i : markers) {
                arrayController.setMarker(i, Marker.SET);
            }

            proc.delay(delayTime);
            startTime = System.nanoTime();
        }
    }

    public void delay(){
        delay(new int[0]);
    }


}
