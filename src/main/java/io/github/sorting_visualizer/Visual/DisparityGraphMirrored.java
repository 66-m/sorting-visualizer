package io.github.sorting_visualizer.Visual;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Sound.Sound;
import io.github.sorting_visualizer.Visual.Gradient.ColorGradient;
import processing.core.PApplet;

import java.awt.*;

public class DisparityGraphMirrored extends Visualization {

    public DisparityGraphMirrored(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
         name = "Disparity Graph Mirrored";
    }

    @Override
    public void update() {
        super.update();

        int rectWidth = (screenWidth - (arrayController.getLength() - 1)) / arrayController.getLength();

        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            int barHeight = (int)((screenHeight - 5.)/ arrayController.getLength() * (arrayController.getLength() - Math.abs(i - arrayController.get(i))));

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }

            arrayController.setMarker(i, Marker.NORMAL);

            proc.rect(PApplet.map(i, 0, arrayController.getLength(), 0, screenWidth), proc.height / 2f + barHeight / 2f, rectWidth, -1 * barHeight); //MirroredDisparityGraph
        }
    }

}
