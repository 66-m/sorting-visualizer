package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

import java.awt.*;

public class SwirlDots extends Visualization {

    int radius;

    public SwirlDots(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
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

            proc.circle(x, y, 5); //Swirl dots

        }
    }

}
