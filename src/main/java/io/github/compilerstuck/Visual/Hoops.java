package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import processing.core.PApplet;

import java.awt.*;

public class Hoops extends Visualization {

    int radius;

    public Hoops(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Hoops";
    }

    @Override
    public void update() {
        super.update();
        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());

            proc.noFill();

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }


            arrayController.setMarker(i, Marker.NORMAL);

            radius = (int) PApplet.map(i, 0, arrayController.getLength(), 0, (float) (Math.min(screenHeight, screenWidth)/1.1));

            proc.ellipse(screenWidth / 2, screenHeight / 2, radius, radius);

        }
    }

}
