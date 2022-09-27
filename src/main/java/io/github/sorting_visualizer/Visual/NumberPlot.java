package io.github.sorting_visualizer.Visual;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Sound.Sound;
import io.github.sorting_visualizer.Visual.Gradient.ColorGradient;
import processing.core.PApplet;

import java.awt.*;

public class NumberPlot extends Visualization {

    public NumberPlot(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Number Plot";
    }

    @Override
    public void update() {
        super.update();

        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            int barHeight = (arrayController.get(i) + 1) * (screenHeight - 5) / arrayController.getLength();

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }


            arrayController.setMarker(i, Marker.NORMAL);

            proc.text(arrayController.get(i) + 1, PApplet.map(i, 0, arrayController.getLength(), 0, screenWidth), screenHeight - barHeight); //Classic

        }
    }

}
