package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayModel;
import io.github.compilerstuck.Control.DelayStrategy;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Control.ProcessingContext;
import io.github.compilerstuck.Visual.Marker;


public abstract class SortingAlgorithm {
    protected ProcessingContext proc;
    protected String name;
    protected boolean delay;
    protected int delayTime = 1; //ms
    protected int alternativeSize;
    protected static boolean run = true;
    protected boolean selected = true;
    protected long startTime;
    protected double delayFactor = 1.;
    private DelayStrategy delayStrategy = DelayStrategy.DEFAULT;


    ArrayModel arrayController;

    public SortingAlgorithm(ArrayModel arrayController) {
        this(arrayController, MainController.processing);
    }

    public SortingAlgorithm(ArrayModel arrayController, ProcessingContext proc) {
        this.proc = proc;
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

    public void setDelayFactor(double delayFactor) {
        this.delayFactor = delayFactor;
    }

    public void setDelayStrategy(DelayStrategy delayStrategy) {
        this.delayStrategy = delayStrategy;
    }

    public void delay(int[] markers) {
        if (delay && delayStrategy.shouldDelay(arrayController.getLength(), delayFactor)) {
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
