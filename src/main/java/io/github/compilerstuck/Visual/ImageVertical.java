package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.model.ArrayModel;
import io.github.compilerstuck.Control.render.LoadedImage;
import io.github.compilerstuck.Control.render.RenderContext;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

public class ImageVertical extends Visualization {

    LoadedImage img;

    public ImageVertical(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
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

        int imgPartHeight = screenWidth / arrayController.getLength();

        for (int i = 0; i < arrayController.getLength(); i += 1) {
            int pos = arrayController.get(i) * imgPartHeight;

            for (int x = pos; x < pos + imgPartHeight; x++) {

                for (int y = 0; y < screenHeight; y++) {
                    int realLoc = (x - pos + i * imgPartHeight) + y * img.pixelWidth();
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
        proc.textSize((int) (25. / 1280 * screenWidth));
        proc.text("CompilerStuck", screenWidth - (int) (175. / 1280 * screenWidth), (int) (21. / 1280 * screenWidth)); //Branding
        proc.textSize(20);
    }


    public boolean setImg(String imagePath) {
        boolean imageFound = true;
        proc.setResizable(false); // Enable window resizing

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
