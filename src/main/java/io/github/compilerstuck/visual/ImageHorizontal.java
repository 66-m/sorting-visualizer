package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.Brand;
import io.github.compilerstuck.control.config.MainControllerConfig;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.LoadedImage;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

public class ImageHorizontal extends Visualization {
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

    int imgPartWidth = screenHeight / arrayController.getLength();

    for (int i = 0; i < arrayController.getLength(); i += 1) {
      int pos = arrayController.get(i) * imgPartWidth;

      for (int y = pos; y < pos + imgPartWidth; y++) {

        for (int x = 0; x < screenWidth; x++) {
          int realLoc = x + (y - pos + i * imgPartWidth) * img.pixelWidth();
          int loc = x + y * img.pixelWidth();

          float r = proc.red(img.pixels()[loc]);
          float g = proc.green(img.pixels()[loc]);
          float b = proc.blue(img.pixels()[loc]);

          // If Marker.SET is set, set the pixel to white
          if (arrayController.getMarker(i) == Marker.SET) {
            r = 255;
            g = 255;
            b = 255;
          }

          proc.pixels()[realLoc] = proc.color(r, g, b);
        }
      }
      if (arrayController.getMarker(i) == Marker.SET) {
        sound.playSound(i);
      }
      if (arrayController.getMarker(i) == Marker.SET) {
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

      // Resize the image to match the window size
      img.resize(proc.getWidth(), proc.getHeight());

    } catch (Exception e) {
      imageFound = false;
    }

    return imageFound;
  }
}
