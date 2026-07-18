package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import processing.core.PApplet;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

import java.awt.*;

public class ScatterPlot extends Visualization {


    public ScatterPlot(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
        name = "Scatter Plot";
    }

    @Override
    public void update() {
        super.update();

        int n = arrayController.getLength();
        int maxPrimitives = Math.min(Math.max(screenWidth * 2, 1), 4096);
        int stride = LodStride.forLength(n, maxPrimitives);

        for (int i = 0; i < n; i += stride) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            int barHeight = (arrayController.get(i) + 1) * (screenHeight - 5) / n;

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }

            arrayController.setMarker(i, Marker.NORMAL);

            float x =  PApplet.map(i, 0, n, 0, screenWidth);
            float y = screenHeight - barHeight;

            
            proc.pushMatrix();
            proc.circle(x, y, 3); //Classic
            proc.popMatrix();

        }
    }

}
