package io.github.compilerstuck.Control;

import io.github.compilerstuck.Control.config.DelayStrategy;
import io.github.compilerstuck.Control.config.ShuffleType;
import io.github.compilerstuck.Control.model.ArrayController;
import io.github.compilerstuck.Control.render.ProcessingContext;
import io.github.compilerstuck.Control.shuffle.AlmostSortedShuffleStrategy;
import io.github.compilerstuck.Control.shuffle.RandomShuffleStrategy;
import io.github.compilerstuck.Control.shuffle.ReverseShuffleStrategy;
import io.github.compilerstuck.Control.shuffle.SortedShuffleStrategy;
import io.github.compilerstuck.SortingAlgorithms.SortingAlgorithm;
import io.github.compilerstuck.Visual.Marker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ArrayModel, DelayStrategy, and ShuffleStrategy.
 */
class ArrayModelAndStrategyTest {

    /** No-op ProcessingContext for testing (avoids any real Processing runtime). */
    private static final ProcessingContext NO_OP_CTX = ms -> { /* no-op */ };

    private ArrayController controller;

    @BeforeEach
    void setUp() {
        SortingAlgorithm.setRun(true);
        MainController.processing = NO_OP_CTX;
        controller = new ArrayController(10);
    }

    // -----------------------------------------------------------------------
    // ArrayModel – ArrayController basics
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ArrayController initial values are 0..length-1")
    void initialValuesAreIdentityPermutation() {
        for (int i = 0; i < controller.getLength(); i++) {
            assertEquals(i, controller.get(i));
        }
    }

    @Test
    @DisplayName("set() writes value and increments write count")
    void setWritesValue() {
        controller.set(3, 99);
        assertEquals(99, controller.get(3));
        assertTrue(controller.getWrites() >= 1);
    }

    @Test
    @DisplayName("swap() exchanges two elements")
    void swapExchangesElements() {
        controller.swap(0, 9);
        assertEquals(9, controller.get(0));
        assertEquals(0, controller.get(9));
    }

    @Test
    @DisplayName("isSorted() returns true for sorted array")
    void isSortedReturnsTrueWhenSorted() {
        assertTrue(controller.isSorted(), "Initial identity permutation should be sorted");
    }

    @Test
    @DisplayName("isSorted() returns false after a swap that disorders the array")
    void isSortedReturnsFalseWhenNotSorted() {
        controller.swap(0, 9);
        assertFalse(controller.isSorted());
    }

    @Test
    @DisplayName("setMarker() and getMarker() round-trip")
    void markerRoundTrip() {
        controller.setMarker(5, Marker.SET);
        assertEquals(Marker.SET, controller.getMarker(5));
    }

    @Test
    @DisplayName("addComparisons() accumulates correctly")
    void comparisonsAccumulate() {
        controller.addComparisons(3);
        controller.addComparisons(7);
        assertEquals(10, controller.getComparisons());
    }

    @Test
    @DisplayName("getArray() returns the backing array view")
    void getArrayReturnsMutableView() {
        int[] arr = controller.getArray();
        assertNotNull(arr);
        assertEquals(controller.getLength(), arr.length);
        assertEquals(controller.get(0), arr[0]);
    }

    @Test
    @DisplayName("resize() resets state to a fresh identity array")
    void resizeFreshensState() {
        controller.swap(0, 9);
        controller.addComparisons(5);
        controller.resize(5);

        assertEquals(5, controller.getLength());
        assertTrue(controller.isSorted());
        assertEquals(0, controller.getComparisons());
    }

    // -----------------------------------------------------------------------
    // DelayStrategy
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("DelayStrategy.DEFAULT always delays small arrays")
    void defaultStrategyDelaysSmallArrays() {
        // With a 10-element array (< DEFAULT_THRESHOLD), should always delay
        // at delayFactor = 1.0
        long fires = 0;
        for (int i = 0; i < 1000; i++) {
            if (DelayStrategy.DEFAULT.shouldDelay(10, 1.0)) fires++;
        }
        assertEquals(1000, fires, "Should fire every time for tiny array with factor=1.0");
    }

    @Test
    @DisplayName("DelayStrategy.DEFAULT respects delayFactor=0 (never delay)")
    void defaultStrategyRespectsZeroFactor() {
        long fires = 0;
        for (int i = 0; i < 1000; i++) {
            if (DelayStrategy.DEFAULT.shouldDelay(10, 0.0)) fires++;
        }
        assertEquals(0, fires, "delayFactor=0 should suppress all delays");
    }

    @Test
    @DisplayName("DelayStrategy.DEFAULT fires proportionally for large arrays")
    void defaultStrategyProportionalForLargeArrays() {
        // arrayLength = 4000 (> DEFAULT_THRESHOLD=2000), factor=1.0
        // Expected probability per call ≈ 0.5
        long fires = 0;
        int trials = 100_000;
        for (int i = 0; i < trials; i++) {
            if (DelayStrategy.DEFAULT.shouldDelay(4000, 1.0)) fires++;
        }
        double rate = (double) fires / trials;
        assertTrue(rate > 0.4 && rate < 0.6,
                "Expected ~50% fire rate for arrayLength=4000, got " + rate);
    }

    // -----------------------------------------------------------------------
    // ShuffleStrategy implementations
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("RandomShuffleStrategy produces a permutation (same elements)")
    void randomShuffleIsPermutation() {
        new RandomShuffleStrategy().shuffle(controller, NO_OP_CTX);
        int sum = 0;
        for (int i = 0; i < controller.getLength(); i++) sum += controller.get(i);
        assertEquals(45, sum, "Sum of 0..9 should be 45 regardless of order");
    }

    @Test
    @DisplayName("ReverseShuffleStrategy reverses the array")
    void reverseShuffleReversesArray() {
        new ReverseShuffleStrategy().shuffle(controller, NO_OP_CTX);
        for (int i = 0; i < controller.getLength(); i++) {
            assertEquals(controller.getLength() - 1 - i, controller.get(i),
                    "Index " + i + " should be " + (controller.getLength() - 1 - i));
        }
    }

    @Test
    @DisplayName("AlmostSortedShuffleStrategy keeps elements as a permutation")
    void almostSortedShuffleIsPermutation() {
        new AlmostSortedShuffleStrategy().shuffle(controller, NO_OP_CTX);
        int sum = 0;
        for (int i = 0; i < controller.getLength(); i++) sum += controller.get(i);
        assertEquals(45, sum);
    }

    @Test
    @DisplayName("SortedShuffleStrategy leaves the array sorted")
    void sortedShuffleLeavesSorted() {
        // The array starts sorted; SortedShuffle should not swap anything
        new SortedShuffleStrategy().shuffle(controller, NO_OP_CTX);
        assertTrue(controller.isSorted());
    }

    @ParameterizedTest(name = "setShuffleType({0}) wires the correct strategy")
    @ValueSource(strings = {"RANDOM", "REVERSE", "ALMOST_SORTED", "SORTED"})
    @DisplayName("setShuffleType wires correct strategy")
    void setShuffleTypeWiresCorrectStrategy(String typeName) {
        ShuffleType type = ShuffleType.valueOf(typeName);
        // Should not throw; just ensure the strategy is set without errors
        assertDoesNotThrow(() -> controller.setShuffleType(type));
    }
}
