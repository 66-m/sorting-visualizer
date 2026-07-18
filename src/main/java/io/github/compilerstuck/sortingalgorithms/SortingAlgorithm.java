package io.github.compilerstuck.sortingalgorithms;

import io.github.compilerstuck.control.config.DelayStrategy;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.model.CancellationToken;
import io.github.compilerstuck.control.model.OperationReporter;
import io.github.compilerstuck.control.render.ProcessingContext;
import io.github.compilerstuck.visual.Marker;


public abstract class SortingAlgorithm {
    protected ProcessingContext proc;
    protected String name;
    protected boolean delay;
    protected int delayTime = 1; //ms
    protected int alternativeSize;
    protected boolean selected = true;
    protected long startTime;
    protected double delayFactor = 1.;
    private DelayStrategy delayStrategy = DelayStrategy.DEFAULT;
    protected OperationReporter operationReporter = OperationReporter.NOOP;
    protected CancellationToken cancellationToken = CancellationToken.alwaysActive();


    ArrayModel arrayController;

    public SortingAlgorithm(ArrayModel arrayController) {
        this(arrayController, ms -> { /* no-op delay */ });
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

    public void setProcessingContext(ProcessingContext proc) {
        this.proc = proc != null ? proc : ms -> { /* no-op */ };
    }

    public void setOperationReporter(OperationReporter operationReporter) {
        this.operationReporter = operationReporter != null ? operationReporter : OperationReporter.NOOP;
    }

    protected void report(String operation) {
        operationReporter.report(operation);
    }

    public void setCancellationToken(CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken != null
                ? cancellationToken
                : CancellationToken.alwaysActive();
    }

    protected boolean isCancelled() {
        return cancellationToken.isCancelled();
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
        if (isCancelled()) {
            return;
        }
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
