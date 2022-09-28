package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import processing.core.PApplet;
import processing.core.PImage;

public class ImageVertical extends Visualization {
    private PImage img;

    public ImageVertical(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Image - Vertical Sorting";
    }

    @Override
    public void update() {
        super.update();

        proc.background(15);

        proc.loadPixels();
        img.loadPixels();

        int imgPartWidth = screenWidth / arrayController.getLength();

        for (int i = 0; i < arrayController.getLength(); i += 1) {
            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);

                arrayController.setMarker(i, Marker.NORMAL);
            }
            int pos = (int) PApplet.map(i, 0, arrayController.getLength(), 0, screenWidth);
            for (int x = pos; x < pos + imgPartWidth; x++) {

                for (int y = 0; y < img.pixelHeight; y++) {
                    int realLoc = (x - pos + i * imgPartWidth) + y * img.pixelWidth;
                    int loc = x + y * img.pixelWidth;
                    float r = proc.red(img.pixels[loc]);
                    float g = proc.green(img.pixels[loc]);
                    float b = proc.blue(img.pixels[loc]);
                    proc.pixels[realLoc] = proc.color(r, g, b);
                }

            }

        }
        proc.updatePixels();

        proc.fill(255);
        proc.textSize((int) (25. / 1280 * screenWidth));
        proc.text("CompilerStuck", screenWidth - (int) (175. / 1280 * screenWidth), (int) (21. / 1280 * screenWidth)); //Branding
        proc.textSize(20);
    }

    public void setImg(PImage img) {
        this.img = img;
    }


}
