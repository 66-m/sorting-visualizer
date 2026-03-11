package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayModel;
import io.github.compilerstuck.Control.RenderContext;
import processing.core.PApplet;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import processing.core.PConstants;

import java.lang.Math;

import java.awt.*;

public class SphereHoops extends Visualization {

    float angle = 0;


    public SphereHoops(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
        name = "3D - Sphere Hoops";
    }

    @Override
    public void update() {
        super.update();

        //int rectWidth = (screenWidth - (arrayController.getLength() - 1)) / arrayController.getLength();
        int radius = (int) (Math.min(screenHeight, screenWidth) / 1.5);

        angle -= PApplet.PI / (15 * ((PApplet)proc).frameRate);
        ((PApplet)proc).lights();


        for (int i = 0; i < arrayController.getLength(); i++) {
            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            float wi = (float) Math.sqrt(1-Math.pow((((float) i / arrayController.getLength()) * 2-1), 2));
            
            int sphere_wi = (int) PApplet.map(wi, 0, 1, 0, radius);

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }

            arrayController.setMarker(i, Marker.NORMAL);

            proc.stroke(color.getRGB());
            //proc.fill(color.getRGB());
            ((PApplet)proc).noFill();


            //proc.rect(PApplet.map(i, 0, arrayController.getLength(), 0, screenWidth), screenHeight, rectWidth, -1 * barHeight); //Classic bar

            ((PApplet)proc).pushMatrix();

            ((PApplet)proc).translate((float) screenWidth / 2, (float) (screenHeight / 2), -(int) (Math.min(screenHeight, screenWidth) / 10));

            ((PApplet)proc).rotateX(PConstants.PI/3);
            //((PApplet)proc).rotateX(angle);

            ((PApplet)proc).translate(0, 0, radius/2 - PApplet.map(i, 0, arrayController.getLength(), 0, radius));          

            ((PApplet)proc).circle(0, 0, sphere_wi);

            ((PApplet)proc).popMatrix();

        }
    }

}
