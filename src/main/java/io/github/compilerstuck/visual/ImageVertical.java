package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.Brand;
import io.github.compilerstuck.control.config.MainControllerConfig;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.LoadedImage;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

public class ImageVertical extends Visualization {

  private static final int WHITE = 0xFFFFFFFF;

  LoadedImage img;

  public ImageVertical(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "Image - Vertical Sorting";
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
    int imgPartHeight = screenWidth / length;
    int[] src = img.pixels();
    int[] dst = proc.pixels();
    int pixelWidth = img.pixelWidth();

    for (int i = 0; i < length; i++) {
      int pos = arrayController.get(i) * imgPartHeight;
      boolean highlight = arrayController.getMarker(i) == Marker.SET;

      for (int x = pos; x < pos + imgPartHeight; x++) {
        int dstX = x - pos + i * imgPartHeight;
        if (highlight) {
          for (int y = 0; y < screenHeight; y++) {
            dst[dstX + y * pixelWidth] = WHITE;
          }
        } else {
          for (int y = 0; y < screenHeight; y++) {
            dst[dstX + y * pixelWidth] = src[x + y * pixelWidth];
          }
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
