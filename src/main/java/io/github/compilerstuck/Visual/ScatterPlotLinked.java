package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import processing.core.PApplet;

import java.awt.*;

public class ScatterPlotLinked extends Visualization {


    public ScatterPlotLinked(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Scatter Plot Linked";
    }

    @Override
    public void update() {
        super.update();

        //int rectWidth = (screenWidth - (arrayController.getLength() - 1)) / arrayController.getLength();

        for (int i = 0; i < arrayController.getLength() - 1; i++) {
            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            int barHeight1 = (arrayController.get(i) + 1) * (screenHeight - 5) / arrayController.getLength();
            int barHeight2 = (arrayController.get(i % arrayController.getLength() + 1) + 1) * (screenHeight - 5) / arrayController.getLength();

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }


            arrayController.setMarker(i, Marker.NORMAL);

            proc.line(PApplet.map(i, 0, arrayController.getLength(), 0, screenWidth), screenHeight - barHeight1, PApplet.map((i % arrayController.getLength() + 1), 0, arrayController.getLength(), 0, screenWidth), screenHeight - barHeight2);

        }
    }

}
