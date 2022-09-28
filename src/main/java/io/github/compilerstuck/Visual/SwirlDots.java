package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

import java.awt.*;

public class SwirlDots extends Visualization {

    int radius;

    public SwirlDots(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Swirl Dots";
    }

    @Override
    public void update() {
        super.update();

        radius = (int) (Math.min(screenHeight, screenWidth) / 2.5);

        for (int i = 0; i < arrayController.getLength(); i++) {
            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }

            arrayController.setMarker(i, Marker.NORMAL);

            double phase = 16 * Math.PI * i / arrayController.getLength();
            int x = screenWidth / 2 + (int) (radius * arrayController.get(i) / arrayController.getLength() * Math.sin(phase));
            int y = screenHeight / 2 + (int) (radius * arrayController.get(i) / arrayController.getLength() * Math.cos(phase));

            proc.ellipse(x, y, 7, 7); //Swirl dots

        }
    }

}
