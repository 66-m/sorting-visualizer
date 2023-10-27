package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;

public class GnomeSort extends SortingAlgorithm {

    public GnomeSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Gnome Sort";
        alternativeSize = arrayController.getLength();
        delayFactor = 1. / 50;
    }

    public GnomeSort(ArrayController arrayController, int alternativeArrSize) {
        super(arrayController);
        this.name = "Gnome Sort";
        this.alternativeSize = alternativeArrSize;
        delayFactor = 1. / 50;
    }


    public void sort() {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        delayFactor = 1. / (arrayController.getLength()/40);

        int index = 0;
        while (index < arrayController.getLength() && run) {


            if (index == 0)
                index++;
            if (arrayController.get(index) >= arrayController.get(index - 1))
                index++;
            else {
                arrayController.swap(index, index - 1);
                index--;

                delay(new int[]{index});
            }
            arrayController.addComparisons(1);


        }

        arrayController.addRealTime(System.nanoTime() - startTime);

    }

}
