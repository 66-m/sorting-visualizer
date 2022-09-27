package io.github.sorting_visualizer.Visual;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Sound.Sound;
import io.github.sorting_visualizer.Visual.Gradient.ColorGradient;
import processing.core.PApplet;

import java.awt.*;

import static java.lang.Math.floor;
import static java.lang.Math.min;

public class Cube extends Visualization {

    int radius;
    static float aa = 0;

    public Cube(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "3D - Cube";

    }

    @Override
    public void update() {
        super.update();

        radius = (int) (min(screenHeight, screenWidth) / 3.5);

        if (Math.pow((floor(Math.pow(arrayController.getLength(), 1 / 3f) + 0.1)), 3) != arrayController.getLength()) {
//            code to update for non perfect squares
            int nextN = (int) (floor(Math.pow(arrayController.getLength(), 1 / 3f) + 0.1)); //next lower
            MainController.updateArraySize(nextN * nextN * nextN); // Update arraySize
        }

        aa -= PApplet.PI / (10 * proc.frameRate);

        int xSize = (int) (Math.pow(arrayController.getLength(), 1 / 3.) + 0.1);
        int xCnt = 0;
        int yCnt = 0;
        int zCnt = 0;

        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            if (arrayController.getMarker(arrayController.get(i)) == Marker.SET) {
                sound.playSound(arrayController.get(i));
            }

            arrayController.setMarker(arrayController.get(i), Marker.NORMAL);

            int barHeight = ((100000 / arrayController.getLength() * (arrayController.getLength() - 2 * Math.min(Math.min(Math.abs(i - arrayController.get(i)), Math.abs(i - arrayController.getLength() - arrayController.get(i))), Math.abs(i + arrayController.getLength() - arrayController.get(i))))));

            float xa = PApplet.map(xCnt, 0, xSize, -radius, radius);
            float ya = PApplet.map(yCnt, 0, xSize, -radius, radius);
            float za = PApplet.map(zCnt, 0, xSize, -radius, radius);

            float zb = (float) (Math.sin(aa) * xa + Math.cos(aa) * za);
            float x = (float) (screenWidth * 0.5 + (float) Math.cos(aa) * xa - Math.sin(aa) * za);

            float z = (float) (Math.sin(-10) * ya + Math.cos(-10) * zb);
            float y = (float) (screenHeight * 0.5 - 20 + Math.cos(-10) * ya - Math.sin(-10) * zb);


            //16:9
            //float size = PApplet.map(z, -radius, radius, 25, 40);

            //9:16
            float size = PApplet.map(z, -radius, radius, 15, 25);

            size = PApplet.map(barHeight, 0, 100000, 0, size);


            proc.ellipse(x, y, size, size); //Swirl dots

            zCnt++;
            if (zCnt == xSize) {
                if (xCnt == xSize - 1) {
                    yCnt += 1;
                    zCnt = 0;
                    xCnt = 0;
                } else {
                    xCnt += 1;
                    zCnt = 0;
                }
            }
        }
    }

}
