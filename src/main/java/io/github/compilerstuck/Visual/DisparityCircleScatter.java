package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

import java.awt.*;

public class DisparityCircleScatter extends Visualization {

    int radius;

    public DisparityCircleScatter(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Disparity Circle Scatter";
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

            //double barHeight = ((screenHeight-10.)/ arrayController.getLength() * (arrayController.getLength()/2. - Math.abs(arrayController.getLength()/2. - arrayController.get(i))))/(screenHeight/2.);
            double barHeight = ((screenHeight - 10.) / arrayController.getLength() * (arrayController.getLength() - 2 * Math.min(Math.min(Math.abs(i - arrayController.get(i)), Math.abs(i - arrayController.getLength() - arrayController.get(i))), Math.abs(i + arrayController.getLength() - arrayController.get(i))))) / (screenHeight);


            double phase = 2 * Math.PI * i / arrayController.getLength();
            int x = (screenWidth / 2) + (int) (radius * barHeight * Math.sin(phase));
            int y = (screenHeight / 2) - (int) (radius * barHeight * Math.cos(phase));

            proc.ellipse(x, y, 10, 10); //Swirl dots

        }
    }

}
