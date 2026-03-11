package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayModel;
import io.github.compilerstuck.Control.RenderContext;
import processing.core.PApplet;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import processing.core.PConstants;

import static java.lang.Math.floor;

import java.lang.Math;

import java.awt.*;

public class DisparityPlane extends Visualization {

    float angle = 0;
    float squareRoot;


    public DisparityPlane(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
        name = "3D - Disparity Plane";
    }

    @Override
    public void update() {
        super.update();

        //int rectWidth = (screenWidth - (arrayController.getLength() - 1)) / arrayController.getLength();
        int radius = (int) (Math.min(screenHeight, screenWidth) / 1.2);

        angle += PApplet.PI / (15 * ((PApplet)proc).frameRate);
        ((PApplet)proc).lights();

        if (Math.pow(floor(Math.pow(arrayController.getLength(), 1 / 2.)), 2.) != arrayController.getLength()) {

            //            code to update for non perfect squares
                        int nextN = (int) (floor(Math.pow(arrayController.getLength(), 1 / 2.) + 0.1)); //next lower
            
                        MainController.updateArraySize(nextN * nextN); // Update arraySize
                    }     

        squareRoot = (int) Math.pow(arrayController.getLength(), 1 / 2.);

        for (int i = 0; i < arrayController.getLength(); i++) {
            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));


            int barHeight = screenHeight/4 - (int) (((screenHeight/4 - 10.) / arrayController.getLength() * (arrayController.getLength() - 2 * Math.min(Math.min(Math.abs(i - arrayController.get(i)), Math.abs(i - arrayController.getLength() - arrayController.get(i))), Math.abs(i + arrayController.getLength() - arrayController.get(i))))));
            
            float tileDim = radius / squareRoot;

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }

            arrayController.setMarker(i, Marker.NORMAL);

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());


            //Classic bar: proc.rect(PApplet.map(i, 0, arrayController.getLength(), 0, screenWidth), screenHeight, rectWidth, -1 * barHeight);

            ((PApplet)proc).pushMatrix();

            ((PApplet)proc).translate((float) screenWidth / 2, (float) (screenHeight / 2.5), -(int) (Math.min(screenHeight, screenWidth) / 10));

            ((PApplet)proc).rotateX(PConstants.PI/3);
            ((PApplet)proc).rotateZ(angle);

            // Pyramid: ((PApplet)proc).translate(0, 0, radius/2 - PApplet.map(i, 0, arrayController.getLength(), 0, radius));          

            ((PApplet)proc).translate(-radius/2 + (int)floor(i/squareRoot) * tileDim, -radius/2 +  i%squareRoot * tileDim, barHeight);  

            
            proc.rect(-tileDim/2, -tileDim/2, tileDim, tileDim);

            ((PApplet)proc).popMatrix();

        }
    }

}
