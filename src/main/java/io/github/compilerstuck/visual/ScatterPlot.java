package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.ScatterPlotSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class ScatterPlot extends Visualization implements ConfigurableVisualization {

  private final IndexXCache indexXCache = new IndexXCache();

  private volatile ScatterPlotSettings settings = ScatterPlotSettings.defaults();

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedN = -1;
  private int[] barHeights;
  private float[] xyd;
  private int[] argb;

  public ScatterPlot(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Scatter Plot";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof ScatterPlotSettings s) {
      settings = s;
    }
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void ensureHeights(int n, int heightScale) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedN == n) {
      return;
    }
    if (barHeights == null || barHeights.length < n) {
      barHeights = new int[n];
    }
    for (int i = 0; i < n; i++) {
      barHeights[i] = (arrayController.get(i) + 1) * heightScale / n;
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedN = n;
  }

  @Override
  public void update(float delta) {
    ScatterPlotSettings s = settings;
    float pointSize = (float) s.pointSize();
    int n = arrayController.getLength();
    int heightScale = screenHeight - 5;

    indexXCache.ensure(n, screenWidth);
    float[] xs = indexXCache.xs();
    ensureHeights(n, heightScale);

    if (xyd == null || xyd.length < n * 3) {
      xyd = new float[n * 3];
      argb = new int[n];
    }

    for (int i = 0; i < n; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      int o = i * 3;
      xyd[o] = xs[i];
      xyd[o + 1] = barHeights[i];
      xyd[o + 2] = pointSize;
      argb[i] = color.getRGB();
    }
    rs.fillCircles(xyd, argb, n);
  }
}
