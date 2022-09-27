package io.github.sorting_visualizer.SortingAlgorithms;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;

public class TimSort extends SortingAlgorithm {
    long startTime;
    public TimSort(ArrayController arrayController) {
        super(arrayController);
        this.name = "Tim Sort";
        alternativeSize = arrayController.getLength();
    }

    public TimSort(ArrayController arrayController, int alternativeSize) {
        super(arrayController);
        this.name = "Tim Sort";
        this.alternativeSize = alternativeSize;
    }

    int RUN = 32;

    public void insertionSort(int left, int right)
    {
        for (int i = 1+left; i <= right; i++)
        {
            int temp = arrayController.get(i);
            int j = i - 1;
            while (j >= left && arrayController.get(j) > temp && run)
            {
                arrayController.addComparisons(1);
                arrayController.set(j+1,arrayController.get(j));
                arrayController.setMarker(j+1,Marker.SET);
                if (delay){
                    arrayController.addRealTime(System.nanoTime() - startTime);
                    proc.delay(1);
                    startTime = System.nanoTime();
                }
                j--;
            }
            arrayController.addComparisons(1);
            arrayController.set(j+1,temp);
            arrayController.setMarker(j+1,Marker.SET);
            if (delay){
                arrayController.addRealTime(System.nanoTime() - startTime);
                proc.delay(1);
                startTime = System.nanoTime();
            }
        }
    }

    public void merge(int l,int m, int r)
    {
            int len1 = m - l + 1, len2 = r - m;
            int[] left = new int[len1];
            int[] right = new int[len2];
            for (int x = 0; x < len1; x++)
            {
                left[x] = arrayController.get(l+x);
                arrayController.addWritesAux(1);
            }
            for (int x = 0; x < len2; x++)
            {
                right[x] = arrayController.get(m+1+x);
                arrayController.addWritesAux(1);
            }

            int i = 0;
            int j = 0;
            int k = l;

            while (i < len1 && j < len2 && run)
            {
                if (left[i] <= right[j])
                {
                    arrayController.set(k,left[i]);
                    arrayController.setMarker(k,Marker.SET);
                    if (delay){
                        arrayController.addRealTime(System.nanoTime() - startTime);
                        proc.delay(1);
                        startTime = System.nanoTime();
                    }
                    i++;
                }
                else
                {
                    arrayController.set(k,right[j]);
                    arrayController.setMarker(k,Marker.SET);
                    if (delay){
                        arrayController.addRealTime(System.nanoTime() - startTime);
                        proc.delay(1);
                        startTime = System.nanoTime();
                    }
                    j++;
                }
                arrayController.addComparisons(1);
                k++;
            }

            while (i < len1 && run)
            {
                arrayController.set(k,left[i]);
                arrayController.setMarker(k,Marker.SET);
                if (delay){
                    arrayController.addRealTime(System.nanoTime() - startTime);
                    proc.delay(1);
                    startTime = System.nanoTime();
                }
                k++;
                i++;
            }
            while (j < len2 && run)
            {
                arrayController.set(k,right[j]);
                arrayController.setMarker(k,Marker.SET);
                if (delay){
                    arrayController.addRealTime(System.nanoTime() - startTime);
                    proc.delay(1);
                    startTime = System.nanoTime();
                }
                k++;
                j++;
            }

    }
    public void sort()
    {
        MainController.setCurrentOperation(name);
        startTime = System.nanoTime();

        for (int i = 0; i < arrayController.getLength() && run; i += RUN)
        {
            insertionSort(i, Math.min((i + 31), (arrayController.getLength() - 1)));
        }

        for (int size = RUN; size < arrayController.getLength() && run; size = 2 * size)
        {

            for (int left = 0; left < arrayController.getLength() && run; left += 2 * size)
            {
                int mid = left + size - 1;
                int right = Math.min((left + 2 * size - 1), (arrayController.getLength() - 1));

                merge(left, mid, right);
            }
        }
        arrayController.addRealTime(System.nanoTime() - startTime);
    }


}
