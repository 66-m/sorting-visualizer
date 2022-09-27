package io.github.sorting_visualizer.Visual;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Sound.Sound;
import io.github.sorting_visualizer.Visual.Gradient.ColorGradient;

import java.awt.*;

public class Circle extends Visualization {

    int radius;

    public Circle(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Circle";
    }

    @Override
    public void update() {
        super.update();

        radius = (int)(Math.min(screenHeight,screenWidth)/2.4);

        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }


            arrayController.setMarker(i, Marker.NORMAL);


            double phase = 2*Math.PI * i/arrayController.getLength();
            int x = (screenWidth/2)+(int)(radius* Math.sin(phase));
            int y = (screenHeight/2)-(int)(radius* Math.cos(phase));

            proc.line((int)(screenWidth/2),(int)(screenHeight/2), x, y);

        }
    }

}
