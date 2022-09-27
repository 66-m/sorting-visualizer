package io.github.sorting_visualizer.Visual;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Sound.Sound;
import io.github.sorting_visualizer.Visual.Gradient.ColorGradient;
import processing.core.PApplet;

import java.awt.*;

public class DisparityGraph extends Visualization {


    public DisparityGraph(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Disparity Graph";
    }

    @Override
    public void update() {
        super.update();

        int rectWidth = (screenWidth - (arrayController.getLength() - 1)) / arrayController.getLength();

        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            int barHeight = (int)(((screenHeight-10.)/ arrayController.getLength() * (arrayController.getLength() - 2*Math.min(Math.min(Math.abs(i - arrayController.get(i)),Math.abs(i-arrayController.getLength() - arrayController.get(i))),Math.abs(i+arrayController.getLength() - arrayController.get(i))))));

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }

            arrayController.setMarker(i, Marker.NORMAL);


            proc.rect(PApplet.map(i, 0, arrayController.getLength(), 0, screenWidth), screenHeight, rectWidth, -1 * barHeight); //DisparityGraph
        }
    }

}
