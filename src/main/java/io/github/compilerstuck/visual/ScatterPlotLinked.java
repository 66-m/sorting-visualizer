package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.ScatterPlotLinkedSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class ScatterPlotLinked extends Visualization implements ConfigurableVisualization {

  private final IndexXCache indexXCache = new IndexXCache();

  private volatile ScatterPlotLinkedSettings settings = ScatterPlotLinkedSettings.defaults();

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedN = -1;
  private int[] barHeights;
  private float[] xyxy;
  private int[] argb;

  public ScatterPlotLinked(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Scatter Plot Linked";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof ScatterPlotLinkedSettings s) {
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
    ScatterPlotLinkedSettings s = settings;
    int n = arrayController.getLength();
    int heightScale = screenHeight - 5;

    indexXCache.ensure(n, screenWidth);
    float[] xs = indexXCache.xs();
    ensureHeights(n, heightScale);

    int lineCount = Math.max(0, n - 1);
    if (xyxy == null || xyxy.length < lineCount * 4) {
      xyxy = new float[Math.max(4, lineCount * 4)];
      argb = new int[Math.max(1, lineCount)];
    }

    for (int i = 0; i < lineCount; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      int o = i * 4;
      xyxy[o] = xs[i];
      xyxy[o + 1] = barHeights[i];
      xyxy[o + 2] = xs[i + 1];
      xyxy[o + 3] = barHeights[i + 1];
      argb[i] = color.getRGB();
    }
    rs.strokeWeight((float) s.lineThickness());
    rs.strokeLines(xyxy, argb, lineCount);
  }
}
