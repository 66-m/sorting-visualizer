package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class DisparitySquareScatter extends Visualization {

  int sideLength;

  private int[] baseX;
  private int[] baseY;
  private final ColorBatch colorBatch = new ColorBatch();
  private int cacheLength = -1;
  private int cacheSideX = -1;
  private int cacheSideY = -1;

  public DisparitySquareScatter(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Disparity Square Scatter";
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
  public void update() {
    super.update();

    int length = arrayController.getLength();
    sideLength = (int) (Math.min(screenHeight, screenWidth) / 2.4) * 2;
    int sideLengthX = screenWidth;
    int sideLengthY = screenHeight;
    int centerX = screenWidth / 2;
    int centerY = screenHeight / 2;

    rebuildPerimeter(length, sideLengthX, sideLengthY);

    proc.noStroke();
    colorBatch.reset();

    for (int i = 0; i < length; i++) {
      int value = arrayController.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayController.getMarker(i));
      int rgb = color.getRGB();

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      double barHeight =
          ((screenHeight - 10.)
                  / length
                  * (length
                      - 2
                          * Math.min(
                              Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                              Math.abs(i + length - value))))
              / (screenHeight);

      int x = (int) (baseX[i] * barHeight);
      int y = (int) (baseY[i] * barHeight);

      colorBatch.fill(proc, rgb);
      proc.circle(centerX + x, centerY + y, 6);
    }
  }
}
