package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.HoopsSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

public class Hoops extends Visualization implements ConfigurableVisualization {

  int radius;

  private volatile HoopsSettings settings = HoopsSettings.defaults();

  private int[] radii;
  private int cacheLength = -1;
  private int cacheMaxRadius = -1;
  private float[] xywh;
  private int[] argb;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedN = -1;
  private int cachedCenterX = -1;
  private int cachedCenterY = -1;
  private int cachedMaxRadius = -1;
  private ColorGradient cachedGradient;

  public Hoops(ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Hoops";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof HoopsSettings s) {
      settings = s;
      cacheLength = -1;
      cachedRevision = Long.MIN_VALUE;
    }
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void rebuildRadii(int length, int maxRadius) {
    if (cacheLength == length && cacheMaxRadius == maxRadius) {
      return;
    }
    cacheLength = length;
    cacheMaxRadius = maxRadius;
    if (radii == null || radii.length < length) {
      radii = new int[length];
    }
    for (int i = 0; i < length; i++) {
      radii[i] = (int) VisMath.map(i, 0, length, 0, (float) maxRadius);
    }
  }

  private void ensurePacked(int length, int maxRadius, int centerX, int centerY) {
    long rev = arrayModel.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedN == length
        && cachedCenterX == centerX
        && cachedCenterY == centerY
        && cachedMaxRadius == maxRadius
        && cachedGradient == colorGradient) {
      return;
    }
    if (xywh == null || xywh.length < length * 4) {
      xywh = new float[length * 4];
      argb = new int[length];
    }
    for (int i = 0; i < length; i++) {
      if (arrayModel.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }
      radius = radii[i];
      int o = i * 4;
      xywh[o] = centerX;
      xywh[o + 1] = centerY;
      xywh[o + 2] = radius;
      xywh[o + 3] = radius;
      argb[i] = colorGradient.getMarkerArgb(arrayModel.get(i), arrayModel.getMarker(i));
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedN = length;
    cachedCenterX = centerX;
    cachedCenterY = centerY;
    cachedMaxRadius = maxRadius;
    cachedGradient = colorGradient;
  }

  @Override
  public void update(float delta) {
    HoopsSettings s = settings;
    int length = arrayModel.getLength();
    int maxRadius = (int) (Math.min(screenHeight, screenWidth) * s.radiusScale());
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;
    rebuildRadii(length, maxRadius);
    ensurePacked(length, maxRadius, centerX, centerY);

    rs.strokeWeight(0.5f);
    rs.strokeEllipses(xywh, argb, length);
  }
}
