package io.github.compilerstuck.Control;

import io.github.compilerstuck.SortingAlgorithms.SortingAlgorithm;
import io.github.compilerstuck.Visual.Marker;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.min;

public class ArrayController {
    private int[] array;
    private Marker[] markers;
    private int length;

    private long comparisons;
    private long arrayAccesses;
    private long swaps;
    private long writes;
    private long writesAux;
    private double sortedPercentage;
    private int segments;
    private int maxSortingTime = 1000;

    private double delay;
    private double realTime;

    private ShuffleType shuffleType;


    public ArrayController(int size) {
        shuffleType = ShuffleType.RANDOM;
        resize(size);
    }

    public void resize(int size) {
        array = new int[size];
        markers = new Marker[size];
        length = size;
        comparisons = 0;
        arrayAccesses = 0;
        swaps = 0;
        writes = 0;
        delay = 0;
        realTime = 0;
        writesAux = 0;
        sortedPercentage = 1;
        segments = 1;

        //Initial Values
        for (int i = 0; i < size; i++) {
            array[i] = i;
            markers[i] = Marker.NORMAL;
        }
    }

    public long getComparisons() {
        return comparisons;
    }

    public void addComparisons(int n) {
        comparisons += n;
    }

    public long getArrayAccesses() {
        return arrayAccesses;
    }

    public long getSwaps() {
        return swaps;
    }

    public long getWrites() {
        return writes;
    }

    public void addSleepTime(double sleepTime) {
        this.delay += sleepTime;
    }

    public double getDelay() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    public double getRealTime() {
        return realTime;
    }

    public void addRealTime(double realTime) {
        this.realTime += realTime;
    }

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
        arrayAccesses = 0;
        swaps = 0;
        writes = 0;
        delay = 0;
        realTime = 0;
        writesAux = 0;
        sortedPercentage = 1;
        segments = 1;
    }

    public void resetArray() {
        for (int i = 0; i < length; i++) {
            array[i] = i;
            markers[i] = Marker.NORMAL;
        }
    }

    public Marker getMarker(int index) {
        return markers[index];
    }

    public void setMarker(int i, Marker m) {
        markers[i] = m;
    }

    public void resetMarkers() {
        for (int i = 0; i < length; i++) {
            markers[i] = Marker.NORMAL;
        }
    }

    public int getLength() {
        return length;
    }

    public int get(int i) {
        return array[i];
    }

    public void set(int i, int value) {
        array[i] = value;
        writes += 1;
    }

    public void swap(int i, int j) {
        int swapOneValue = array[i];
        array[i] = array[j];
        array[j] = swapOneValue;
        writes += 2;
        swaps += 1;
    }

    public boolean isSorted() {
        for (int i = 1; i < length; i++) {
            if (array[i - 1] > array[i]) return false;
        }
        return true;

    }


    public double getSortedPercentage() {
        return sortedPercentage;
    }

    public int getSegments() {
        return segments;
    }

    public void update() {

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
    }

    void shuffle() {
        if (!SortingAlgorithm.isRun()) return;
        switch (shuffleType) {
            case RANDOM -> standardShuffle();
            case REVERSE -> reverseShuffle();
            case ALMOST_SORTED -> almostSortedShuffle();
            case SORTED -> sortedShuffle();
        }
    }

    void standardShuffle() {
        for (int i = 0; i < length && SortingAlgorithm.isRun(); i++) {

            int swapTwo = (int) (Math.random() * length);

            swap(i, swapTwo);

            setMarker(i, Marker.SET);
            setMarker(swapTwo, Marker.SET);

            MainController.setCurrentOperation("Shuffling.. " + (int) ((double) i / (length - 1) * 100) + "%");


            ArrayList<Integer> delayTimes = (ArrayList<Integer>) Arrays.stream(new int[maxSortingTime]).boxed().collect(Collectors.toList());
            for (int j = 0; j < maxSortingTime; j++) {
                delayTimes.set(j, (int) PApplet.map(j, 0, maxSortingTime-1, 0, length-1));
            }


            if (delayTimes.contains(i)) {
                MainController.processing.delay(maxSortingTime / min(maxSortingTime, length));
            }
        }

    }

    void reverseShuffle() {
        for (int i = 0; i < length / 2 && SortingAlgorithm.isRun(); i++) {
            int swapTwo = length - 1 - i;
            swap(i, swapTwo);

            setMarker(i, Marker.SET);
            setMarker(swapTwo, Marker.SET);

            MainController.setCurrentOperation("Shuffling.. " + (int) (i / (length / 2. - 1) * 100) + "%");

            ArrayList<Integer> delayTimes = (ArrayList<Integer>) Arrays.stream(new int[maxSortingTime]).boxed().collect(Collectors.toList());
            for (int j = 0; j < maxSortingTime; j++) {
                delayTimes.set(j, (int) PApplet.map(j, 0, maxSortingTime-1, 0, length-1));
            }


            if (delayTimes.contains(i)) {
                MainController.processing.delay(maxSortingTime / min(maxSortingTime, length));
            }
        }
    }

    void almostSortedShuffle() {
        for (int i = 0; i < length / 10 && SortingAlgorithm.isRun(); i++) {

            int swapOne = (int) (Math.random() * length);
            int swapTwo = (int) (Math.random() * length);

            swap(swapOne, swapTwo);

            setMarker(swapOne, Marker.SET);
            setMarker(swapTwo, Marker.SET);

            MainController.setCurrentOperation("Shuffling.. " + (int) ((double) (i) / (length / 10 - 1) * 100) + "%");

            ArrayList<Integer> delayTimes = (ArrayList<Integer>) Arrays.stream(new int[maxSortingTime]).boxed().collect(Collectors.toList());
            for (int j = 0; j < maxSortingTime; j++) {
                delayTimes.set(j, (int) PApplet.map(j, 0, maxSortingTime-1, 0, length-1));
            }


            if (delayTimes.contains(i)) {
                MainController.processing.delay(maxSortingTime / min(maxSortingTime, length));
            }
        }

    }

    void sortedShuffle() {
        for (int i = 0; i < length && SortingAlgorithm.isRun(); i++) {
            setMarker(i, Marker.SET);

            MainController.setCurrentOperation("Shuffling.. " + (int) ((double) (i) / (length - 1) * 100) + "%");

            ArrayList<Integer> delayTimes = (ArrayList<Integer>) Arrays.stream(new int[maxSortingTime]).boxed().collect(Collectors.toList());
            for (int j = 0; j < maxSortingTime; j++) {
                delayTimes.set(j, (int) PApplet.map(j, 0, maxSortingTime-1, 0, length-1));
            }


            if (delayTimes.contains(i)) {
                MainController.processing.delay(maxSortingTime / min(maxSortingTime, length));
            }
        }
    }

    public void setShuffleType(ShuffleType shuffleType) {
        this.shuffleType = shuffleType;
    }

}
