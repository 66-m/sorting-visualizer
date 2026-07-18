package io.github.compilerstuck.control.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayControllerMetricsCacheTest {

    @Test
    @DisplayName("update is a no-op when metrics are clean")
    void updateSkipsWhenClean() {
        ArrayController controller = new ArrayController(8);
        assertTrue(controller.isMetricsDirty());
        controller.update();
        assertFalse(controller.isMetricsDirty());
        double pct = controller.getSortedPercentage();
        controller.update(); // should not change
        assertEquals(pct, controller.getSortedPercentage());
        assertFalse(controller.isMetricsDirty());
    }

    @Test
    @DisplayName("set and swap mark metrics dirty")
    void mutationsMarkDirty() {
        ArrayController controller = new ArrayController(8);
        controller.update();
        assertFalse(controller.isMetricsDirty());

        controller.swap(0, 7);
        assertTrue(controller.isMetricsDirty());
        controller.update();
        assertFalse(controller.isMetricsDirty());

        controller.set(1, 3);
        assertTrue(controller.isMetricsDirty());
    }
}
