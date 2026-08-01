package io.github.compilerstuck.control.shuffle;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.config.ShuffleType;
import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.model.OperationReporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecordedShuffleTest {

  @Test
  @DisplayName("mute capture then replay ends on the same permutation")
  void captureAndReplayRoundTrip() {
    ArrayController array = new ArrayController(64);
    array.setShuffleType(ShuffleType.RANDOM);
    array.setDelayContext(() -> {});
    array.setOperationReporter(OperationReporter.NOOP);

    int[] identity = array.getArray().clone();
    RecordedShuffle recorded = array.captureMuteShuffle();
    int[] afterMute = recorded.post();

    assertEquals(identity.length, afterMute.length);
    // Mute shuffle should have changed a random array (extremely likely for n=64).
    assertTrue(recorded.swapCount() > 0);

    array.restoreContents(identity);
    array.replayRecordedShuffle(recorded);
    assertArrayEquals(afterMute, array.getArray());
  }

  @Test
  @DisplayName("sorted shuffle records no swaps and replay restores post state")
  void sortedCaptureHasNoSwaps() {
    ArrayController array = new ArrayController(16);
    array.setShuffleType(ShuffleType.SORTED);
    array.setDelayContext(() -> {});
    array.setOperationReporter(OperationReporter.NOOP);

    int[] before = array.getArray().clone();
    RecordedShuffle recorded = array.captureMuteShuffle();
    assertEquals(0, recorded.swapCount());
    assertArrayEquals(before, recorded.post());

    array.replayRecordedShuffle(recorded);
    assertArrayEquals(before, array.getArray());
  }
}
