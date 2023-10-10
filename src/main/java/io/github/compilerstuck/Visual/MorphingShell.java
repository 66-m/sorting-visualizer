package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import processing.core.PApplet;
import processing.core.PConstants;

import static java.lang.Math.floor;
import static java.lang.Math.min;

import java.awt.*;

public class MorphingShell extends Visualization {

    int radius;
    static float aa = 0;
    float angle = 0;


    public MorphingShell(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "3D - Morphing Shell";
    }

    @Override
    public void update() {
        super.update();
        proc.lights();

        radius = Math.min(screenHeight, screenWidth) / 2;

        aa += PApplet.PI / (proc.frameRate * 10);

        int colSize = (int) Math.sqrt(arrayController.getLength());
        int rowCnt = 0;
        int colCnt = 0;
        
        angle += PApplet.PI / (7.5 * proc.frameRate);


        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.noStroke();
            proc.fill(color.getRGB(), (float) (255.));

            if (arrayController.getMarker(rowCnt + colCnt * colSize) == Marker.SET) {
                sound.playSound(rowCnt + colCnt * colSize);
            }

            arrayController.setMarker(rowCnt + colCnt * colSize, Marker.NORMAL);

            double barHeight = arrayController.get(rowCnt + colCnt * colSize);

            float lon = PApplet.map(rowCnt, 0, colSize, -PApplet.PI + aa, PApplet.PI + aa);
            float lat = PApplet.map(colCnt, 0, colSize, -PApplet.PI + aa, PApplet.PI + aa);
            float z = (float) (radius/3 * Math.sin(lon) * Math.cos(lat));
            float x = (float) (radius * Math.sin(lon) * Math.sin(lat));
            float y = (float) (radius * Math.cos(lon));
            y += barHeight;
            int size = (int) PApplet.map(z, 0, screenHeight, 10, 20);

            //x = (float) (screenHeight * 0.5 + x / 2);
            //y = screenHeight / 2. + y / 2;


            proc.pushMatrix();
            proc.translate((float) screenWidth / 4, (float) screenHeight / 2, -(int) (min(screenHeight, screenWidth) / 10));
            
            //proc.rotateX(PConstants.PI/3);
            //proc.rotateX(angle);
            
            proc.translate(y/2, x/2, z);          

            proc.ellipse(0, 0, 15, 15);
            proc.popMatrix();



            //proc.ellipse((float) y, x, size, size); //Swirl dots

            colCnt++;
            if (colCnt == colSize) {
                rowCnt += 1;
                colCnt = 0;
            }

        }
    }

}
