package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

import java.awt.*;

public class DisparityChords extends Visualization {

    int radius;

    public DisparityChords(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Disparity Chords";
    }

    @Override
    public void update() {
        super.update();

        radius = (int) (Math.min(screenHeight, screenWidth) / 2.4);

        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }


            arrayController.setMarker(i, Marker.NORMAL);


            double phase = 2 * Math.PI * i / arrayController.getLength();
            int x = (screenWidth / 2) + (int) (radius * Math.sin(phase));
            int y = (screenHeight / 2) - (int) (radius * Math.cos(phase));

            //original position phase
            double phase2 = 2 * Math.PI * arrayController.get(i) / arrayController.getLength();
            int x2 = (screenWidth / 2) + (int) (radius * Math.sin(phase2));
            int y2 = (screenHeight / 2) - (int) (radius * Math.cos(phase2));

            //line from phase to org pos - make a small dot if its at same
            if (x == x2 && y == y2) {
                proc.ellipse(x, y, 1, 1);
            } else {
                proc.line(x, y, x2, y2);
            }

            

        }
    }

}
