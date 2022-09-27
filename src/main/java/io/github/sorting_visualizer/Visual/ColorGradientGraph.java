package io.github.sorting_visualizer.Visual;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Sound.Sound;
import io.github.sorting_visualizer.Visual.Gradient.ColorGradient;
import processing.core.PApplet;

import java.awt.*;

public class ColorGradientGraph extends Visualization {


    public ColorGradientGraph(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Color Gradient Graph";
    }

    @Override
    public void update() {
        super.update();

        int rectWidth = (screenWidth - (arrayController.getLength() - 1)) / arrayController.getLength();

        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }


            arrayController.setMarker(i, Marker.NORMAL);


            proc.rect(PApplet.map(i, 0, arrayController.getLength(), 0, screenWidth), screenHeight, rectWidth, -1 * screenHeight); //Classic

        }
    }

}
