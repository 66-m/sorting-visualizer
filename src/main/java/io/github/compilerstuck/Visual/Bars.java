package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.model.ArrayModel;
import io.github.compilerstuck.Control.render.RenderContext;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

import java.awt.*;
import processing.core.PApplet;

public class Bars extends Visualization {


    public Bars(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
        name = "Bars";
    }

    @Override
    public void update() {
        super.update();

        int rectWidth = (screenWidth - (arrayController.getLength() - 1)) / arrayController.getLength();

        for (int i = 0; i < arrayController.getLength(); i++) {
            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));


            int barHeight = (arrayController.get(i) + 1) * (screenHeight - 5) / arrayController.getLength();

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }


            arrayController.setMarker(i, Marker.NORMAL);

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());


            proc.rect(PApplet.map(i, 0, arrayController.getLength(), 0, screenWidth), screenHeight, rectWidth, -1 * barHeight); //Classic bar

        }
    }

}
