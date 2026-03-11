package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayModel;
import io.github.compilerstuck.Control.RenderContext;
import processing.core.PApplet;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import processing.core.PImage;

public class ImageVertical extends Visualization {

    PImage img;

    public ImageVertical(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
        name = "Image - Vertical Sorting";
        setImg("dummy-image.jpg");
    }

    @Override
    public void update() {
        this.screenHeight = proc.getHeight();
        this.screenWidth = proc.getWidth();

        if(arrayController.getLength()>img.pixelWidth){
            MainController.updateArraySize(img.pixelWidth);
        }
        else if (img.pixelWidth % arrayController.getLength() > 0) {
            int newWindowWidth = img.pixelWidth;
            int max = img.pixelWidth;

            // Find the next highest value that divides the image width without a remainder
            for (int i = arrayController.getLength(); i <= max; i++) {
                if (img.pixelWidth % i == 0) {
                    newWindowWidth = i;
                    break;
                }
            }

            // Update the array size and resize the window
            MainController.updateArraySize(newWindowWidth);
        }

        proc.background(15);

        ((PApplet)proc).loadPixels();
        img.loadPixels();

        int imgPartHeight = screenWidth / arrayController.getLength();

        for (int i = 0; i < arrayController.getLength(); i += 1) {
            int pos = arrayController.get(i) * imgPartHeight;

            for (int x = pos; x < pos + imgPartHeight; x++) {

                for (int y = 0; y < screenHeight; y++) {
                    int realLoc = (x - pos + i * imgPartHeight) + y * img.pixelWidth;
                    int loc = x + y * img.pixelWidth;

                    float r = ((PApplet)proc).red(img.pixels[loc]);
                    float g = ((PApplet)proc).green(img.pixels[loc]);
                    float b = ((PApplet)proc).blue(img.pixels[loc]);

                    // If Marker.SET is set, set the pixel to white
                    if (arrayController.getMarker(i) == Marker.SET) {
                        r = 255;
                        g = 255;
                        b = 255;
                    }

                    ((PApplet)proc).pixels[realLoc] = ((PApplet)proc).color(r, g, b);
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

        ((PApplet)proc).updatePixels();

        proc.fill(255);
        proc.textSize((int) (25. / 1280 * screenWidth));
        proc.text("CompilerStuck", screenWidth - (int) (175. / 1280 * screenWidth), (int) (21. / 1280 * screenWidth)); //Branding
        proc.textSize(20);
    }


    public boolean setImg(String imagePath) {
        boolean imageFound = true;
        ((PApplet)proc).getSurface().setResizable(false); // Enable window resizing

        try {
            img = ((PApplet)proc).loadImage(imagePath);

            // Resize the image to match the window size
            img.resize(proc.getWidth(), proc.getHeight());

        } catch (Exception e) {
            imageFound = false;
        }

        return imageFound;
    }

}
