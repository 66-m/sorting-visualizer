package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.model.ArrayModel;
import io.github.compilerstuck.Control.render.RenderContext;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

import java.awt.*;

public class Circle extends Visualization {

    int radius;

    public Circle(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
        name = "Circle";
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

            proc.line(screenWidth / 2, screenHeight / 2, x, y);

        }
    }

}
