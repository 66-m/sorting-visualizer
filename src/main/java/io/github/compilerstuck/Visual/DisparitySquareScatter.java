package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

import java.awt.*;

public class DisparitySquareScatter extends Visualization {

    int sideLength;

    public DisparitySquareScatter(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Disparity Square Scatter";
    }

    @Override
    public void update() {
        super.update();

        sideLength = (int) (Math.min(screenHeight, screenWidth) / 2.4) * 2;
        int sideLengthX = (int) screenWidth; //(int) (screenWidth / 2.4) * 2;
        int sideLengthY = (int) screenHeight; //(int) (screenHeight / 2.4) * 2;

        for (int i = 0; i < arrayController.getLength(); i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            //proc.stroke(color.getRGB());
            proc.noStroke();
            proc.fill(color.getRGB());

            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }

            arrayController.setMarker(i, Marker.NORMAL);

            double barHeight = ((screenHeight - 10.) / arrayController.getLength() * (arrayController.getLength() - 2 * Math.min(Math.min(Math.abs(i - arrayController.get(i)), Math.abs(i - arrayController.getLength() - arrayController.get(i))), Math.abs(i + arrayController.getLength() - arrayController.get(i))))) / (screenHeight);


            int centerX = screenWidth/2;
            int centerY = screenHeight/2;
            
            int x, y;
            if (i < arrayController.getLength() / 4) {
                x = - sideLengthX/2 + i * sideLengthX / (arrayController.getLength() / 4);
                y = - sideLengthY/2;
            } else if (i < arrayController.getLength() / 2) {
                x = + sideLengthX/2;
                y = - sideLengthY/2 + (i%(arrayController.getLength()/4)) * sideLengthY / (arrayController.getLength() / 4);
            } else if (i < 3 * arrayController.getLength() / 4) {
                x = + sideLengthX/2 - (i%(arrayController.getLength()/4)) * sideLengthX / (arrayController.getLength() / 4);
                y = + sideLengthY/2;
            } else {
                x = - sideLengthX/2;
                y = + sideLengthY/2 - (i%(arrayController.getLength()/4)) * sideLengthY / (arrayController.getLength() / 4);
            }

            x *= barHeight;
            y *= barHeight;

            proc.circle(centerX + x, centerY + y, 6);
        }
    }
}
