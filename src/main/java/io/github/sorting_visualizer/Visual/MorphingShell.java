package io.github.sorting_visualizer.Visual;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Sound.Sound;
import io.github.sorting_visualizer.Visual.Gradient.ColorGradient;
import processing.core.PApplet;

import java.awt.*;

public class MorphingShell extends Visualization {

    int radius;
    static float aa = 0;

    public MorphingShell(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "3D - Morphing Shell";
    }

    @Override
    public void update() {
        super.update();

        radius = Math.min(screenHeight,screenWidth)/2;

        aa += PApplet.PI/(proc.frameRate*10);

        int colSize = (int) Math.sqrt(arrayController.getLength());
        int rowCnt = 0;
        int colCnt = 0;

        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            if (arrayController.getMarker(rowCnt+colCnt*colSize) == Marker.SET) {
                sound.playSound(rowCnt+colCnt*colSize);
            }

            arrayController.setMarker(rowCnt+colCnt*colSize, Marker.NORMAL);

            double barHeight = arrayController.get(rowCnt+colCnt*colSize);

            float lon = PApplet.map(rowCnt, 0, colSize, -PApplet.PI+ aa, PApplet.PI + aa);
            float lat = PApplet.map(colCnt, 0, colSize, -PApplet.PI + aa, PApplet.PI + aa);
            float z = (float) (radius * Math.sin(lon) * Math.cos(lat));
            float x = (float) (radius * Math.sin(lon) * Math.sin(lat));
            double y = (float) (radius * Math.cos(lon));
            y += barHeight;
            int size = (int) PApplet.map(z, 0, screenHeight, 10, 20);

            x = (float) (screenHeight*0.5+ x / 2);
            y = screenHeight/2. + y / 2;



            proc.ellipse((float)y,x,size,size); //Swirl dots

            colCnt++;
            if (colCnt == colSize) {
                rowCnt += 1;
                colCnt = 0;
            }

        }
    }

}
