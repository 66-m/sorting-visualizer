package io.github.compilerstuck.control.catalog;

import io.github.compilerstuck.visual.*;
import java.util.List;

/**
 * Static registry of all visualizations available to the Settings UI, in the order they were
 * historically presented.
 */
public final class VisualizationCatalog {

  private VisualizationCatalog() {}

  public static List<VisualizationDescriptor> all() {
    return List.of(
        new VisualizationDescriptor("bars", "Bars", VisualConstraints.NONE, Bars::new),
        new VisualizationDescriptor(
            "scatter-plot", "Scatter Plot", VisualConstraints.NONE, ScatterPlot::new),
        new VisualizationDescriptor(
            "scatter-plot-linked",
            "Scatter Plot Linked",
            VisualConstraints.NONE,
            ScatterPlotLinked::new),
        new VisualizationDescriptor(
            "number-plot", "Number Plot", VisualConstraints.NONE, NumberPlot::new),
        new VisualizationDescriptor(
            "disparity-graph", "Disparity Graph", VisualConstraints.NONE, DisparityGraph::new),
        new VisualizationDescriptor(
            "disparity-graph-mirrored",
            "Disparity Graph Mirrored",
            VisualConstraints.NONE,
            DisparityGraphMirrored::new),
        new VisualizationDescriptor(
            "horizontal-pyramid",
            "Horizontal Pyramid",
            VisualConstraints.NONE,
            HorizontalPyramid::new),
        new VisualizationDescriptor(
            "color-gradient-graph",
            "Color Gradient Graph",
            VisualConstraints.NONE,
            ColorGradientGraph::new),
        new VisualizationDescriptor("circle", "Circle", VisualConstraints.NONE, Circle::new),
        new VisualizationDescriptor(
            "disparity-circle", "Disparity Circle", VisualConstraints.NONE, DisparityCircle::new),
        new VisualizationDescriptor(
            "disparity-circle-scatter",
            "Disparity Circle Scatter",
            VisualConstraints.NONE,
            DisparityCircleScatter::new),
        new VisualizationDescriptor(
            "disparity-circle-scatter-linked",
            "Disparity Circle Scatter Linked",
            VisualConstraints.NONE,
            DisparityCircleScatterLinked::new),
        new VisualizationDescriptor(
            "disparity-chords", "Disparity Chords", VisualConstraints.NONE, DisparityChords::new),
        new VisualizationDescriptor(
            "disparity-square-scatter",
            "Disparity Square Scatter",
            VisualConstraints.NONE,
            DisparitySquareScatter::new),
        new VisualizationDescriptor(
            "swirl-dots", "Swirl Dots", VisualConstraints.NONE, SwirlDots::new),
        new VisualizationDescriptor(
            "phyllotaxis", "Phyllotaxis", VisualConstraints.NONE, Phyllotaxis::new),
        new VisualizationDescriptor(
            "image-vertical",
            "Image - Vertical Sorting",
            VisualConstraints.IMAGE,
            ImageVertical::new),
        new VisualizationDescriptor(
            "image-horizontal",
            "Image - Horizontal Sorting",
            VisualConstraints.IMAGE,
            ImageHorizontal::new),
        new VisualizationDescriptor("hoops", "Hoops", VisualConstraints.NONE, Hoops::new),
        new VisualizationDescriptor(
            "morphing-shell",
            "3D - Morphing Shell",
            VisualConstraints.NONE,
            MorphingShell::new),
        new VisualizationDescriptor("sphere", "3D - Sphere", VisualConstraints.SQUARE, Sphere::new),
        new VisualizationDescriptor(
            "sphere-hoops", "3D - Sphere Hoops", VisualConstraints.NONE, SphereHoops::new),
        new VisualizationDescriptor(
            "spheric-disparity-lines",
            "3D - Spheric Disparity Lines",
            VisualConstraints.SQUARE,
            SphericDisparityLines::new),
        new VisualizationDescriptor(
            "disparity-sphere-hoops",
            "3D - Disparity Sphere Hoops",
            VisualConstraints.NONE,
            DisparitySphereHoops::new),
        new VisualizationDescriptor("cube", "3D - Cube", VisualConstraints.CUBE, Cube::new),
        new VisualizationDescriptor(
            "cubic-lines", "3D - Cubic Lines", VisualConstraints.CUBE, CubicLines::new),
        new VisualizationDescriptor(
            "pyramid", "3D - Pyramid", VisualConstraints.NONE, Pyramid::new),
        new VisualizationDescriptor("plane", "3D - Plane", VisualConstraints.SQUARE, Plane::new),
        new VisualizationDescriptor(
            "disparity-plane",
            "3D - Disparity Plane",
            VisualConstraints.SQUARE,
            DisparityPlane::new),
        new VisualizationDescriptor(
            "mosaic-squares", "Mosaic Squares", VisualConstraints.SQUARE, MosaicSquares::new));
  }

  public static VisualizationDescriptor findById(String id) {
    return all().stream().filter(d -> d.id().equals(id)).findFirst().orElse(all().get(0));
  }

  public static int indexOfId(String id) {
    List<VisualizationDescriptor> list = all();
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).id().equals(id)) {
        return i;
      }
    }
    return 0;
  }
}
