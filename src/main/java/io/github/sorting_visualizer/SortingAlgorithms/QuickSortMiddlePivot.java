package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;
import io.github.sorting_visualizer.Control.ArrayController;

public class QuickSortMiddlePivot extends SortingAlgorithm {

    long startTime;

    public QuickSortMiddlePivot(ArrayController arrayController) {
        super(arrayController);
        this.name = "Quick Sort (Pivot Middle)";
        alternativeSize = arrayController.getLength();
    }

    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();
        sort(arrayController, 0, arrayController.getLength() - 1);
        arrayController.addRealTime(System.nanoTime() - startTime);
    }

    private void sort(ArrayController arrayController, int start, int end) {
        if (arrayController.getLength() == 0) {
            return;
        }

        if (start >= end) {
            return;
        }

        // pick the pivot
        int middle = start + (end - start) / 2;
        int pivot = arrayController.get(middle);

        // make left < pivot and right > pivot
        int i = start, j = end;
        while (i <= j && run) {
            while (arrayController.get(i) < pivot && run) {
                i++;
                arrayController.addComparisons(1);
            }
            arrayController.addComparisons(1);

            while (arrayController.get(j) > pivot && run) {
                j--;
                arrayController.addComparisons(1);
            }
            arrayController.addComparisons(1);

            if (i <= j) {
                arrayController.swap(i, j);
                arrayController.setMarker(i, Marker.SET);
                arrayController.setMarker(j, Marker.SET);
                i++;
                j--;
            }

            if (delay){
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(1);
                startTime = System.nanoTime();}

        }

        // recursively sort two sub parts
        if (start < j && run)
            sort(arrayController, start, j);

        if (end > i && run)
            sort(arrayController, i, end);
    }

// CODE FOR RIGHTMOST ELEMENT AS PIVOT. Has Bug (?)
//    private void sort(ArrayController arrayController, int start, int end){
//        arrayController.addRealTime(System.nanoTime() - startTime);
//        startTime = System.nanoTime();
//
//        int partition = partition(arrayController, start, end);
//
//        if (partition - 1 > start){
//            sort(arrayController, start, partition - 1);
//        }
//        arrayController.addComparisons(1);
//
//
//        if (partition + 1 < end){
//            sort(arrayController, partition + 1, end);
//        }
//        arrayController.addComparisons(1);
//
//        arrayController.addRealTime(System.nanoTime() - startTime);
//        startTime = System.nanoTime();
//
//    }
//
//    private int partition(ArrayController arrayController, int start, int end){
//
//        int pivot = arrayController.get(start+ (int) proc.random((end-start)));
//
//        for (int i = start; i < end; i++) {
//
//            if (arrayController.get(i) < pivot){
//
//                arrayController.swap(i,start);
//                arrayController.setMarker(i, Marker.SET);
//                arrayController.setMarker(start, Marker.SET);
//
//                start++;
//            }
//
//            arrayController.addRealTime(System.nanoTime() - startTime);
//            arrayController.addComparisons(1);
//            if (i%2==0){proc.delay(1);}
//            startTime = System.nanoTime();
//        }
//
//        arrayController.swap(start, end);
//
//        arrayController.setMarker(start, Marker.SET);
//        arrayController.setMarker(end, Marker.SET);
//
//        return start;
//
//    }


}
