package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.model.ArrayModel;
import io.github.compilerstuck.Control.render.RenderContext;
import processing.core.PApplet;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

import java.awt.*;

import static java.lang.Math.*;
import static processing.core.PApplet.radians;

public class Phyllotaxis extends Visualization {

    int radius;
    int c;

    public Phyllotaxis(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
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

            ((PApplet)proc).circle(screenWidth / 2f + x, screenHeight / 2f + y, 5); //Swirl dots

        }
    }

}
