package io.github._66_m.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github._66_m.control.config.visual.ModelOptions.AssemblyOrder;
import org.junit.jupiter.api.Test;

class ModelMappingTest {

  @Test
  void oneToOneMappingIsTheIdentity() {
    for (int k = 0; k < 50; k++) {
      assertEquals(k, AbstractModelVisualization.elementForPiece(k, 50, 50));
      assertEquals(7, AbstractModelVisualization.sourcePiece(k, 7, 50, 50));
    }
  }

  @Test
  void morePiecesThanElementsKeepsBlockOffsets() {
    // 10 pieces, 5 elements: element e owns pieces 2e and 2e+1.
    assertEquals(2, AbstractModelVisualization.elementForPiece(5, 10, 5));
    // Slot 5 is the second piece of element 2; holding value 4 it shows piece 9.
    assertEquals(9, AbstractModelVisualization.sourcePiece(5, 4, 10, 5));
  }

  @Test
  void sourcePieceStaysInRange() {
    for (int pieces : new int[] {3, 64, 100}) {
      for (int length : new int[] {5, 64, 1000}) {
        for (int k = 0; k < pieces; k++) {
          int s = AbstractModelVisualization.sourcePiece(k, length - 1, pieces, length);
          assertTrue(s >= 0 && s < pieces);
        }
      }
    }
  }

  @Test
  void disparityIsZeroInPlaceAndOneAcrossTheRing() {
    assertEquals(0f, AbstractModelVisualization.disparity(4, 4, 100), 1e-6f);
    assertEquals(1f, AbstractModelVisualization.disparity(0, 50, 100), 1e-6f);
  }

  @Test
  void heightScatterTakesOnlyTheValuesHeight() {
    float[] pos = {1, 2, 3, 4, 5, 6};
    float[] out = new float[3];
    AbstractModelVisualization.scatterPoint(pos, 0, 1, AssemblyOrder.HEIGHT, out);
    assertEquals(1f, out[0]);
    assertEquals(5f, out[1]);
    assertEquals(3f, out[2]);
  }
}
