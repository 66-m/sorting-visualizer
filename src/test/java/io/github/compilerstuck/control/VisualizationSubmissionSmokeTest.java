package io.github.compilerstuck.control;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.control.catalog.VisualizationCatalog;
import io.github.compilerstuck.control.catalog.VisualizationDescriptor;
import io.github.compilerstuck.control.config.visual.CubeSettings;
import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.model.SnapshotPublisher;
import io.github.compilerstuck.control.render.FakeRenderSystem;
import io.github.compilerstuck.sound.HeadlessSound;
import io.github.compilerstuck.visual.ConfigurableVisualization;
import io.github.compilerstuck.visual.Visualization;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Headless submission smoke: catalog renders at N=64, 3D visuals must submit 3D geometry, and named
 * visuals match expected FakeRenderSystem counters.
 */
class VisualizationSubmissionSmokeTest {

  @TestFactory
  Stream<DynamicTest> everyVisualizationRendersAt64() {
    return VisualizationCatalog.all().stream()
        .map(
            descriptor ->
                DynamicTest.dynamicTest(
                    descriptor.id() + " @64",
                    () -> {
                      FakeRenderSystem rs =
                          assertDoesNotThrow(
                              () -> renderOnce(descriptor, 64, 400, 300),
                              descriptor.id() + " should render without throwing");
                      if (descriptor.displayName().startsWith("3D ")) {
                        assertTrue(
                            rs.begin3DCount() >= 1 || rs.total3DPrimitives() > 0,
                            descriptor.id() + " should submit 3D geometry");
                      }
                    }));
  }

  @Test
  void barsSubmitsOneRectPerElement() {
    FakeRenderSystem rs = renderOnce(VisualizationCatalog.findById("bars"), 256, 800, 400);
    assertEquals(256, rs.rectCount());
  }

  @Test
  void cubeSubmitsBoxes() {
    FakeRenderSystem rs = renderOnce(VisualizationCatalog.findById("cube"), 64, 400, 400);
    assertTrue(rs.boxInstances() > 0);
    assertTrue(rs.begin3DCount() >= 1);
  }

  @Test
  void cubeFillOpacityZeroSkipsBoxesButKeepsWireframe() {
    FakeRenderSystem rs =
        renderOnceWithCubeSettings(
            64,
            400,
            400,
            new CubeSettings(
                CubeSettings.DEFAULT_ROTATION_SPEED,
                0,
                true,
                CubeSettings.DEFAULT_SCENE_SCALE_DIVISOR));
    assertEquals(0, rs.boxInstances());
    assertTrue(rs.line3DCount() > 0);
    assertTrue(rs.begin3DCount() >= 1);
  }

  @Test
  void cubeFillOpacityOpaqueStillSubmitsBoxes() {
    FakeRenderSystem rs =
        renderOnceWithCubeSettings(
            64,
            400,
            400,
            new CubeSettings(
                CubeSettings.DEFAULT_ROTATION_SPEED,
                CubeSettings.FILL_OPACITY_MAX,
                true,
                CubeSettings.DEFAULT_SCENE_SCALE_DIVISOR));
    assertTrue(rs.boxInstances() > 0);
    assertTrue(rs.line3DCount() > 0);
  }

  @Test
  void numberPlotSubmitsTextPerElement() {
    FakeRenderSystem rs = renderOnce(VisualizationCatalog.findById("number-plot"), 64, 800, 400);
    assertTrue(rs.textCount() >= 64);
  }

  @Test
  void imageHorizontalUploadsPixelsFromBlankBuffer() {
    FakeRenderSystem rs =
        renderOnce(VisualizationCatalog.findById("image-horizontal"), 64, 400, 300);
    assertTrue(rs.pixelUploadCount() >= 1);
  }

  private static FakeRenderSystem renderOnce(
      VisualizationDescriptor descriptor, int size, int width, int height) {
    ArrayController controller = new ArrayController(size);
    SnapshotPublisher publisher = new SnapshotPublisher();
    publisher.publish(controller);
    FakeRenderSystem rs = new FakeRenderSystem(width, height);
    HeadlessSound sound = new HeadlessSound(publisher.publishedView());
    ColorGradient gradient =
        new ColorGradient(Color.BLACK, Color.WHITE, Color.RED, "submission-smoke", size);
    Visualization visualization =
        descriptor.factory().create(publisher.publishedView(), gradient, sound, rs);
    rs.resetCounts();
    visualization.render(1f / 60f);
    return rs;
  }

  private static FakeRenderSystem renderOnceWithCubeSettings(
      int size, int width, int height, CubeSettings settings) {
    ArrayController controller = new ArrayController(size);
    SnapshotPublisher publisher = new SnapshotPublisher();
    publisher.publish(controller);
    FakeRenderSystem rs = new FakeRenderSystem(width, height);
    HeadlessSound sound = new HeadlessSound(publisher.publishedView());
    ColorGradient gradient =
        new ColorGradient(Color.BLACK, Color.WHITE, Color.RED, "submission-smoke", size);
    Visualization visualization =
        VisualizationCatalog.findById("cube")
            .factory()
            .create(publisher.publishedView(), gradient, sound, rs);
    ((ConfigurableVisualization) visualization).applySettings(settings);
    rs.resetCounts();
    visualization.render(1f / 60f);
    return rs;
  }
}
