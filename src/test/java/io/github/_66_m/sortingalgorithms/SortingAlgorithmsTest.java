package io.github._66_m.sortingalgorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github._66_m.control.catalog.AlgorithmCatalog;
import io.github._66_m.control.catalog.AlgorithmDescriptor;
import io.github._66_m.control.model.ArrayController;
import io.github._66_m.control.model.ArrayModel;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Runs every catalog algorithm (except the deliberately pathological Bogo Sort) over a matrix of
 * sizes and input shapes, and requires the output to be exactly the sorted input: a sorted
 * permutation, not merely "in order".
 */
class SortingAlgorithmsTest {

  private static final String BOGO_SORT_ID = "bogo-sort";

  private static final int[] SIZES = {1, 2, 3, 4, 10, 50, 257, 1000};

  /** Input shapes; every value stays in {@code [0, size)}, like the arrays the app produces. */
  private static final Map<String, IntFunction<int[]>> DATASETS =
      Map.of(
          "random", size -> randomPermutation(size, 12345L + size),
          "sorted", size -> fill(size, i -> i),
          "reverse", size -> fill(size, i -> size - i - 1),
          "few-unique", size -> fill(size, i -> i % 5 % size),
          "all-equal", size -> fill(size, i -> size / 2),
          "organ-pipe", size -> fill(size, i -> Math.min(i, size - 1 - i) * 2 % size));

  static Stream<Arguments> algorithmsSizesAndDatasets() {
    return AlgorithmCatalog.all().stream()
        .filter(d -> !d.id().equals(BOGO_SORT_ID))
        .flatMap(
            d ->
                Arrays.stream(SIZES)
                    .boxed()
                    .flatMap(
                        size ->
                            DATASETS.keySet().stream()
                                .sorted()
                                .map(dataset -> Arguments.of(d.id(), size, dataset))));
  }

  @ParameterizedTest(name = "{0} sorts {2} input of {1} elements")
  @MethodSource("algorithmsSizesAndDatasets")
  @Timeout(10)
  void algorithmProducesTheSortedInput(String algorithmId, int size, String dataset) {
    AlgorithmDescriptor descriptor = AlgorithmCatalog.find(algorithmId).orElseThrow();
    int[] input = DATASETS.get(dataset).apply(size);

    int[] output = runSort(array -> descriptor.factory().apply(array, null), input);

    assertSortedPermutation(input, output, algorithmId + " on " + dataset + " x" + size);
  }

  @Test
  @DisplayName("the checker rejects an algorithm that sorts by overwriting values")
  void checkerRejectsAnAlgorithmThatLosesValues() {
    int[] input = randomPermutation(10, 7L);

    int[] output = runSort(ZeroingSort::new, input);

    assertThrows(AssertionError.class, () -> assertSortedPermutation(input, output, "zeroing"));
  }

  @Test
  @DisplayName("BogoSort eventually produces a sorted array (small size)")
  @Timeout(5)
  void bogoSortSmallArray() {
    AlgorithmDescriptor bogo = AlgorithmCatalog.find(BOGO_SORT_ID).orElseThrow();
    int[] input = randomPermutation(4, 42L);

    int[] output = runSort(array -> bogo.factory().apply(array, null), input);

    assertSortedPermutation(input, output, "bogo-sort");
  }

  private static int[] runSort(Function<ArrayModel, SortingAlgorithm> factory, int[] input) {
    ArrayController controller = new ArrayController(input.length);
    for (int i = 0; i < input.length; i++) {
      controller.set(i, input[i]);
    }
    SortingAlgorithm algorithm = factory.apply(controller);
    algorithm.setDelay(false);
    algorithm.sort();
    int[] output = new int[input.length];
    for (int i = 0; i < output.length; i++) {
      output[i] = controller.get(i);
    }
    return output;
  }

  private static void assertSortedPermutation(int[] input, int[] output, String label) {
    int[] expected = input.clone();
    Arrays.sort(expected);
    assertArrayEquals(expected, output, label + ": output is not the sorted input");
  }

  private static int[] randomPermutation(int size, long seed) {
    int[] arr = fill(size, i -> i);
    Random rnd = new Random(seed);
    for (int i = size - 1; i > 0; i--) {
      int j = rnd.nextInt(i + 1);
      int tmp = arr[i];
      arr[i] = arr[j];
      arr[j] = tmp;
    }
    return arr;
  }

  private static int[] fill(int size, IntUnaryOperator valueAt) {
    int[] arr = new int[size];
    for (int i = 0; i < size; i++) {
      arr[i] = valueAt.applyAsInt(i);
    }
    return arr;
  }

  /** Deliberately broken: produces an ordered array by discarding every value. */
  private static final class ZeroingSort extends SortingAlgorithm {
    ZeroingSort(ArrayModel array) {
      super(array);
    }

    @Override
    public void sort() {
      for (int i = 0; i < arrayController.getLength(); i++) {
        arrayController.set(i, 0);
      }
    }
  }
}
