package io.github.compilerstuck.control.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.model.SortingStateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HudRendererTest {

  private HudRenderer hud;
  private FakeRenderSystem rs;
  private ArrayController array;
  private SortingStateManager state;

  @BeforeEach
  void setUp() {
    hud = new HudRenderer();
    rs = new FakeRenderSystem(1280, 720);
    array = new ArrayController(16);
    state = new SortingStateManager();
    state.setCurrentOperation("Waiting");
  }

  @Test
  void secondDrawWithUnchangedMetricsReusesLabelInstances() {
    hud.drawMeasurements(rs, state, array);
    String[] first = hud.labelsForTest();
    String[] snapshot = new String[first.length];
    System.arraycopy(first, 0, snapshot, 0, first.length);

    rs.resetCounts();
    hud.drawMeasurements(rs, state, array);

    String[] second = hud.labelsForTest();
    assertSame(first, second);
    for (int i = 0; i < snapshot.length; i++) {
      assertSame(snapshot[i], second[i], "label[" + i + "] should be reused");
    }
    assertTrue(hud.wouldSkipRebuild(state, array, rs.getWidth()));
    assertEquals(8, rs.textCount());
  }

  @Test
  void metricChangeRebuildsThenSkipsAgain() {
    hud.drawMeasurements(rs, state, array);
    assertTrue(hud.wouldSkipRebuild(state, array, rs.getWidth()));

    state.setCurrentOperation("Bubble Sort");
    assertFalse(hud.wouldSkipRebuild(state, array, rs.getWidth()));

    hud.drawMeasurements(rs, state, array);
    assertEquals("Bubble Sort", hud.labelsForTest()[0]);
    assertTrue(hud.wouldSkipRebuild(state, array, rs.getWidth()));
  }

  @Test
  void prepareKeepsEndOfShuffleMetricsFrozenAgainstCounterChurn() {
    for (int i = 0; i < 16; i++) {
      array.set(i, 15 - i);
    }
    array.update();
    state.setCurrentOperation("Shuffling");
    hud.drawMeasurements(rs, state, array);
    String sortedLine = hud.labelsForTest()[1];
    String comparisonsLine = hud.labelsForTest()[2];

    state.setCurrentOperation("Prepare.. 0%");
    state.setEqualizePreparing(true);
    array.addComparisons(1_000);
    array.addWritesAux(50);
    hud.drawMeasurements(rs, state, array);

    String[] labels = hud.labelsForTest();
    assertEquals("Prepare.. 0%", labels[0]);
    assertEquals(sortedLine, labels[1], "sorted % must stay at end-of-shuffle value");
    assertEquals(comparisonsLine, labels[2], "counters must stay frozen during Prepare..");
    assertEquals("16 Elements", labels[7]);

    state.setCurrentOperation("Prepare.. 42%");
    hud.drawMeasurements(rs, state, array);
    assertEquals("Prepare.. 42%", hud.labelsForTest()[0]);
    assertEquals(sortedLine, hud.labelsForTest()[1]);
    assertTrue(hud.wouldSkipRebuild(state, array, rs.getWidth()));
  }
}
