package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class ColorGradientGraph extends Visualization {

  private static final float SEAM_OVERLAP = 1f;

  private float[] xywh;
  private int[] argb;

  public ColorGradientGraph(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Color Gradient Graph";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  @Override
  public void update(float delta) {
    int length = arrayController.getLength();
    float slotWidth = (float) screenWidth / length;

    if (xywh == null || xywh.length < length * 4) {
      xywh = new float[length * 4];
      argb = new int[length];
    }

    for (int i = 0; i < length; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      float x0 = i * slotWidth;
      float x1 = Math.min(screenWidth, (i + 1) * slotWidth + SEAM_OVERLAP);
      int o = i * 4;
      xywh[o] = x0;
      xywh[o + 1] = 0;
      xywh[o + 2] = x1 - x0;
      xywh[o + 3] = screenHeight;
      argb[i] = color.getRGB();
    }
    rs.fillRects(xywh, argb, length);
  }
}
