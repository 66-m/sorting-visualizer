package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.DisparityChordsSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

public class DisparityChords extends Visualization implements ConfigurableVisualization {

  int radius;
  private final PhaseLut phaseLut = new PhaseLut();

  private volatile DisparityChordsSettings settings = DisparityChordsSettings.defaults();

  private int[] endX;
  private int[] endY;
  private int cacheLength = -1;
  private int cacheRadius = -1;
  private int cacheCenterX = -1;
  private int cacheCenterY = -1;
  private float[] xyxy;
  private int[] lineArgb;
  private float[] ellipseXywh;
  private int[] ellipseArgb;
  private int lineCount;
  private int ellipseCount;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedLength = -1;
  private int cachedRadius = -1;
  private int cachedOpacity = -1;
  private double cachedMarkerSize = Double.NaN;
  private ColorGradient cachedGradient;

  public DisparityChords(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Disparity Chords";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof DisparityChordsSettings s) {
      settings = s;
      cacheLength = -1;
      cachedRevision = Long.MIN_VALUE;
    }
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void rebuildRingPositions(int length, int radius, int centerX, int centerY) {
    if (cacheLength == length
        && cacheRadius == radius
        && cacheCenterX == centerX
        && cacheCenterY == centerY) {
      return;
    }
    cacheLength = length;
    cacheRadius = radius;
    cacheCenterX = centerX;
    cacheCenterY = centerY;

    if (endX == null || endX.length < length) {
      endX = new int[length];
      endY = new int[length];
    }

    phaseLut.ensure(length, 1.0);
    float[] sin = phaseLut.sin();
    float[] cos = phaseLut.cos();
    for (int i = 0; i < length; i++) {
      endX[i] = centerX + (int) (radius * sin[i]);
      endY[i] = centerY + (int) (radius * cos[i]);
    }
  }

  private void ensurePacked(int length, int radius, DisparityChordsSettings s) {
    long rev = arrayModel.getVisualRevision();
    int opacity = s.chordOpacity();
    double markerSize = s.coincidentMarkerSize();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedLength == length
        && cachedRadius == radius
        && cachedOpacity == opacity
        && cachedMarkerSize == markerSize
        && cachedGradient == colorGradient) {
      return;
    }

    if (xyxy == null || xyxy.length < length * 4) {
      xyxy = new float[length * 4];
      lineArgb = new int[length];
      ellipseXywh = new float[length * 4];
      ellipseArgb = new int[length];
    }

    lineCount = 0;
    ellipseCount = 0;
    float marker = (float) markerSize;

    for (int i = 0; i < length; i++) {
      int value = arrayModel.get(i);
      int rgb = colorGradient.getMarkerArgb(value, arrayModel.getMarker(i));

      if (arrayModel.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      int x = endX[i];
      int y = endY[i];
      int x2 = endX[value];
      int y2 = endY[value];

      if (x == x2 && y == y2) {
        int o = ellipseCount * 4;
        ellipseXywh[o] = x;
        ellipseXywh[o + 1] = y;
        ellipseXywh[o + 2] = marker;
        ellipseXywh[o + 3] = marker;
        ellipseArgb[ellipseCount] = VisColors.withAlpha(rgb, opacity);
        ellipseCount++;
      } else {
        int o = lineCount * 4;
        xyxy[o] = x;
        xyxy[o + 1] = y;
        xyxy[o + 2] = x2;
        xyxy[o + 3] = y2;
        lineArgb[lineCount] = VisColors.withAlpha(rgb, opacity);
        lineCount++;
      }
    }

    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedLength = length;
    cachedRadius = radius;
    cachedOpacity = opacity;
    cachedMarkerSize = markerSize;
    cachedGradient = colorGradient;
  }

  @Override
  public void update(float delta) {
    int length = arrayModel.getLength();
    DisparityChordsSettings s = settings;
    radius = (int) (Math.min(screenHeight, screenWidth) * s.radiusScale());
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;

    rebuildRingPositions(length, radius, centerX, centerY);
    ensurePacked(length, radius, s);

    rs.strokeWeight((float) s.lineThickness());
    rs.strokeLines(xyxy, lineArgb, lineCount);
    rs.strokeEllipses(ellipseXywh, ellipseArgb, ellipseCount);
  }
}
