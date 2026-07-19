package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class DisparitySquareScatter extends Visualization {

  int sideLength;

  private int[] baseX;
  private int[] baseY;
  private int cacheLength = -1;
  private int cacheSideX = -1;
  private int cacheSideY = -1;
  private float[] xyd;
  private int[] argb;

  public DisparitySquareScatter(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Disparity Square Scatter";
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void rebuildPerimeter(int length, int sideLengthX, int sideLengthY) {
    if (cacheLength == length && cacheSideX == sideLengthX && cacheSideY == sideLengthY) {
      return;
    }
    cacheLength = length;
    cacheSideX = sideLengthX;
    cacheSideY = sideLengthY;
    if (baseX == null || baseX.length < length) {
      baseX = new int[length];
      baseY = new int[length];
    }
    int quarter = length / 4;
    for (int i = 0; i < length; i++) {
      int x;
      int y;
      if (i < quarter) {
        x = -sideLengthX / 2 + i * sideLengthX / quarter;
        y = -sideLengthY / 2;
      } else if (i < length / 2) {
        x = +sideLengthX / 2;
        y = -sideLengthY / 2 + (i % quarter) * sideLengthY / quarter;
      } else if (i < 3 * length / 4) {
        x = +sideLengthX / 2 - (i % quarter) * sideLengthX / quarter;
        y = +sideLengthY / 2;
      } else {
        x = -sideLengthX / 2;
        y = +sideLengthY / 2 - (i % quarter) * sideLengthY / quarter;
      }
      baseX[i] = x;
      baseY[i] = y;
    }
  }

  @Override
  public void update(float delta) {
    int length = arrayController.getLength();
    sideLength = (int) (Math.min(screenHeight, screenWidth) / 2.4) * 2;
    int sideLengthX = screenWidth;
    int sideLengthY = screenHeight;
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;

    rebuildPerimeter(length, sideLengthX, sideLengthY);

    if (xyd == null || xyd.length < length * 3) {
      xyd = new float[length * 3];
      argb = new int[length];
    }

    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      double barHeight =
          ((screenHeight - 10.)
                  / length
                  * (length
                      - 2
                          * Math.min(
                              Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                              Math.abs(i + length - value))))
              / (screenHeight);

      int o = i * 3;
      xyd[o] = centerX + (int) (baseX[i] * barHeight);
      xyd[o + 1] = centerY + (int) (baseY[i] * barHeight);
      xyd[o + 2] = 6;
      argb[i] = color.getRGB();
    }
    rs.fillCircles(xyd, argb, length);
  }
}
