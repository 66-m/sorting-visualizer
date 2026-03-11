package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayModel;
import io.github.compilerstuck.Control.RenderContext;
import processing.core.PApplet;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

import static java.lang.Math.min;

import java.awt.*;

public class MorphingShell extends Visualization {

    int radius;
    static float aa = 0;
    float angle = 0;


    public MorphingShell(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
        name = "3D - Morphing Shell";
    }

    @Override
    public void update() {
        super.update();
        ((PApplet)proc).lights();

        radius = Math.min(screenHeight, screenWidth) / 2;

        aa += PApplet.PI / (((PApplet)proc).frameRate * 10);

        int colSize = (int) Math.sqrt(arrayController.getLength());
        int rowCnt = 0;
        int colCnt = 0;
        
        angle += PApplet.PI / (7.5 * ((PApplet)proc).frameRate);


        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            ((PApplet)proc).noStroke();
            ((PApplet)proc).fill(color.getRGB(), (float) (255.));

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

            //x = (float) (screenHeight * 0.5 + x / 2);
            //y = screenHeight / 2. + y / 2;


            ((PApplet)proc).pushMatrix();
            ((PApplet)proc).translate((float) screenWidth / 4, (float) screenHeight / 2, -(int) (min(screenHeight, screenWidth) / 10));
            
            //((PApplet)proc).rotateX(PConstants.PI/3);
            //((PApplet)proc).rotateX(angle);
            
            ((PApplet)proc).translate(y/2, x/2, z);          

            proc.ellipse(0, 0, 15, 15);
            ((PApplet)proc).popMatrix();



            //proc.ellipse((float) y, x, size, size); //Swirl dots

            colCnt++;
            if (colCnt == colSize) {
                rowCnt += 1;
                colCnt = 0;
            }

        }
    }

}
