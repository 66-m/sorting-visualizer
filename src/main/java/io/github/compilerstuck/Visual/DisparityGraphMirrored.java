package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayModel;
import io.github.compilerstuck.Control.RenderContext;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

import java.awt.*;
import processing.core.PApplet;

public class DisparityGraphMirrored extends Visualization {

    public DisparityGraphMirrored(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
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

            int barHeight = (int) ((screenHeight - 5.) / arrayController.getLength() * (arrayController.getLength() - Math.abs(i - arrayController.get(i))));

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }

            arrayController.setMarker(i, Marker.NORMAL);

            proc.rect(PApplet.map(i, 0, arrayController.getLength(), 0, screenWidth), proc.getHeight() / 2f + barHeight / 2f, rectWidth, -1 * barHeight); //MirroredDisparityGraph
        }
    }

}
