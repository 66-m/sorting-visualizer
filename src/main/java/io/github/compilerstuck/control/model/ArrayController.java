package io.github.compilerstuck.control.model;

import io.github.compilerstuck.control.config.ShuffleStrategy;
import io.github.compilerstuck.control.config.ShuffleType;
import io.github.compilerstuck.control.render.ProcessingContext;
import io.github.compilerstuck.control.shuffle.AlmostSortedShuffleStrategy;
import io.github.compilerstuck.control.shuffle.RandomShuffleStrategy;
import io.github.compilerstuck.control.shuffle.ReverseShuffleStrategy;
import io.github.compilerstuck.control.shuffle.SortedShuffleStrategy;
import io.github.compilerstuck.visual.Marker;

public class ArrayController implements ArrayModel {
    private int[] array;
    private Marker[] markers;
    private int length;

    private long comparisons;
    private long swaps;
    private long writes;
    private long writesAux;
    private double sortedPercentage;
    private int segments;
    /** When true, {@link #update()} must recompute sorted% / segments. */
    private volatile boolean metricsDirty = true;
    private double delay;
    private double realTime;

    private ShuffleStrategy shuffleStrategy;
    private OperationReporter operationReporter = OperationReporter.NOOP;
    private CancellationToken cancellationToken = CancellationToken.alwaysActive();
    private ProcessingContext processingContext = ms -> { /* no-op */ };


    public ArrayController(int size) {
        setShuffleType(ShuffleType.RANDOM);
        resize(size);
    }

    public void resize(int size) {
        array = new int[size];
        markers = new Marker[size];
        length = size;
        comparisons = 0;
        swaps = 0;
        writes = 0;
        delay = 0;
        realTime = 0;
        writesAux = 0;
        sortedPercentage = 1;
        segments = 1;
        metricsDirty = true;

        //Initial Values
        for (int i = 0; i < size; i++) {
            array[i] = i;
            markers[i] = Marker.NORMAL;
        }
    }

    private void markMetricsDirty() {
        metricsDirty = true;
    }

    @Override
    public long getComparisons() {
        return comparisons;
    }

    @Override
    public void addComparisons(int n) {
        comparisons += n;
    }

    @Override
    public long getSwaps() {
        return swaps;
    }

    @Override
    public long getWrites() {
        return writes;
    }

    @Override
    public void addSleepTime(double sleepTime) {
        this.delay += sleepTime;
    }

    @Override
    public double getDelay() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    @Override
    public double getRealTime() {
        return realTime;
    }

    @Override
    public void addRealTime(double realTime) {
        this.realTime += realTime;
    }

    @Override
    public int[] getArray() {
        return array;
    }

    public long getWritesAux() {
        return writesAux;
    }

    public void addWritesAux(int n) {
        this.writesAux += n;
    }

    public void resetMeasurements() {
        comparisons = 0;
        swaps = 0;
        writes = 0;
        delay = 0;
        realTime = 0;
        writesAux = 0;
        sortedPercentage = 1;
        segments = 1;
        metricsDirty = true;
    }

    public void resetArray() {
        for (int i = 0; i < length; i++) {
            array[i] = i;
            markers[i] = Marker.NORMAL;
        }
        markMetricsDirty();
    }

    @Override
    public Marker getMarker(int index) {
        return markers[index];
    }

    @Override
    public void setMarker(int i, Marker m) {
        markers[i] = m;
    }

    public void resetMarkers() {
        for (int i = 0; i < length; i++) {
            markers[i] = Marker.NORMAL;
        }
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int get(int i) {
        return array[i];
    }

    @Override
    public void set(int i, int value) {
        array[i] = value;
        writes += 1;
        markMetricsDirty();
    }

    @Override
    public void swap(int i, int j) {
        int swapOneValue = array[i];
        array[i] = array[j];
        array[j] = swapOneValue;
        writes += 2;
        swaps += 1;
        markMetricsDirty();
    }

    @Override
    public boolean isSorted() {
        for (int i = 1; i < length; i++) {
            if (array[i - 1] > array[i]) return false;
        }
        return true;

    }


    @Override
    public double getSortedPercentage() {
        return sortedPercentage;
    }

    @Override
    public int getSegments() {
        return segments;
    }

    public void update() {
        if (!metricsDirty) {
            return;
        }

        double sortedCount = 0;
        int segmentStart = 0;
        int sgmnts = 0;
        for (int i = 1; i < length; i++) {
            if (array[i] < array[i - 1]) {
                sortedCount += i - 1 - segmentStart;
                segmentStart = i;
                sgmnts++;
            } else if (i == length - 1 && array[length - 1] > array[length - 2]) {
                sortedCount += i - segmentStart + 1;
                segmentStart = i;
                sgmnts++;
            }
        }

        segments = sgmnts;
        sortedPercentage = sortedCount / length;
        metricsDirty = false;
    }

    /** Package-visible for tests. */
    boolean isMetricsDirty() {
        return metricsDirty;
    }

    void shuffle() {
        if (cancellationToken.isCancelled()) return;
        shuffleStrategy.shuffle(this, processingContext, operationReporter, cancellationToken);
        markMetricsDirty();
    }

    public void setProcessingContext(ProcessingContext processingContext) {
        this.processingContext = processingContext != null
                ? processingContext
                : ms -> { /* no-op */ };
    }

    public void setShuffleType(ShuffleType shuffleType) {
        this.shuffleStrategy = switch (shuffleType) {
            case RANDOM -> new RandomShuffleStrategy();
            case REVERSE -> new ReverseShuffleStrategy();
            case ALMOST_SORTED -> new AlmostSortedShuffleStrategy();
            case SORTED -> new SortedShuffleStrategy();
        };
    }

    public void setOperationReporter(OperationReporter operationReporter) {
        this.operationReporter = operationReporter != null ? operationReporter : OperationReporter.NOOP;
    }

    public void setCancellationToken(CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken != null
                ? cancellationToken
                : CancellationToken.alwaysActive();
    }

}
