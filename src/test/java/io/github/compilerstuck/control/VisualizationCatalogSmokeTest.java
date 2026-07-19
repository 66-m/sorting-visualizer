package io.github.compilerstuck.control;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.github.compilerstuck.control.catalog.VisualizationCatalog;
import io.github.compilerstuck.control.catalog.VisualizationDescriptor;
import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.model.SnapshotPublisher;
import io.github.compilerstuck.control.render.FakeRenderSystem;
import io.github.compilerstuck.sound.HeadlessSound;
import io.github.compilerstuck.visual.Visualization;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Headless smoke test: every live visualization must construct and run one {@code render} without
 * throwing ({@link FakeRenderSystem}, no GPU).
 */
class VisualizationCatalogSmokeTest {

  @TestFactory
  Stream<DynamicTest> everyVisualizationUpdatesHeadlessly() {
    int size = 16;
    List<VisualizationDescriptor> descriptors = VisualizationCatalog.all();

    return descriptors.stream()
        .map(
            descriptor ->
                DynamicTest.dynamicTest(
                    descriptor.id(),
                    () -> {
                      ArrayController controller = new ArrayController(size);
                      SnapshotPublisher publisher = new SnapshotPublisher();
                      publisher.publish(controller);
                      FakeRenderSystem rs = new FakeRenderSystem(400, 300);
                      HeadlessSound sound = new HeadlessSound(publisher.publishedView());
                      ColorGradient gradient =
                          new ColorGradient(
                              Color.BLACK, Color.WHITE, Color.RED, "smoke-test", size);

                      Visualization visualization =
                          descriptor
                              .factory()
                              .create(publisher.publishedView(), gradient, sound, rs);

                      assertDoesNotThrow(
                          () -> visualization.render(1f / 60f),
                          descriptor.id() + " should update without throwing");
                    }));
  }
}
