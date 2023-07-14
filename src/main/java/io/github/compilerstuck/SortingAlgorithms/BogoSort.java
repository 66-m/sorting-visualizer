package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

import java.util.Random;

public class BogoSort extends SortingAlgorithm {

    long trycnt = 0;

    public BogoSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Bogo Sort";
        alternativeSize = arrayController.getLength();
        selected = false;
    }

    public BogoSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Bogo Sort";
        this.alternativeSize = alternativeSize;
    }


    public void sort() {
        trycnt = 0;
        MainController.setCurrentOperation(name);
        long startTime = System.nanoTime();
        Random r = new Random();
        while (!arrayController.isSorted() && run) {

            int a = r.nextInt(arrayController.getLength());
            int b = r.nextInt(arrayController.getLength());

            arrayController.swap(a, b);


            arrayController.setMarker(a, Marker.SET);
            if (delay & Math.random() > 0.999999) {
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
