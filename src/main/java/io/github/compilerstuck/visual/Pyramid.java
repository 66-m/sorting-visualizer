package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import processing.core.PApplet;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import processing.core.PConstants;

import java.lang.Math;

import java.awt.*;

public class Pyramid extends Visualization {

    float angle = 0;


    public Pyramid(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
        name = "3D - Pyramid";
    }

    @Override
    public void update() {
        super.update();

        //int rectWidth = (screenWidth - (arrayController.getLength() - 1)) / arrayController.getLength();
        int radius = (int) (Math.min(screenHeight, screenWidth) / 1.7);

        angle -= PApplet.PI / (15 * proc.frameRate());
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
