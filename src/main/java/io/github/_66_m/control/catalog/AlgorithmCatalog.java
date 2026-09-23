package io.github._66_m.control.catalog;

import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.sortingalgorithms.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static registry of all sorting algorithms available to the Settings UI, in the order they were
 * historically presented. This is the single source of truth for algorithm ids and display names.
 */
public final class AlgorithmCatalog {

  private static final Logger LOGGER = Logger.getLogger(AlgorithmCatalog.class.getName());

  private static final List<AlgorithmDescriptor> ALL =
      List.of(
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
          descriptor("bogo-sort", "Bogo Sort", BogoSort::new),
          descriptor("unbelievable-sort", "I Can't Believe It Can Sort", UnbelievableSort::new));

  private AlgorithmCatalog() {}

  public static List<AlgorithmDescriptor> all() {
    return ALL;
  }

  public static Optional<AlgorithmDescriptor> find(String id) {
    return ALL.stream().filter(d -> d.id().equals(id)).findFirst();
  }

  /** Looks up {@code id}, falling back to the first algorithm (with a warning) if it is unknown. */
  public static AlgorithmDescriptor findByIdOrDefault(String id) {
    return find(id)
        .orElseGet(
            () -> {
              LOGGER.log(
                  Level.WARNING,
                  "Unknown algorithm id {0}; falling back to {1}",
                  new Object[] {id, ALL.get(0).id()});
              return ALL.get(0);
            });
  }

  /** Index of {@code id} in {@link #all()}, or 0 if it is unknown. */
  public static int indexOfId(String id) {
    for (int i = 0; i < ALL.size(); i++) {
      if (ALL.get(i).id().equals(id)) {
        return i;
      }
    }
    return 0;
  }

  /** The catalog stamps each instance with its display name, so names live only here. */
  private static AlgorithmDescriptor descriptor(
      String id, String displayName, Function<ArrayModel, SortingAlgorithm> ctor) {
    return new AlgorithmDescriptor(
        id,
        displayName,
        (array, proc) -> {
          SortingAlgorithm alg = ctor.apply(array);
          alg.setName(displayName);
          alg.setDelayContext(proc);
          return alg;
        });
  }
}
