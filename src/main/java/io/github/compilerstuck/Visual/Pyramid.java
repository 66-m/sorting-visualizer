package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import processing.core.PApplet;
import processing.core.PConstants;

import java.lang.Math;

import java.awt.*;

public class Pyramid extends Visualization {

    float angle = 0;


    public Pyramid(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "3D - Pyramid";
    }

    @Override
    public void update() {
        super.update();

        //int rectWidth = (screenWidth - (arrayController.getLength() - 1)) / arrayController.getLength();
        int radius = (int) (Math.min(screenHeight, screenWidth) / 1.7);

        angle -= PApplet.PI / (7.5 * proc.frameRate);
        proc.lights();


        for (int i = 0; i < arrayController.getLength(); i++) {
            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));


            int barHeight = (arrayController.get(i) + 1) * (radius - 5) / arrayController.getLength();

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }

            arrayController.setMarker(i, Marker.NORMAL);

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());


            //proc.rect(PApplet.map(i, 0, arrayController.getLength(), 0, screenWidth), screenHeight, rectWidth, -1 * barHeight); //Classic bar

            proc.pushMatrix();

            proc.translate((float) screenWidth / 2, (float) (screenHeight / 2.5), -(int) (Math.min(screenHeight, screenWidth) / 10));

            proc.rotateX(PConstants.PI/3);
            proc.rotateZ(angle);

            proc.translate(0, 0, radius/2 - PApplet.map(i, 0, arrayController.getLength(), 0, radius));          

            proc.rect(-barHeight/2, -barHeight/2, barHeight, barHeight);

            proc.popMatrix();

        }
    }

}
