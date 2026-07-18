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

        int n = arrayController.getLength();
        int maxPrimitives = Math.max(1, Math.min(screenWidth, 2048));
        int stride = LodStride.forLength(n, maxPrimitives);
        int bucketCount = (n + stride - 1) / stride;
        int rectWidth = Math.max(1, (screenWidth - (bucketCount - 1)) / bucketCount);

        for (int i = 0; i < n; i += stride) {
            int bucketEnd = Math.min(i + stride, n);
            int maxValuePlusOne = 0;
            int colorIndex = i;
            int soundIndex = -1;

            for (int j = i; j < bucketEnd; j++) {
                int valuePlusOne = arrayController.get(j) + 1;
                if (valuePlusOne > maxValuePlusOne) {
                    maxValuePlusOne = valuePlusOne;
                    colorIndex = j;
                }
                if (arrayController.getMarker(j) == Marker.SET) {
                    soundIndex = j;
                }
            }

            if (soundIndex >= 0) {
                sound.playSound(soundIndex);
            }

            Color color = colorGradient.getMarkerColor(arrayController.get(colorIndex), arrayController.getMarker(colorIndex));

            for (int j = i; j < bucketEnd; j++) {
                arrayController.setMarker(j, Marker.NORMAL);
            }

            int barHeight = maxValuePlusOne * (screenHeight - 5) / n;

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());

            proc.rect(PApplet.map(i, 0, n, 0, screenWidth), screenHeight, rectWidth, -1 * barHeight); //Classic bar
        }
    }

}
