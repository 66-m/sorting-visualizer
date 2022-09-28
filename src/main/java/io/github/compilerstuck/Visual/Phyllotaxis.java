package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import processing.core.PApplet;

import java.awt.*;

import static java.lang.Math.*;
import static processing.core.PApplet.radians;

public class Phyllotaxis extends Visualization {

    int radius;
    int c;

    public Phyllotaxis(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Phyllotaxis";
    }

    @Override
    public void update() {
        super.update();

        radius = (int) (Math.min(screenHeight, screenWidth) / 2.5);
        c = Math.min(screenHeight, screenWidth) / 70;

        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }

            arrayController.setMarker(i, Marker.NORMAL);

            float a = i * radians((float) 180.5);
            float r = (float) (c * sqrt(arrayController.get(i)));
            r = PApplet.map(r, (float) 0, (float) (c * sqrt(arrayController.getLength())), 0, Math.min(screenHeight, (float) screenWidth) / 2 - 20);
            float x = (float) (r * cos(a));
            float y = (float) (r * sin(a));

            proc.rect(screenWidth / 2f + x, screenHeight / 2f + y, 8, 8); //Swirl dots

        }
    }

}
