package io.github.compilerstuck.control;

import io.github.compilerstuck.control.catalog.VisualizationCatalog;
import io.github.compilerstuck.control.catalog.VisualizationDescriptor;
import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.render.HeadlessRenderContext;
import io.github.compilerstuck.sound.HeadlessSound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import io.github.compilerstuck.visual.Visualization;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.awt.Color;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Headless smoke test: every registered visualization must be constructible
 * and able to run one {@code update()} without throwing, using a headless
 * render context (no real Processing/Swing runtime).
 */
class VisualizationCatalogSmokeTest {

    @TestFactory
    Stream<DynamicTest> everyVisualizationUpdatesHeadlessly() {
        int size = 16; // perfect square and reasonably sized for grid visuals
        List<VisualizationDescriptor> descriptors = VisualizationCatalog.all();

        return descriptors.stream().map(descriptor -> DynamicTest.dynamicTest(descriptor.id(), () -> {
            ArrayController controller = new ArrayController(size);
            HeadlessRenderContext renderContext = new HeadlessRenderContext(400, 300);
            HeadlessSound sound = new HeadlessSound(controller);
            ColorGradient gradient = new ColorGradient(Color.BLACK, Color.WHITE, Color.RED, "smoke-test", size);

            Visualization visualization = descriptor.factory()
                    .create(controller, gradient, sound, renderContext);

            if (descriptor.constraints().requiresImage()) {
                // Image visuals rely on a real loaded image being resized to the window
                // dimensions; the headless dummy image is always 1x1, so update() isn't
                // representative here. Just verify construction succeeded.
                return;
            }

            assertDoesNotThrow(visualization::update,
                    descriptor.id() + " should update without throwing");
        }));
    }
}
