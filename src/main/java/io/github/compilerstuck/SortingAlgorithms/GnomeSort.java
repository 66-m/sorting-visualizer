package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

import java.util.Random;

public class GnomeSort extends SortingAlgorithm {

    public GnomeSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Gnome Sort";
        alternativeSize = arrayController.getLength();
    }

    public GnomeSort(ArrayController arrayController, int alternativeArrSize) {
        super(arrayController);
        this.name = "Gnome Sort";
        this.alternativeSize = alternativeArrSize;
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        long startTime = System.nanoTime();

        int index = 0;
        while (index < arrayController.getLength() && run) {


            if (index == 0)
                index++;
            if (arrayController.get(index) >= arrayController.get(index - 1))
                index++;
            else {
                arrayController.swap(index, index - 1);
                index--;
                if (delay && new Random().nextInt(30) == 1) {
                    arrayController.setMarker(index, Marker.SET);
                    arrayController.addRealTime(System.nanoTime() - startTime);
                    proc.delay(1);
                    startTime = System.nanoTime();
                }
            }
            arrayController.addComparisons(1);


        }

        arrayController.addRealTime(System.nanoTime() - startTime);

    }

}
