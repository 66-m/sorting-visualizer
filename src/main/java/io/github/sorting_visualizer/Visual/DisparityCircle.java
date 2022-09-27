package io.github.sorting_visualizer.Visual;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Sound.Sound;
import io.github.sorting_visualizer.Visual.Gradient.ColorGradient;

import java.awt.*;

public class DisparityCircle extends Visualization {

    int radius;

    public DisparityCircle(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Disparity Circle";
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

            double barHeight = ((screenHeight-0.1)/ arrayController.getLength() * (arrayController.getLength() - 2*Math.min(Math.min(Math.abs(i - arrayController.get(i)),Math.abs(i-arrayController.getLength() - arrayController.get(i))),Math.abs(i+arrayController.getLength() - arrayController.get(i)))))/(screenHeight);

            double phase = 2*Math.PI * i/arrayController.getLength();
            int x = (screenWidth/2)+(int)(radius*barHeight* Math.sin(phase));
            int y = (screenHeight/2)-(int)(radius*barHeight* Math.cos(phase));

            proc.line((int)(screenWidth/2), (int)(screenHeight/2), x, y); //Swirl dots

        }
    }

}
