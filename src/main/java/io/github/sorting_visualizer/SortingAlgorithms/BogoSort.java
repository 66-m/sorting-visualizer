package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;

import java.util.Random;

public class BogoSort extends SortingAlgorithm {

    float startTime;
    int trycnt = 0;

    public BogoSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Bogo Sort";
        alternativeSize = arrayController.getLength();
    }

    public BogoSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Bogo Sort";
        this.alternativeSize = alternativeSize;
    }


    public void sort() {
        trycnt=0;
        MainController.setCurrentOperation(name);
        long startTime = System.nanoTime();
        Random r = new Random();
        while(!arrayController.isSorted() && run){

            int a = r.nextInt(arrayController.getLength());
            int b = r.nextInt(arrayController.getLength());

            arrayController.swap(a,b);


            arrayController.setMarker(a,Marker.SET);
            if (delay & Math.random()>0.98){
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(1);
                startTime = System.nanoTime();
            }

            trycnt++;
            MainController.setCurrentOperation("Bogo Sort (Tries: " + trycnt + ")");
        }


        arrayController.addRealTime(System.nanoTime() - startTime);
    }

}
