package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.InstanceData;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class Pyramid extends Visualization {

  private float angle = 0;

  private final InstanceData quads = new InstanceData();

  private float[] zOffsets;
  private int[] barHeights;
  private int[] colorsRgb;
  private int zOffsetLength = -1;
  private int zOffsetRadius = -1;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedLength = -1;
  private int cachedRadius = -1;
  private ColorGradient cachedGradient;

  public Pyramid(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "3D - Pyramid";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void rebuildZOffsets(int length, int radius) {
    if (zOffsetLength == length && zOffsetRadius == radius) {
      return;
    }
    zOffsetLength = length;
    zOffsetRadius = radius;
    if (zOffsets == null || zOffsets.length < length) {
      zOffsets = new float[length];
    }
    for (int i = 0; i < length; i++) {
      zOffsets[i] = radius / 2 - VisMath.map(i, 0, length, 0, radius);
    }
  }

  private void ensureBarHeightsAndColors(int length, int radius) {
    long rev = arrayController.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedLength == length
        && cachedRadius == radius
        && cachedGradient == colorGradient) {
      return;
    }
    if (barHeights == null || barHeights.length < length) {
      barHeights = new int[length];
      colorsRgb = new int[length];
    }
    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));

      barHeights[i] = (value + 1) * (radius - 5) / length;

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      colorsRgb[i] = color.getRGB();
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedLength = length;
    cachedRadius = radius;
    cachedGradient = colorGradient;
  }

  @Override
  public void update(float delta) {
    int screenMin = Math.min(screenHeight, screenWidth);
    int radius = (int) (screenMin / 1.7);
    int length = arrayController.getLength();
    float centerY = (float) (screenHeight / 2.5);
    float centerZ = -(int) (screenMin / 10);
    float centerX = (float) screenWidth / 2;

    angle -= (float) (Math.PI / 15) * delta;

    rebuildZOffsets(length, radius);
    ensureBarHeightsAndColors(length, radius);

    float rotX = VisMath.PI / 3;
    float cosX = (float) Math.cos(rotX);
    float sinX = (float) Math.sin(rotX);
    float cosZ = (float) Math.cos(angle);
    float sinZ = (float) Math.sin(angle);

    quads.ensureCapacity(length);
    for (int i = 0; i < length; i++) {
      float tz = zOffsets[i];
      // Local offset (0,0,tz) → rotateZ → rotateX → center
      float x1 = 0;
      float y1 = 0;
      float z1 = tz;
      float x2 = cosZ * x1 - sinZ * y1;
      float y2 = sinZ * x1 + cosZ * y1;
      float z2 = z1;
      float y3 = cosX * y2 - sinX * z2;
      float z3 = sinX * y2 + cosX * z2;

      float s = barHeights[i];
      quads.set(
          i,
          toWorldX(centerX + x2),
          toWorldY(centerY + y3),
          centerZ + z3,
          s,
          s,
          1f,
          -rotX,
          0f,
          -angle,
          colorsRgb[i]);
    }
    quads.count = length;

    rs.begin3D();
    rs.drawQuads(quads);
    rs.end3D();
  }
}
