package io.github.compilerstuck.control.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InstanceDepthSortTest {

  @Test
  void hasTranslucencyDetectsPartialAlpha() {
    InstanceData data = new InstanceData();
    data.ensureCapacity(2);
    data.set(0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0xFF112233);
    data.set(1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0x80112233);
    data.count = 2;
    assertTrue(InstanceDepthSort.hasTranslucency(data));
  }

  @Test
  void hasTranslucencyTreatsZeroAlphaAsTranslucent() {
    InstanceData data = new InstanceData();
    data.ensureCapacity(1);
    data.set(0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0x00112233);
    data.count = 1;
    assertTrue(InstanceDepthSort.hasTranslucency(data));
  }

  @Test
  void hasTranslucencyFalseWhenFullyOpaque() {
    InstanceData data = new InstanceData();
    data.ensureCapacity(1);
    data.set(0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0xFF112233);
    data.count = 1;
    assertFalse(InstanceDepthSort.hasTranslucency(data));
  }

  @Test
  void backToFrontOrdersFarFirst() {
    InstanceData data = new InstanceData();
    data.ensureCapacity(3);
    data.set(0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0x80FFFFFF); // near cam at z=10
    data.set(1, 0, 0, -100, 1, 1, 1, 0, 0, 0, 0x80FFFFFF); // far
    data.set(2, 0, 0, -50, 1, 1, 1, 0, 0, 0, 0x80FFFFFF); // mid
    data.count = 3;

    InstanceDepthSort sort = new InstanceDepthSort();
    int[] order = sort.backToFrontOrder(data, 0f, 0f, 10f);
    assertEquals(1, order[0]);
    assertEquals(2, order[1]);
    assertEquals(0, order[2]);
  }
}
