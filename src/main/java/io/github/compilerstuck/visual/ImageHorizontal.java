package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.Brand;
import io.github.compilerstuck.control.config.MainControllerConfig;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.LoadedImage;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

public class ImageHorizontal extends Visualization {
  private static final int WHITE = 0xFFFFFFFF;

  private LoadedImage img;

  public ImageHorizontal(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Image - Horizontal Sorting";
    setImg("dummy-image.jpg");
  }

  @Override
  public void update() {
    this.screenHeight = proc.getHeight();
    this.screenWidth = proc.getWidth();

    proc.background(15);

    proc.loadPixels();
    img.loadPixels();

    int length = arrayController.getLength();
    int imgPartWidth = screenHeight / length;
    int[] src = img.pixels();
    int[] dst = proc.pixels();
    int pixelWidth = img.pixelWidth();

    for (int i = 0; i < length; i++) {
      int pos = arrayController.get(i) * imgPartWidth;
      boolean highlight = arrayController.getMarker(i) == Marker.SET;

      for (int y = pos; y < pos + imgPartWidth; y++) {
        int srcRow = y * pixelWidth;
        int dstRow = (y - pos + i * imgPartWidth) * pixelWidth;
        if (highlight) {
          for (int x = 0; x < screenWidth; x++) {
            dst[dstRow + x] = WHITE;
          }
        } else {
          System.arraycopy(src, srcRow, dst, dstRow, screenWidth);
        }
      }
      if (highlight) {
        sound.playSound(i);
      }
      arrayController.setMarker(i, Marker.NORMAL);
    }

    proc.updatePixels();

    proc.fill(255);
    proc.textSize(MainControllerConfig.scaleToWidth(25, screenWidth));
    proc.text(
        Brand.WATERMARK,
        screenWidth - (int) (175. / 1280 * screenWidth),
        (int) (21. / 1280 * screenWidth));
    proc.textSize(20);
  }

  public boolean setImg(String imagePath) {
    boolean imageFound = true;

    try {
      img = proc.loadImage(imagePath);
      img.resize(proc.getWidth(), proc.getHeight());
    } catch (Exception e) {
      imageFound = false;
    }

    return imageFound;
  }
}
