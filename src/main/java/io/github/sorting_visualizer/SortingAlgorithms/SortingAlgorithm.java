package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import processing.core.PApplet;

public abstract class SortingAlgorithm {
    protected PApplet proc;
    protected String name;
    protected boolean delay;
    protected int alternativeSize;
    protected static boolean run = true;


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

}
