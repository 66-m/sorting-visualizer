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
        delayFactor = 0.000001;
    }

    public BogoSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Bogo Sort";
        this.alternativeSize = alternativeSize;
        delayFactor = 0.000001;
    }


    public void sort() {
        trycnt = 0;
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();
        Random r = new Random();
        while (!arrayController.isSorted() && run) {

            int a = r.nextInt(arrayController.getLength());
            int b = r.nextInt(arrayController.getLength());

            arrayController.swap(a, b);


            arrayController.setMarker(a, Marker.SET);
            delay();

            trycnt++;
            MainController.setCurrentOperation("Bogo Sort (Tries: " + trycnt + ")");
        }


        arrayController.addRealTime(System.nanoTime() - startTime);
    }

}
