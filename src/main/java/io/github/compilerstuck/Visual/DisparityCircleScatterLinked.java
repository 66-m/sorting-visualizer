package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

import java.awt.*;

public class DisparityCircleScatterLinked extends Visualization {

    int radius;

    public DisparityCircleScatterLinked(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Disparity Circle Scatter Linked";
    }

    @Override
    public void update() {
        super.update();

        radius = (int) (Math.min(screenHeight, screenWidth) / 2.4);


        for (int i = 0; i < arrayController.getLength() - 1; i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }

            arrayController.setMarker(i, Marker.NORMAL);

            double barHeight1 = ((screenHeight - 10.) / arrayController.getLength() * (arrayController.getLength() - 2 * Math.min(Math.min(Math.abs(i - arrayController.get(i)), Math.abs(i - arrayController.getLength() - arrayController.get(i))), Math.abs(i + arrayController.getLength() - arrayController.get(i))))) / (screenHeight);
            double barHeight2 = ((screenHeight - 10.) / arrayController.getLength() * (arrayController.getLength() - 2 * Math.min(Math.min(Math.abs((i % arrayController.getLength() + 1) - arrayController.get((i % arrayController.getLength() + 1))), Math.abs((i % arrayController.getLength() + 1) - arrayController.getLength() - arrayController.get((i % arrayController.getLength() + 1)))), Math.abs((i % arrayController.getLength() + 1) + arrayController.getLength() - arrayController.get((i % arrayController.getLength() + 1)))))) / (screenHeight);

            double phase1 = 2 * Math.PI * i / arrayController.getLength();
            int x1 = (screenWidth / 2) + (int) (radius * barHeight1 * Math.sin(phase1));
            int y1 = (screenHeight / 2) - (int) (radius * barHeight1 * Math.cos(phase1));

            double phase2 = 2 * Math.PI * (i % arrayController.getLength() + 1) / arrayController.getLength();
            int x2 = (screenWidth / 2) + (int) (radius * barHeight2 * Math.sin(phase2));
            int y2 = (screenHeight / 2) - (int) (radius * barHeight2 * Math.cos(phase2));

            proc.line(x1, y1, x2, y2);

        }
    }

}
