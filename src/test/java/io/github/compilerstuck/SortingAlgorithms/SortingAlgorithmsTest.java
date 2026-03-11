package io.github.compilerstuck.SortingAlgorithms;

import io.github.compilerstuck.Control.ArrayModel;
import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import processing.core.PApplet;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SortingAlgorithmsTest {

    /**
     * Minimal stub implementation of the Processing runtime used by the visualizer.
     * Most of the sorting code only ever calls {@code delay(int)}, so we provide a
     * no-op implementation to avoid starting a real graphics context during tests.
     */
    static class DummyProcessing extends PApplet implements io.github.compilerstuck.Control.ProcessingContext {
        @Override
        public void delay(int ms) {
            // do nothing, keep tests fast
        }
    }

    @BeforeAll
    static void setupMainController() {
        // set static processing field so that algorithms don't hit NPE if they
        // accidentally call it even though we disable delays in the tests.
        MainController.processing = new DummyProcessing();
    }

    /**
     * Create a random permutation of 0..size-1.  Using a fixed seed ensures
     * deterministic behaviour across runs.
     */
    private static int[] randomPermutation(int size, long seed) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = i;
        }
        Random rnd = new Random(seed);
        for (int i = size - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
        return arr;
    }

    /**
     * Provide every algorithm except for the deliberately pathological BogoSort.
     * Each combination will be executed with two different array sizes to increase
     * confidence that the implementation works for both small and moderately
     * sized inputs.
     */
    static Stream<Arguments> algorithmAndSizes() {
        List<Class<? extends SortingAlgorithm>> algs = List.of(
                BubbleSort.class,
                SelectionSort.class,
                InsertionSort.class,
                MergeSort.class,
                QuickSortMiddlePivot.class,
                QuickSortDualPivot.class,
                HeapSort.class,
                ShellSort.class,
                TimSort.class,
                CountingSort.class,
                RadixLSDSortBase10.class,
                BucketSort.class,
                AmericanFlagSort.class,
                CycleSort.class,
                CombSort.class,
                GnomeSort.class,
                OddEvenSort.class,
                ShakerSort.class,
                DoubleSelectionSort.class,
                PigeonholeSort.class,
                GravitySort.class
        );

        return algs.stream()
                .flatMap(alg -> Stream.of(Arguments.of(alg, 10), Arguments.of(alg, 50)));
    }

    @ParameterizedTest(name = "{0} sorts {1} elements")
    @MethodSource("algorithmAndSizes")
    void algorithmShouldSort(Class<? extends SortingAlgorithm> algoClass, int size) throws Exception {
        // we'll test three common scenarios: random data, already-sorted, and reverse-sorted
        int[][] datasets = new int[4][];
        datasets[0] = randomPermutation(size, 12345L + size);
        datasets[1] = new int[size];
        datasets[2] = new int[size];
        datasets[3] = new int[size];
        for (int i = 0; i < size; i++) {
            datasets[1][i] = i;              // already sorted
            datasets[2][i] = size - i - 1;   // reverse order
            datasets[3][i] = i % 5;          // many duplicates
        }

        for (int idx = 0; idx < datasets.length; idx++) {
            int[] values = datasets[idx];
            ArrayController controller = new ArrayController(size);
            for (int i = 0; i < size; i++) {
                controller.set(i, values[i]);
            }
            SortingAlgorithm algorithm = algoClass.getConstructor(ArrayModel.class)
                    .newInstance(controller);
            algorithm.setDelay(false);
            algorithm.sort();
            String type;
            switch (idx) {
                case 0 -> type = "random";
                case 1 -> type = "sorted";
                case 2 -> type = "reverse";
                case 3 -> type = "duplicates";
                default -> type = "unknown";
            }
            assertTrue(controller.isSorted(), algoClass.getSimpleName() + " failed to sort " + type + " dataset");
        }
    }

    @Test
    @DisplayName("BogoSort eventually produces a sorted array (small size)")
    @org.junit.jupiter.api.Timeout(value = 5)
    void bogoSortSmallArray() {
        int size = 4; // small enough that random swaps finish in a reasonable time
        ArrayController controller = new ArrayController(size);
        int[] values = randomPermutation(size, 42L);
        for (int i = 0; i < size; i++) {
            controller.set(i, values[i]);
        }

        BogoSort algo = new BogoSort(controller);
        algo.setDelay(false);
        // run for a capped number of iterations to avoid infinite loops in case
        // something is wrong; the implementation keeps trying until sorted, so we
        // simply execute the method on the small array.
        algo.sort();
        assertTrue(controller.isSorted(), "BogoSort did not sort the small array");
    }
}
