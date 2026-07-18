package io.github.compilerstuck.visual;

import static java.lang.Math.floor;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;

public class MosaicSquares extends Visualization {

  public MosaicSquares(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Mosaic Squares";
  }

  @Override
  public void update() {
    super.update();

    int nextN = (int) (floor(Math.pow(arrayController.getLength(), 1 / 2.) + 0.1));
    float squareRoot = nextN;
    int drawCount = Math.min(arrayController.getLength(), nextN * nextN);

    float tileDimX = screenWidth / squareRoot;
    float tileDimY = screenHeight / squareRoot;

    for (int i = 0; i < drawCount; i++) {
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }

      arrayController.setMarker(i, Marker.NORMAL);

      proc.stroke(color.getRGB());
      proc.fill(color.getRGB());

      proc.rect((i % squareRoot) * tileDimX, (int) (i / squareRoot) * tileDimY, tileDimX, tileDimY);
    }
  }
}
