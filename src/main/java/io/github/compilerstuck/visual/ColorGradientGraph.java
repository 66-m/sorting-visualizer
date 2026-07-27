package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.ColorGradientGraphSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class ColorGradientGraph extends Visualization implements ConfigurableVisualization {

  private static final float SEAM_OVERLAP = 1f;

  private volatile ColorGradientGraphSettings settings = ColorGradientGraphSettings.defaults();

  private float[] xywh;
  private int[] argb;

  public ColorGradientGraph(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Color Gradient Graph";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof ColorGradientGraphSettings s) {
      settings = s;
    }
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  @Override
  public void update(float delta) {
    int length = arrayModel.getLength();
    float slotWidth = (float) screenWidth / length;

    if (xywh == null || xywh.length < length * 4) {
      xywh = new float[length * 4];
      argb = new int[length];
    }

    for (int i = 0; i < length; i++) {
      Color color = colorGradient.getMarkerColor(arrayModel.get(i), arrayModel.getMarker(i));

      if (arrayModel.getMarker(i) == Marker.SET) {
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

    if (settings.showIndexDividers() && length > 1) {
      float[] div = new float[Math.max(4, (length - 1) * 4)];
      int[] divArgb = new int[Math.max(1, length - 1)];
      for (int i = 1; i < length; i++) {
        float x = i * slotWidth;
        int o = (i - 1) * 4;
        div[o] = x;
        div[o + 1] = 0;
        div[o + 2] = x;
        div[o + 3] = screenHeight;
        divArgb[i - 1] = 0x66000000;
      }
      rs.strokeWeight(1f);
      rs.strokeLines(div, divArgb, length - 1);
    }
  }
}
