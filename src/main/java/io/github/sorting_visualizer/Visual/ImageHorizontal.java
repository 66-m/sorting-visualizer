package io.github.sorting_visualizer.Visual;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Sound.Sound;
import io.github.sorting_visualizer.Visual.Gradient.ColorGradient;
import processing.core.PImage;

public class ImageHorizontal extends Visualization {

    PImage img;

    public ImageHorizontal(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Image - Horizontal Sorting";
        img = proc.loadImage("pathToImage");
    }

    @Override
    public void update() {
        this.screenHeight = proc.height;
        this.screenWidth = proc.width;

        proc.background(15);


        proc.loadPixels();
        img.loadPixels();

        int imgPartWidth = screenHeight / arrayController.getLength();

        for (int i = 0; i < arrayController.getLength(); i += 1) {
            int pos = arrayController.get(i) * imgPartWidth;

            for (int y = pos; y < pos + imgPartWidth; y++) {

                for (int x = 0; x < img.pixelWidth; x++) {
                    int realLoc = (x) + (y - pos + i * imgPartWidth) * img.pixelWidth;
                    int loc = x + y * img.pixelWidth;

                    float r = proc.red(img.pixels[loc]);
                    float g = proc.green(img.pixels[loc]);
                    float b = proc.blue(img.pixels[loc]);

                    if (arrayController.getMarker(i) == Marker.SET && r != 0 || g != 0 && b != 0) {
                        r = 255;
                        g = 255;
                        b = 255;
                    }
                    proc.pixels[realLoc] = proc.color(r, g, b);
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
        proc.textSize((int) (25. / 1280 * screenWidth));
        proc.text("CompilerStuck", screenWidth - (int) (175. / 1280 * screenWidth), (int) (21. / 1280 * screenWidth)); //Branding
        proc.textSize(20);
    }

}
