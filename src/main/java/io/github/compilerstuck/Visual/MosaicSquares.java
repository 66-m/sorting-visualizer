package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

import static java.lang.Math.floor;

import java.lang.Math;

import java.awt.*;

public class MosaicSquares extends Visualization {

    public MosaicSquares(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        super(arrayController, colorGradient, sound);
        name = "Mosaic Squares";
    }

    @Override
    public void update() {
        super.update();

        if (Math.pow(floor(Math.pow(arrayController.getLength(), 1 / 2.)), 2.) != arrayController.getLength()) {
            int nextN = (int) (floor(Math.pow(arrayController.getLength(), 1 / 2.) + 0.1)); // next lower

            MainController.updateArraySize(nextN * nextN); // Update arraySize
        }

        float squareRoot = (int) Math.pow(arrayController.getLength(), 1 / 2.);

        float tileDimX = screenWidth / squareRoot;
        float tileDimY = screenHeight / squareRoot;


        for (int i = 0; i < arrayController.getLength(); i++) {
            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));


            if (arrayController.getMarker(i) == Marker.SET) {
                sound.playSound(i);
            }

            arrayController.setMarker(i, Marker.NORMAL);

            proc.stroke(color.getRGB());
            proc.fill(color.getRGB());
           
            proc.rect((i % squareRoot) * tileDimX, (int)(i / squareRoot) * tileDimY, tileDimX, tileDimY);

        }
    }

}
