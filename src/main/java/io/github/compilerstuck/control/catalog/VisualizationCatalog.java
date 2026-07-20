package io.github.compilerstuck.control.catalog;

import io.github.compilerstuck.visual.Bars;
import io.github.compilerstuck.visual.Circle;
import io.github.compilerstuck.visual.ColorGradientGraph;
import io.github.compilerstuck.visual.Cube;
import io.github.compilerstuck.visual.CubicLines;
import io.github.compilerstuck.visual.DisparityChords;
import io.github.compilerstuck.visual.DisparityCircle;
import io.github.compilerstuck.visual.DisparityCircleScatter;
import io.github.compilerstuck.visual.DisparityCircleScatterLinked;
import io.github.compilerstuck.visual.DisparityGraph;
import io.github.compilerstuck.visual.DisparityGraphMirrored;
import io.github.compilerstuck.visual.DisparityPlane;
import io.github.compilerstuck.visual.DisparitySphereHoops;
import io.github.compilerstuck.visual.DisparitySquareScatter;
import io.github.compilerstuck.visual.Hoops;
import io.github.compilerstuck.visual.HorizontalPyramid;
import io.github.compilerstuck.visual.ImageHorizontal;
import io.github.compilerstuck.visual.ImageVertical;
import io.github.compilerstuck.visual.MorphingShell;
import io.github.compilerstuck.visual.MosaicSquares;
import io.github.compilerstuck.visual.NumberPlot;
import io.github.compilerstuck.visual.Phyllotaxis;
import io.github.compilerstuck.visual.Plane;
import io.github.compilerstuck.visual.Pyramid;
import io.github.compilerstuck.visual.ScatterPlot;
import io.github.compilerstuck.visual.ScatterPlotLinked;
import io.github.compilerstuck.visual.Sphere;
import io.github.compilerstuck.visual.SphereHoops;
import io.github.compilerstuck.visual.SphericDisparityLines;
import io.github.compilerstuck.visual.SwirlDots;
import java.util.List;

/** Live visualization registry for {@link io.github.compilerstuck.control.render.RenderSystem}. */
public final class VisualizationCatalog {

  private VisualizationCatalog() {}

  public static List<VisualizationDescriptor> all() {
    return List.of(
        new VisualizationDescriptor(
            "bars", "Bars", VisualConstraints.NONE, (a, g, s, rs) -> new Bars(a, g, s, rs)),
        new VisualizationDescriptor(
            "scatter-plot",
            "Scatter Plot",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new ScatterPlot(a, g, s, rs)),
        new VisualizationDescriptor(
            "scatter-plot-linked",
            "Scatter Plot Linked",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new ScatterPlotLinked(a, g, s, rs)),
        new VisualizationDescriptor(
            "number-plot",
            "Number Plot",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new NumberPlot(a, g, s, rs)),
        new VisualizationDescriptor(
            "disparity-graph",
            "Disparity Graph",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new DisparityGraph(a, g, s, rs)),
        new VisualizationDescriptor(
            "disparity-graph-mirrored",
            "Disparity Graph Mirrored",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new DisparityGraphMirrored(a, g, s, rs)),
        new VisualizationDescriptor(
            "horizontal-pyramid",
            "Horizontal Pyramid",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new HorizontalPyramid(a, g, s, rs)),
        new VisualizationDescriptor(
            "color-gradient-graph",
            "Color Gradient Graph",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new ColorGradientGraph(a, g, s, rs)),
        new VisualizationDescriptor(
            "circle", "Circle", VisualConstraints.NONE, (a, g, s, rs) -> new Circle(a, g, s, rs)),
        new VisualizationDescriptor(
            "disparity-circle",
            "Disparity Circle",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new DisparityCircle(a, g, s, rs)),
        new VisualizationDescriptor(
            "disparity-circle-scatter",
            "Disparity Circle Scatter",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new DisparityCircleScatter(a, g, s, rs)),
        new VisualizationDescriptor(
            "disparity-circle-scatter-linked",
            "Disparity Circle Scatter Linked",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new DisparityCircleScatterLinked(a, g, s, rs)),
        new VisualizationDescriptor(
            "disparity-chords",
            "Disparity Chords",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new DisparityChords(a, g, s, rs)),
        new VisualizationDescriptor(
            "disparity-square-scatter",
            "Disparity Square Scatter",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new DisparitySquareScatter(a, g, s, rs)),
        new VisualizationDescriptor(
            "swirl-dots",
            "Swirl Dots",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new SwirlDots(a, g, s, rs)),
        new VisualizationDescriptor(
            "phyllotaxis",
            "Phyllotaxis",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new Phyllotaxis(a, g, s, rs)),
        new VisualizationDescriptor(
            "mosaic-squares",
            "Mosaic Squares",
            VisualConstraints.SQUARE,
            (a, g, s, rs) -> new MosaicSquares(a, g, s, rs)),
        new VisualizationDescriptor(
            "image-vertical",
            "Image - Vertical Sorting",
            VisualConstraints.IMAGE,
            (a, g, s, rs) -> new ImageVertical(a, g, s, rs)),
        new VisualizationDescriptor(
            "image-horizontal",
            "Image - Horizontal Sorting",
            VisualConstraints.IMAGE,
            (a, g, s, rs) -> new ImageHorizontal(a, g, s, rs)),
        new VisualizationDescriptor(
            "hoops", "Hoops", VisualConstraints.NONE, (a, g, s, rs) -> new Hoops(a, g, s, rs)),
        new VisualizationDescriptor(
            "morphing-shell",
            "3D - Morphing Shell",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new MorphingShell(a, g, s, rs)),
        new VisualizationDescriptor(
            "sphere",
            "3D - Sphere",
            VisualConstraints.SQUARE,
            (a, g, s, rs) -> new Sphere(a, g, s, rs)),
        new VisualizationDescriptor(
            "sphere-hoops",
            "3D - Sphere Hoops",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new SphereHoops(a, g, s, rs)),
        new VisualizationDescriptor(
            "spheric-disparity-lines",
            "3D - Spheric Disparity Lines",
            VisualConstraints.SQUARE,
            (a, g, s, rs) -> new SphericDisparityLines(a, g, s, rs)),
        new VisualizationDescriptor(
            "disparity-sphere-hoops",
            "3D - Disparity Sphere Hoops",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new DisparitySphereHoops(a, g, s, rs)),
        new VisualizationDescriptor(
            "cube", "3D - Cube", VisualConstraints.CUBE, (a, g, s, rs) -> new Cube(a, g, s, rs)),
        new VisualizationDescriptor(
            "cubic-lines",
            "3D - Cubic Lines",
            VisualConstraints.CUBE,
            (a, g, s, rs) -> new CubicLines(a, g, s, rs)),
        new VisualizationDescriptor(
            "pyramid",
            "3D - Pyramid",
            VisualConstraints.NONE,
            (a, g, s, rs) -> new Pyramid(a, g, s, rs)),
        new VisualizationDescriptor(
            "plane",
            "3D - Plane",
            VisualConstraints.SQUARE,
            (a, g, s, rs) -> new Plane(a, g, s, rs)),
        new VisualizationDescriptor(
            "disparity-plane",
            "3D - Disparity Plane",
            VisualConstraints.SQUARE,
            (a, g, s, rs) -> new DisparityPlane(a, g, s, rs)));
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
