package io.github.compilerstuck.control.catalog;

import io.github.compilerstuck.sortingalgorithms.*;
import java.util.List;

/**
 * Static registry of all sorting algorithms available to the Settings UI, in the order they were
 * historically presented.
 */
public final class AlgorithmCatalog {

  private AlgorithmCatalog() {}

  public static List<AlgorithmDescriptor> all() {
    return List.of(
        descriptor("quicksort-middle", "Quicksort (Middle Pivot)", QuickSortMiddlePivot::new),
        descriptor("merge-sort", "Merge Sort", MergeSort::new),
        descriptor("heap-sort", "Heap Sort", HeapSort::new),
        descriptor("radix-lsd-10", "Radix LSD Sort (Base 10)", RadixLSDSortBase10::new),
        descriptor("shell-sort", "Shell Sort", ShellSort::new),
        descriptor("cycle-sort", "Cycle Sort", CycleSort::new),
        descriptor("selection-sort", "Selection Sort", SelectionSort::new),
        descriptor("gnome-sort", "Gnome Sort", GnomeSort::new),
        descriptor("gravity-sort", "Gravity Sort", GravitySort::new),
        descriptor("counting-sort", "Counting Sort", CountingSort::new),
        descriptor("double-selection-sort", "Double Selection Sort", DoubleSelectionSort::new),
        descriptor("insertion-sort", "Insertion Sort", InsertionSort::new),
        descriptor("odd-even-sort", "Odd Even Sort", OddEvenSort::new),
        descriptor("comb-sort", "Comb Sort", CombSort::new),
        descriptor("bubble-sort", "Bubble Sort", BubbleSort::new),
        descriptor("quicksort-dual", "Quicksort (Dual Pivot)", QuickSortDualPivot::new),
        descriptor("shaker-sort", "Shaker Sort", ShakerSort::new),
        descriptor("bucket-sort", "Bucket Sort", BucketSort::new),
        descriptor("american-flag-sort", "American Flag Sort", AmericanFlagSort::new),
        descriptor("pigeonhole-sort", "Pigeonhole Sort", PigeonholeSort::new),
        descriptor("tim-sort", "Tim Sort", TimSort::new),
        descriptor("bogo-sort", "Bogo Sort", BogoSort::new));
  }

  public static AlgorithmDescriptor findById(String id) {
    return all().stream().filter(d -> d.id().equals(id)).findFirst().orElse(all().get(0));
  }

  public static int indexOfId(String id) {
    List<AlgorithmDescriptor> list = all();
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).id().equals(id)) {
        return i;
      }
    }
    return 0;
  }

  private static AlgorithmDescriptor descriptor(
      String id,
      String displayName,
      java.util.function.Function<
              io.github.compilerstuck.control.model.ArrayModel, SortingAlgorithm>
          ctor) {
    return new AlgorithmDescriptor(
        id,
        displayName,
        (array, proc) -> {
          SortingAlgorithm alg = ctor.apply(array);
          alg.setDelayContext(proc);
          return alg;
        });
  }
}
