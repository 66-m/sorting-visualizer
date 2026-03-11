package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayModel;
import io.github.compilerstuck.Control.RenderContext;
import processing.core.PApplet;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import processing.core.PImage;

public class ImageHorizontal extends Visualization {
    private PImage img;

    public ImageHorizontal(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
        name = "Image - Horizontal Sorting";
        setImg("dummy-image.jpg");
    }

    @Override
    public void update() {
        this.screenHeight = proc.getHeight();
        this.screenWidth = proc.getWidth();

        if(arrayController.getLength()>img.pixelHeight){
            MainController.updateArraySize(img.pixelHeight);
        }
        else if (img.pixelHeight % arrayController.getLength() > 0) {
            int newWindowHeight = img.pixelHeight;
            int max = img.pixelHeight;

            // Find the next highest value that divides the image hight without a remainder
            for (int i = arrayController.getLength(); i <= max; i++) {
                if (img.pixelHeight % i == 0) {
                    newWindowHeight = i;
                    break;
                }
            }

            // Update the array size and resize the window
            MainController.updateArraySize(newWindowHeight);
        }

        proc.background(15);

        ((PApplet)proc).loadPixels();
        img.loadPixels();

        int imgPartWidth = screenHeight / arrayController.getLength();

        for (int i = 0; i < arrayController.getLength(); i += 1) {
            int pos = arrayController.get(i) * imgPartWidth;

            for (int y = pos; y < pos + imgPartWidth; y++) {

                for (int x = 0; x < screenWidth; x++) {
                    int realLoc = x + (y - pos + i * imgPartWidth) * img.pixelWidth;
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
